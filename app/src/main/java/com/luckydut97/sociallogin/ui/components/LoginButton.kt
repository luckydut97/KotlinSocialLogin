package com.luckydut97.sociallogin.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luckydut97.sociallogin.R
import com.luckydut97.sociallogin.model.SocialPlatform

@Composable
fun SocialLoginButton(
    platform: SocialPlatform,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 소셜 플랫폼별 색상 및 아이콘 설정
    val (backgroundColor, textColor, iconRes) = when (platform) {
        SocialPlatform.GOOGLE -> Triple(Color.White, Color.Black, R.drawable.google_logo)
        SocialPlatform.KAKAO -> Triple(Color(0xFFFFE500), Color.Black, R.drawable.kakao_logo)
        SocialPlatform.NAVER -> Triple(Color(0xFF03C75A), Color.White, R.drawable.naver_logo)
        else -> Triple(Color.Gray, Color.White, 0)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 왼쪽 로고
            if (iconRes != 0) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "$platform 로그인",
                    modifier = Modifier
                        .size(46.dp)
                        .padding(end = 12.dp)
                )
            }

            // 텍스트(text 매개변수 = 구글, 카카오, 네이버로 시작하기)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}