package com.univpm.unirun.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.univpm.unirun.ui.components.UniRunButton
import com.univpm.unirun.ui.components.UniRunButtonVariant
import com.univpm.unirun.ui.components.UniRunInputField
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
    Scaffold { innerPadding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "UNIRUN",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "RUN WITH\nINTENTION",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Accedi per riprendere tracking, profilo e cronologia attività in un'unica shell Compose.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            UniRunInputField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "athlete@unirun.app",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Text("MAIL", style = MaterialTheme.typography.labelSmall) }
            )
            UniRunInputField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "Minimum 6 characters",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = { Text("KEY", style = MaterialTheme.typography.labelSmall) }
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            UniRunButton(
                text = if (isLoading) "ACCESSO IN CORSO" else "ACCEDI",
                onClick = onLogin,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            UniRunButton(
                text = "CONTINUA CON GOOGLE",
                onClick = onGoogleSignInRequest,
                enabled = !isLoading,
                variant = UniRunButtonVariant.Surface,
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Recupera password")
            }
            TextButton(onClick = onRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(
                    text = "Crea un account",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AuthRoute(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onGoogleSignInRequest: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var launcherError by remember { mutableStateOf<String?>(null) }
    val authState by viewModel.authState.collectAsStateWithLifecycle(initialValue = com.univpm.unirun.viewmodel.AuthState.Unauthenticated)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            launcherError = null
            viewModel.signInWithGoogle(parseGoogleSignInAccount(result.data))
        } catch (error: Exception) {
            launcherError = error.localizedMessage ?: "Errore durante il Google Sign-In"
        }
    }

    androidx.compose.runtime.LaunchedEffect(authState) {
        when (authState) {
            is com.univpm.unirun.viewmodel.AuthState.Authenticated -> onNavigateToHome()
            is com.univpm.unirun.viewmodel.AuthState.OnboardingNeeded -> onNavigateToOnboarding()
            else -> Unit
        }
    }

    val errorMessage = launcherError ?: if (authState is com.univpm.unirun.viewmodel.AuthState.Error) {
        (authState as com.univpm.unirun.viewmodel.AuthState.Error).message
    } else {
        null
    }

    AuthScreen(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        onLogin = { viewModel.signInWithEmail(email, password) },
        onGoogleSignInRequest = {
            onGoogleSignInRequest?.invoke() ?: launchGoogleSignIn(context) { googleLauncher.launch(it) }
        },
        onForgotPassword = { viewModel.resetPassword(email) },
        onRegister = onNavigateToRegister,
        isLoading = isLoading,
        errorMessage = errorMessage
    )
}
