package com.univpm.unirun.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.univpm.unirun.ui.components.KineticInputField
import com.univpm.unirun.ui.theme.KineticTheme
import com.univpm.unirun.viewmodel.AuthState
import com.univpm.unirun.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onComplete: (String, Float, Int) -> Unit, isLoading: Boolean) {
    val name = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf(70f) }
    val height = remember { mutableStateOf(170) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        KineticInputField(value = name.value, onValueChange = { name.value = it }, label = "Name")
        KineticInputField(value = weight.value.toString(), onValueChange = { weight.value = it.toFloatOrNull() ?: weight.value }, label = "Weight (kg)")
        KineticInputField(value = height.value.toString(), onValueChange = { height.value = it.toIntOrNull() ?: height.value }, label = "Height (cm)")
        Button(onClick = { onComplete(name.value, weight.value, height.value) }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Continue")
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

    KineticTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
            OnboardingScreen(onComplete = { name, weight, height ->
                viewModel.completeOnboarding(name, weight, height)
            }, isLoading = isLoading)
        }
    }
}
