package com.luckydut97.sociallogin.login

import com.luckydut97.sociallogin.model.UserProfile

/**
 * 소셜 로그인 매니저 인터페이스
 * 모든 소셜 로그인은 이 인터페이스를 구현합니다.
 */
interface SocialLoginManager {

    /**
     * 로그인 콜백 인터페이스
     */
    interface LoginCallback {
        fun onStart()
        fun onSuccess(userProfile: UserProfile)
        fun onFailure(errorMessage: String)
    }

    /**
     * 로그아웃 콜백 인터페이스
     */
    interface LogoutCallback {
        fun onComplete()
    }

    /**
     * 로그아웃 실행
     */
    fun logout(callback: LogoutCallback)
}