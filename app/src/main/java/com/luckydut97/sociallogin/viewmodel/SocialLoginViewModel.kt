package com.luckydut97.sociallogin.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.luckydut97.sociallogin.login.GoogleLoginManager
import com.luckydut97.sociallogin.login.KakaoLoginManager
import com.luckydut97.sociallogin.login.NaverLoginManager
import com.luckydut97.sociallogin.login.SocialLoginManager
import com.luckydut97.sociallogin.model.SocialPlatform
import com.luckydut97.sociallogin.model.UserProfile
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

    // 로그인 매니저 초기화
    private val googleLoginManager = GoogleLoginManager(application)
    private val kakaoLoginManager = KakaoLoginManager()
    private val naverLoginManager = NaverLoginManager(application)

    // 로그인 콜백 객체
    private val loginCallback = object : SocialLoginManager.LoginCallback {
        override fun onStart() {
            _isLoading.value = true
        }

        override fun onSuccess(userProfile: UserProfile) {
            _isLoading.value = false
            _userProfile.value = userProfile
            Log.d(TAG, "${userProfile.platformType} 로그인 성공: ${userProfile.name}")
        }

        override fun onFailure(errorMessage: String) {
            _isLoading.value = false
            _errorMessage.value = errorMessage
            Log.e(TAG, "로그인 실패: $errorMessage")
        }
    }

    // 로그아웃 콜백 객체
    private val logoutCallback = object : SocialLoginManager.LogoutCallback {
        override fun onComplete() {
            _userProfile.value = null
            Log.d(TAG, "로그아웃 완료")
        }
    }

    // 구글 로그인 클라이언트 반환
    fun getGoogleSignInClient(): GoogleSignInClient {
        _isLoading.value = true
        return googleLoginManager.getSignInClient()
    }

    // 구글 로그인 결과 처리
    fun handleGoogleSignInResult(result: ActivityResult) {
        googleLoginManager.handleSignInResult(result, loginCallback)
    }

    // 카카오 로그인
    fun loginWithKakao(context: Context) {
        kakaoLoginManager.login(context, loginCallback)
    }

    // 네이버 로그인
    fun loginWithNaver() {
        naverLoginManager.login(loginCallback)
    }

    // 로그아웃
    fun logout() {
        when (_userProfile.value?.platformType) {
            SocialPlatform.GOOGLE -> googleLoginManager.logout(logoutCallback)
            SocialPlatform.KAKAO -> kakaoLoginManager.logout(logoutCallback)
            SocialPlatform.NAVER -> naverLoginManager.logout(logoutCallback)
            else -> _userProfile.value = null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}