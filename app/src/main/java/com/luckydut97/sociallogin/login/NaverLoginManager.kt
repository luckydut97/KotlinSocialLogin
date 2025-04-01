package com.luckydut97.sociallogin.login

import android.app.Application
import android.util.Log
import com.luckydut97.sociallogin.model.SocialPlatform
import com.luckydut97.sociallogin.model.UserProfile
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse

/**
 * 네이버 로그인 관련 로직을 처리하는 매니저 클래스
 */
class NaverLoginManager(private val application: Application) : SocialLoginManager {

    private val TAG = "NaverLoginManager"

    /**
     * 네이버 로그인 시작
     * @param callback 로그인 콜백
     */
    fun login(callback: SocialLoginManager.LoginCallback) {
        callback.onStart()

        // 네이버 로그인 콜백
        val loginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                // 액세스 토큰 얻기
                val accessToken = NaverIdLoginSDK.getAccessToken()
                if (accessToken != null) {
                    Log.d(TAG, "네이버 로그인 성공: 액세스 토큰 획득")
                    requestUserInfo(callback)
                } else {
                    callback.onFailure("네이버 토큰을 받지 못했습니다.")
                    Log.e(TAG, "네이버 로그인 실패: 토큰이 null입니다.")
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                callback.onFailure("네이버 로그인에 실패했습니다: $message")
                Log.e(TAG, "네이버 로그인 실패: $message (HTTP: $httpStatus)")
            }

            override fun onError(errorCode: Int, message: String) {
                callback.onFailure("네이버 에러: $message")
                Log.e(TAG, "네이버 에러: $message (코드: $errorCode)")
            }
        }

        // 네이버 로그인 실행
        NaverIdLoginSDK.authenticate(application, loginCallback)
    }

    /**
     * 네이버 사용자 정보 요청
     */
    private fun requestUserInfo(callback: SocialLoginManager.LoginCallback) {
        NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(result: NidProfileResponse) {
                val profile = result.profile

                if (profile != null) {
                    val userProfile = UserProfile(
                        id = profile.id ?: "",
                        name = profile.name ?: "",
                        email = profile.email ?: "",
                        profileImageUrl = profile.profileImage,
                        platformType = SocialPlatform.NAVER
                    )
                    Log.d(TAG, "네이버 로그인 성공: ${profile.name}")
                    callback.onSuccess(userProfile)
                } else {
                    callback.onFailure("네이버 프로필 정보가 비어있습니다.")
                    Log.e(TAG, "네이버 프로필 정보가 null입니다.")
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                callback.onFailure("네이버 프로필 요청에 실패했습니다: $message")
                Log.e(TAG, "네이버 프로필 요청 실패: $message (HTTP: $httpStatus)")
            }

            override fun onError(errorCode: Int, message: String) {
                callback.onFailure("네이버 에러: $message")
                Log.e(TAG, "네이버 에러: $message (코드: $errorCode)")
            }
        })
    }

    /**
     * 네이버 로그아웃
     */
    override fun logout(callback: SocialLoginManager.LogoutCallback) {
        NaverIdLoginSDK.logout()
        callback.onComplete()
    }
}