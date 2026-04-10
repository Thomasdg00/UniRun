package com.univpm.unirun.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.univpm.unirun.viewmodel.AuthState
import com.univpm.unirun.viewmodel.AuthViewModel

@Composable
fun RegisterRoute(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val context = LocalContext.current
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    val authState by viewModel.authState.collectAsStateWithLifecycle(initialValue = AuthState.Unauthenticated)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            localError = null
            viewModel.signInWithGoogle(parseGoogleSignInAccount(result.data))
        } catch (error: Exception) {
            localError = error.localizedMessage ?: "Errore durante il Google Sign-In"
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.OnboardingNeeded,
            is AuthState.Authenticated -> onNavigateToOnboarding()

            else -> Unit
        }
    }

    val errorMessage = localError ?: (authState as? AuthState.Error)?.message

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "JOIN UNIRUN",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Crea l'account e completa il profilo nel passo successivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            UniRunInputField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full name",
                placeholder = "Alex Runner",
                leadingIcon = { Text("ID", style = MaterialTheme.typography.labelSmall) }
            )
            UniRunInputField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "athlete@unirun.app",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Text("MAIL", style = MaterialTheme.typography.labelSmall) }
            )
            UniRunInputField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Minimum 8 characters",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Text("KEY", style = MaterialTheme.typography.labelSmall) }
            )
            UniRunInputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm password",
                placeholder = "Repeat password",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Text("OK", style = MaterialTheme.typography.labelSmall) }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it }
                )
                Text(
                    text = "Accetto termini e privacy policy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            UniRunButton(
                text = if (isLoading) "CREAZIONE ACCOUNT" else "CREA ACCOUNT",
                onClick = {
                    localError = when {
                        fullName.isBlank() -> "Inserisci il nome completo"
                        password != confirmPassword -> "Le password non coincidono"
                        !acceptedTerms -> "Devi accettare termini e privacy policy"
                        else -> null
                    }
                    if (localError == null) {
                        viewModel.signUpWithEmail(email.trim(), password)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            UniRunButton(
                text = "CONTINUA CON GOOGLE",
                onClick = { launchGoogleSignIn(context) { googleLauncher.launch(it) } },
                enabled = !isLoading,
                variant = UniRunButtonVariant.Surface,
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            TextButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(
                    text = "Hai già un account? Accedi",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
