package com.luckydut97.sociallogin.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.luckydut97.sociallogin.model.SocialPlatform
import com.luckydut97.sociallogin.model.UserProfile
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SocialLoginViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "SocialLoginViewModel"

    // 사용자 프로필 정보
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 에러 메시지
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 구글 로그인 클라이언트
    private var googleSignInClient: GoogleSignInClient

    init {
        // 구글 로그인 초기화
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, gso)
    }

    // 구글 로그인
    fun getGoogleSignInClient(): GoogleSignInClient = googleSignInClient

    fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        _isLoading.value = false

        account?.let {
            _userProfile.value = UserProfile(
                id = it.id ?: "",
                name = it.displayName ?: "",
                email = it.email ?: "",
                profileImageUrl = it.photoUrl?.toString(),
                platformType = SocialPlatform.GOOGLE
            )
            Log.d(TAG, "Google login success: ${it.displayName}")
        } ?: run {
            _errorMessage.value = "구글 로그인에 실패했습니다."
            Log.e(TAG, "Google login failed: account is null")
        }
    }

    // 카카오 로그인
    fun loginWithKakao(context: Context) {
        _isLoading.value = true

        // 카카오톡 앱이 설치되어 있는지 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡 앱으로 로그인
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡 로그인 실패", error)

                    // 사용자가 취소한 경우
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        _isLoading.value = false
                        _errorMessage.value = "로그인이 취소되었습니다."
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡 로그인 실패 시 카카오 계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoLoginCallback)
                } else if (token != null) {
                    // 로그인 성공, 사용자 정보 요청
                    getUserInfoFromKakao()
                }
            }
        } else {
            // 카카오톡이 설치되어 있지 않은 경우 카카오 계정으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoLoginCallback)
        }
    }

    // 카카오 로그인 콜백
    private val kakaoLoginCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            _isLoading.value = false
            _errorMessage.value = "카카오 로그인에 실패했습니다: ${error.message}"
            Log.e(TAG, "카카오 계정 로그인 실패", error)
        } else if (token != null) {
            getUserInfoFromKakao()
        }
    }

    // 카카오 사용자 정보 요청
    private fun getUserInfoFromKakao() {
        UserApiClient.instance.me { user, error ->
            _isLoading.value = false

            if (error != null) {
                _errorMessage.value = "사용자 정보 요청에 실패했습니다: ${error.message}"
                Log.e(TAG, "카카오 사용자 정보 요청 실패", error)
            } else if (user != null) {
                _userProfile.value = UserProfile(
                    id = user.id.toString(),
                    name = user.kakaoAccount?.profile?.nickname ?: "",
                    email = user.kakaoAccount?.email ?: "",
                    profileImageUrl = user.kakaoAccount?.profile?.thumbnailImageUrl,
                    platformType = SocialPlatform.KAKAO
                )
                Log.d(TAG, "카카오 로그인 성공: ${user.kakaoAccount?.profile?.nickname}")
            }
        }
    }

    // 네이버 로그인
    fun loginWithNaver() {
        _isLoading.value = true

        // 명시적인 OAuthLoginCallback 객체 사용
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                // 액세스 토큰 얻기
                val accessToken = NaverIdLoginSDK.getAccessToken()
                if (accessToken != null) {
                    Log.d(TAG, "네이버 로그인 성공: 액세스 토큰 획득")
                    getUserInfoFromNaver()
                } else {
                    _isLoading.value = false
                    _errorMessage.value = "네이버 토큰을 받지 못했습니다."
                    Log.e(TAG, "네이버 로그인 실패: 토큰이 null입니다.")
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                _isLoading.value = false
                _errorMessage.value = "네이버 로그인에 실패했습니다: $message"
                Log.e(TAG, "네이버 로그인 실패: $message (HTTP: $httpStatus)")
            }

            override fun onError(errorCode: Int, message: String) {
                _isLoading.value = false
                _errorMessage.value = "네이버 에러: $message"
                Log.e(TAG, "네이버 에러: $message (코드: $errorCode)")
            }
        }

        // 네이버 로그인 SDK 인증
        NaverIdLoginSDK.authenticate(getApplication(), oauthLoginCallback)
    }

    // 네이버 사용자 정보 요청
    private fun getUserInfoFromNaver() {
        NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(result: NidProfileResponse) {
                _isLoading.value = false
                val profile = result.profile

                if (profile != null) {
                    _userProfile.value = UserProfile(
                        id = profile.id ?: "",
                        name = profile.name ?: "",
                        email = profile.email ?: "",
                        profileImageUrl = profile.profileImage,
                        platformType = SocialPlatform.NAVER
                    )
                    Log.d(TAG, "네이버 로그인 성공: ${profile.name}")
                } else {
                    _errorMessage.value = "네이버 프로필 정보가 비어있습니다."
                    Log.e(TAG, "네이버 프로필 정보가 null입니다.")
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                _isLoading.value = false
                _errorMessage.value = "네이버 프로필 요청에 실패했습니다: $message"
                Log.e(TAG, "네이버 프로필 요청 실패: $message (HTTP: $httpStatus)")
            }

            override fun onError(errorCode: Int, message: String) {
                _isLoading.value = false
                _errorMessage.value = "네이버 에러: $message"
                Log.e(TAG, "네이버 에러: $message (코드: $errorCode)")
            }
        })
    }

    // 로그아웃
    fun logout() {
        when (_userProfile.value?.platformType) {
            SocialPlatform.GOOGLE -> {
                googleSignInClient.signOut().addOnCompleteListener {
                    _userProfile.value = null
                }
            }
            SocialPlatform.KAKAO -> {
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        Log.e(TAG, "카카오 로그아웃 실패", error)
                    } else {
                        _userProfile.value = null
                    }
                }
            }
            SocialPlatform.NAVER -> {
                NaverIdLoginSDK.logout()
                _userProfile.value = null
            }
            else -> _userProfile.value = null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}