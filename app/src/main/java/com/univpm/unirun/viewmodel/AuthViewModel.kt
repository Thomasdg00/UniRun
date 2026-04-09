package com.univpm.unirun.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.univpm.unirun.data.repository.AuthenticationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Sealed class that represents the different authentication states of the app.
 * - Loading: Auth operation in progress
 * - Authenticated: User is logged in
 * - Unauthenticated: No user logged in
 * - OnboardingNeeded: User logged in but hasn't completed onboarding
 * - Error: Auth operation failed with a message
 */
sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object OnboardingNeeded : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthenticationRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Attempts to sign in with email and password.
     * Validates input before making Firebase call.
     * On success, checks if onboarding is needed.
     */
    fun signInWithEmail(email: String, password: String) {
        val validationError = validateEmailPassword(email, password)
        if (validationError != null) {
            _authState.value = AuthState.Error(validationError)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.signInWithEmail(email, password)
                val onboardingDone = repository.isOnboardingDone()
                _authState.value = if (onboardingDone) {
                    AuthState.Authenticated
                } else {
                    AuthState.OnboardingNeeded
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                _authState.value = AuthState.Error("Utente non trovato")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.Error("Email o password non valida")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Errore di accesso")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Creates a new user account with email and password.
     * Validates email format and password strength.
     * On success, saves user profile and triggers onboarding.
     */
    fun signUpWithEmail(email: String, password: String) {
        val validationError = validateEmailPassword(email, password)
        if (validationError != null) {
            _authState.value = AuthState.Error(validationError)
            return
        }

        val passwordStrengthError = validatePasswordStrength(password)
        if (passwordStrengthError != null) {
            _authState.value = AuthState.Error(passwordStrengthError)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.signUpWithEmail(email, password)
                _authState.value = AuthState.OnboardingNeeded
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState.Error("Email già registrata")
            } catch (e: FirebaseAuthWeakPasswordException) {
                _authState.value = AuthState.Error("Password troppo debole (min 8 caratteri)")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Errore di registrazione")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Signs in with Google account using ID token.
     * Automatically saves user profile on first login.
     * Checks onboarding status after successful auth.
     */
    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val idToken = account.idToken
                    ?: throw IllegalArgumentException("ID token non disponibile")
                
                val isNewUser = repository.signInWithGoogle(idToken)
                val onboardingDone = repository.isOnboardingDone()
                
                _authState.value = when {
                    isNewUser || !onboardingDone -> AuthState.OnboardingNeeded
                    else -> AuthState.Authenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Errore di accesso Google")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sends password reset email to user.
     * Email must be valid format.
     */
    fun resetPassword(email: String) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Inserisci una email valida")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.sendPasswordResetEmail(email)
                _authState.value = AuthState.Error("Email di recupero inviata. Controlla la tua casella postale.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Errore nell'invio email")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Logs out current user and clears auth state.
     * Does NOT clear user preferences (weight, height) for next login.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Errore durante logout")
            }
        }
    }

    /**
     * Saves user profile (name, weight, height) after onboarding.
     * Updates both DataStore and Room database.
     * Transitions to Authenticated state.
     */
    fun completeOnboarding(name: String, weightKg: Float, heightCm: Int) {
        val validationErrors = validateOnboarding(name, weightKg, heightCm)
        if (validationErrors != null) {
            _authState.value = AuthState.Error(validationErrors)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.saveUserProfile(name, weightKg, heightCm)
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Errore nel salvataggio profilo")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears the current error state so it's not displayed again.
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Checks if user is currently authenticated.
     */
    fun isUserAuthenticated(): Boolean {
        return repository.isUserAuthenticated()
    }

    // ==================== Validation Functions ====================

    private fun validateEmailPassword(email: String, password: String): String? {
        return when {
            email.isEmpty() -> "Inserisci un'email"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email non valida"
            password.isEmpty() -> "Inserisci una password"
            password.length < 6 -> "Password: minimo 6 caratteri"
            else -> null
        }
    }

    /**
     * Password must be at least 8 characters, contain uppercase and a number.
     */
    private fun validatePasswordStrength(password: String): String? {
        return when {
            password.length < 8 -> "Password: minimo 8 caratteri"
            !password.any { it.isUpperCase() } -> "Password deve contenere almeno una maiuscola"
            !password.any { it.isDigit() } -> "Password deve contenere almeno un numero"
            else -> null
        }
    }

    /**
     * Validates onboarding form data.
     */
    private fun validateOnboarding(name: String, weightKg: Float, heightCm: Int): String? {
        return when {
            name.trim().isEmpty() -> "Inserisci il tuo nome"
            name.trim().length < 2 -> "Nome deve essere almeno 2 caratteri"
            weightKg < 30f || weightKg > 300f -> "Peso deve essere tra 30 e 300 kg"
            heightCm < 100 || heightCm > 250 -> "Altezza deve essere tra 100 e 250 cm"
            else -> null
        }
    }
}
