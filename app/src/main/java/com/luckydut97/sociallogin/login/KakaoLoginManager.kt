package com.luckydut97.sociallogin.login

import android.content.Context
import android.util.Log
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.luckydut97.sociallogin.model.SocialPlatform
import com.luckydut97.sociallogin.model.UserProfile

/**
 * 카카오 로그인 관련 로직을 처리하는 매니저 클래스
 */
class KakaoLoginManager : SocialLoginManager {

    private val TAG = "KakaoLoginManager"

    /**
     * 카카오 로그인 시작
     * @param context 컨텍스트
     * @param callback 로그인 콜백
     */
    fun login(context: Context, callback: SocialLoginManager.LoginCallback) {
        callback.onStart()

        // 카카오톡이 설치되어 있는지 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡 앱으로 로그인
            loginWithKakaoTalk(context, callback)
        } else {
            // 카카오톡이 설치되어 있지 않은 경우 카카오 계정으로 로그인
            loginWithKakaoAccount(context, callback)
        }
    }

    /**
     * 카카오톡 앱을 통한 로그인
     */
    private fun loginWithKakaoTalk(
        context: Context,
        callback: SocialLoginManager.LoginCallback
    ) {
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오톡 로그인 실패", error)

                // 사용자가 취소한 경우
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    callback.onFailure("로그인이 취소되었습니다.")
                    return@loginWithKakaoTalk
                }

                // 카카오톡 로그인 실패 시 카카오 계정으로 로그인 시도
                loginWithKakaoAccount(context, callback)
            } else if (token != null) {
                // 로그인 성공, 사용자 정보 요청
                requestUserInfo(callback)
            }
        }
    }

    /**
     * 카카오 계정을 통한 로그인
     */
    private fun loginWithKakaoAccount(
        context: Context,
        callback: SocialLoginManager.LoginCallback
    ) {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) {
                callback.onFailure("카카오 로그인에 실패했습니다: ${error.message}")
                Log.e(TAG, "카카오 계정 로그인 실패", error)
            } else if (token != null) {
                requestUserInfo(callback)
            }
        }
    }

    /**
     * 카카오 사용자 정보 요청
     */
    private fun requestUserInfo(callback: SocialLoginManager.LoginCallback) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                callback.onFailure("사용자 정보 요청에 실패했습니다: ${error.message}")
                Log.e(TAG, "카카오 사용자 정보 요청 실패", error)
            } else if (user != null) {
                val userProfile = UserProfile(
                    id = user.id.toString(),
                    name = user.kakaoAccount?.profile?.nickname ?: "",
                    email = user.kakaoAccount?.email ?: "",
                    profileImageUrl = user.kakaoAccount?.profile?.thumbnailImageUrl,
                    platformType = SocialPlatform.KAKAO
                )
                Log.d(TAG, "카카오 로그인 성공: ${userProfile.name}")
                callback.onSuccess(userProfile)
            } else {
                callback.onFailure("사용자 정보가 없습니다.")
            }
        }
    }

    /**
     * 카카오 로그아웃
     */
    override fun logout(callback: SocialLoginManager.LogoutCallback) {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e(TAG, "카카오 로그아웃 실패", error)
            }
            callback.onComplete()
        }
    }
}