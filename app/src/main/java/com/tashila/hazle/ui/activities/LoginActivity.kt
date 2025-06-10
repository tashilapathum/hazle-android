package com.tashila.hazle.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tashila.hazle.features.auth.AuthViewModel
import com.tashila.hazle.ui.components.AuroraBackground
import com.tashila.hazle.ui.screens.LoginScreen
import com.tashila.hazle.ui.screens.SignupScreen
import com.tashila.hazle.ui.theme.HazleTheme
import org.koin.androidx.compose.koinViewModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HazleTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AuroraBackground(modifier = Modifier.fillMaxSize())
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        val navController = rememberNavController()
                        val authViewModel: AuthViewModel = koinViewModel()

                        // Collect one-time auth events for navigation or toasts
                        LaunchedEffect(Unit) {
                            authViewModel.authEvent.collect { event ->
                                when (event) {
                                    AuthViewModel.AuthEvent.LoginSuccess -> {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Login Successful!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("home") { // Navigate to your main app screen
                                            popUpTo("login") {
                                                inclusive = true
                                            } // Clear back stack
                                        }
                                    }

                                    AuthViewModel.AuthEvent.SignupSuccess -> {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Signup Successful! Please login.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigate("login") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = "signup"
                        ) {
                            composable("login") {
                                LoginScreen(
                                    email = authViewModel.email.collectAsState().value,
                                    onEmailChange = authViewModel::onEmailChanged,
                                    password = authViewModel.password.collectAsState().value,
                                    onPasswordChange = authViewModel::onPasswordChanged,
                                    onLoginClick = authViewModel::onLoginClick,
                                    errorMessage = authViewModel.errorMessage.collectAsState().value,
                                    isLoading = authViewModel.isLoading.collectAsState().value,
                                    onSignupClick = {
                                        navController.navigate("signup")
                                        authViewModel::clearError
                                    },
                                )
                            }
                            composable("signup") {
                                SignupScreen(
                                    email = authViewModel.email.collectAsState().value,
                                    onEmailChange = authViewModel::onEmailChanged,
                                    password = authViewModel.password.collectAsState().value,
                                    onPasswordChange = authViewModel::onPasswordChanged,
                                    confirmPassword = authViewModel.confirmPassword.collectAsState().value,
                                    onConfirmPasswordChange = authViewModel::onConfirmPasswordChanged,
                                    onSignupClick = authViewModel::onSignupClick,
                                    errorMessage = authViewModel.errorMessage.collectAsState().value,
                                    isLoading = authViewModel.isLoading.collectAsState().value,
                                    onLoginClick = {
                                        navController.navigate("login")
                                        authViewModel::clearError
                                    },
                                )
                            }
                            composable("home") {
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}