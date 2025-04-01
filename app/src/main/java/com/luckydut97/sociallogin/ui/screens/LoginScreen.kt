package com.luckydut97.sociallogin.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.luckydut97.sociallogin.model.SocialPlatform
import com.luckydut97.sociallogin.ui.components.SocialLoginButton
import com.luckydut97.sociallogin.viewmodel.SocialLoginViewModel

@Composable
fun LoginScreen(viewModel: SocialLoginViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 구글 로그인 결과 처리
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.handleGoogleSignInResult(account)
        } catch (e: ApiException) {
            Toast.makeText(context, "구글 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 에러 메시지 처리
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // 로그인한 사용자가 있으면 프로필 화면 표시
    if (userProfile != null) {
        ProfileScreen(userProfile = userProfile!!, onLogout = { viewModel.logout() })
        return
    }

    // 로딩 중이면 로딩 인디케이터 표시
    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "로딩 중...")
        }
        return
    }

    // 로그인 화면
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 구글 로그인 버튼
            SocialLoginButton(
                platform = SocialPlatform.GOOGLE,
                text = "Google로 시작하기",
                onClick = {
                    googleSignInLauncher.launch(viewModel.getGoogleSignInClient().signInIntent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 카카오 로그인 버튼
            SocialLoginButton(
                platform = SocialPlatform.KAKAO,
                text = "Kakao로 시작하기",
                onClick = { viewModel.loginWithKakao(context) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 네이버 로그인 버튼
            SocialLoginButton(
                platform = SocialPlatform.NAVER,
                text = "Naver로 시작하기",
                onClick = { viewModel.loginWithNaver() }
            )
        }
    }
}