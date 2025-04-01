// 메인 엑티비티
package com.luckydut97.sociallogin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.luckydut97.sociallogin.ui.screens.LoginScreen
import com.luckydut97.sociallogin.ui.theme.SocialLoginTheme
import com.luckydut97.sociallogin.viewmodel.SocialLoginViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: SocialLoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
            .get(SocialLoginViewModel::class.java)

        setContent {
            SocialLoginTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(viewModel = viewModel)
                }
            }
        }
    }
}