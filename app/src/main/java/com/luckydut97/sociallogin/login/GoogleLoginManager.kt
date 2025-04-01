package com.luckydut97.sociallogin.login

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.luckydut97.sociallogin.model.SocialPlatform
import com.luckydut97.sociallogin.model.UserProfile

/**
 * 구글 로그인 관련 로직을 처리하는 매니저 클래스
 */
class GoogleLoginManager(private val application: Application) : SocialLoginManager {

    private val TAG = "GoogleLoginManager"

    // 구글 로그인 클라이언트
    private val googleSignInClient: GoogleSignInClient

    init {
        // 구글 로그인 초기화
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, gso)
    }

    /**
     * 구글 로그인을 위한 클라이언트 반환
     */
    fun getSignInClient(): GoogleSignInClient = googleSignInClient

    /**
     * 구글 로그인 결과 처리
     */
    fun handleSignInResult(result: ActivityResult, callback: SocialLoginManager.LoginCallback) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)

            // 계정 정보로 UserProfile 생성
            val userProfile = createUserProfile(account)
            Log.d(TAG, "Google login success: ${account.displayName}")
            callback.onSuccess(userProfile)
        } catch (e: ApiException) {
            Log.e(TAG, "Google login failed", e)
            callback.onFailure("구글 로그인에 실패했습니다: ${e.message}")
        }
    }

    /**
     * GoogleSignInAccount에서 UserProfile 생성
     */
    private fun createUserProfile(account: GoogleSignInAccount): UserProfile {
        return UserProfile(
            id = account.id ?: "",
            name = account.displayName ?: "",
            email = account.email ?: "",
            profileImageUrl = account.photoUrl?.toString(),
            platformType = SocialPlatform.GOOGLE
        )
    }

    /**
     * 구글 로그아웃
     */
    override fun logout(callback: SocialLoginManager.LogoutCallback) {
        googleSignInClient.signOut().addOnCompleteListener {
            callback.onComplete()
        }
    }
}