package com.univpm.unirun.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.univpm.unirun.ui.components.KineticButton
import com.univpm.unirun.ui.components.KineticInputField
import com.univpm.unirun.ui.theme.KineticTheme
import com.univpm.unirun.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onGoogleSignInRequest: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        KineticInputField(value = email, onValueChange = onEmailChange, label = "Email")
        KineticInputField(value = password, onValueChange = onPasswordChange, label = "Password")

        if (!errorMessage.isNullOrEmpty()) {
            Text(text = errorMessage, modifier = Modifier.padding(top = 8.dp))
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        KineticButton(onClick = onLogin, text = "Login", modifier = Modifier.padding(top = 12.dp))
        Button(onClick = onGoogleSignInRequest, modifier = Modifier.padding(top = 8.dp)) { Text("Sign in with Google") }
        Button(onClick = onForgotPassword, modifier = Modifier.padding(top = 8.dp)) { Text("Forgot password") }
        Button(onClick = onRegister, modifier = Modifier.padding(top = 8.dp)) { Text("Register") }
    }
}

@Composable
fun AuthRoute(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onGoogleSignInRequest: () -> Unit
) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsStateWithLifecycle(initialValue = com.univpm.unirun.viewmodel.AuthState.Unauthenticated)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)

    // react to auth state changes
    when (authState) {
        is com.univpm.unirun.viewmodel.AuthState.Authenticated -> onNavigateToHome()
        is com.univpm.unirun.viewmodel.AuthState.OnboardingNeeded -> onNavigateToOnboarding()
        else -> { /* no-op */ }
    }

    val errorMessage = if (authState is com.univpm.unirun.viewmodel.AuthState.Error) (authState as com.univpm.unirun.viewmodel.AuthState.Error).message else null

    KineticTheme {
        AuthScreen(
            email = emailState.value,
            onEmailChange = { emailState.value = it },
            password = passwordState.value,
            onPasswordChange = { passwordState.value = it },
            onLogin = { viewModel.signInWithEmail(emailState.value, passwordState.value) },
            onGoogleSignInRequest = onGoogleSignInRequest,
            onForgotPassword = { /* show dialog via fragment callback */ viewModel.resetPassword(emailState.value) },
            onRegister = onNavigateToRegister,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }
}
