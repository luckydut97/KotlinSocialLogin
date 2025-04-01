package com.luckydut97.sociallogin.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val platformType: SocialPlatform = SocialPlatform.NONE
)

enum class SocialPlatform {
    GOOGLE, KAKAO, NAVER, NONE
}