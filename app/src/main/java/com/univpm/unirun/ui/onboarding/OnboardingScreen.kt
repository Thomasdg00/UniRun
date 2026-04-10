package com.univpm.unirun.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.univpm.unirun.ui.components.UniRunButton
import com.univpm.unirun.ui.components.UniRunInputField
import com.univpm.unirun.viewmodel.AuthState
import com.univpm.unirun.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onComplete: (String, Float, Int) -> Unit, isLoading: Boolean) {
    val name = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf(70f) }
    val height = remember { mutableStateOf(170) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "COMPLETE PROFILE",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Peso e altezza servono per calcolare calorie, recupero e obiettivi giornalieri.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        UniRunInputField(value = name.value, onValueChange = { name.value = it }, label = "Name", placeholder = "Alex Runner")
        UniRunInputField(
            value = weight.value.toString(),
            onValueChange = { weight.value = it.toFloatOrNull() ?: weight.value },
            label = "Weight (kg)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        UniRunInputField(
            value = height.value.toString(),
            onValueChange = { height.value = it.toIntOrNull() ?: height.value },
            label = "Height (cm)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        UniRunButton(
            text = if (isLoading) "SALVATAGGIO" else "CONTINUA",
            onClick = { onComplete(name.value, weight.value, height.value) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun OnboardingRoute(
    viewModel: AuthViewModel,
    onFinished: () -> Unit
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle(initialValue = AuthState.Unauthenticated)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onFinished()
        } else if (authState is AuthState.Error) {
            val msg = (authState as AuthState.Error).message
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            OnboardingScreen(onComplete = { name, weight, height ->
                viewModel.completeOnboarding(name, weight, height)
            }, isLoading = isLoading)
        }
    }
}
