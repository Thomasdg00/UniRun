package com.univpm.unirun

import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import com.univpm.unirun.data.repository.AuthenticationRepository
import com.univpm.unirun.ui.auth.AuthViewModelProvider
import com.univpm.unirun.ui.navigation.UniRunNavHost
import com.univpm.unirun.ui.theme.UniRunTheme
import com.univpm.unirun.viewmodel.AuthViewModel

/**
 * Main activity that implements AuthViewModelProvider to inject AuthViewModel
 * into fragments (AuthFragment, OnboardingFragment).
 * 
 * This simple DI pattern allows fragments to access the shared ViewModel.
 * In production, consider using Hilt for proper dependency injection.
 */
class MainActivity : AppCompatActivity(), AuthViewModelProvider {

    // Lazy initialization of AuthViewModel
    override val authViewModel: AuthViewModel by lazy {
        val database = AppDatabase.getInstance(this)
        val preferencesRepository = UserPreferencesRepository(this)
        val authRepository = AuthenticationRepository(
            context = this,
            database = database,
            preferencesRepository = preferencesRepository
        )
        ViewModelProvider(
            this,
            AuthViewModelFactory(authRepository)
        )[AuthViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            UniRunTheme {
                UniRunNavHost(authViewModel = authViewModel)
            }
        }
    }
}

/**
 * Simple ViewModelFactory to provide AuthViewModel with its repository dependency.
 */
class AuthViewModelFactory(private val repository: AuthenticationRepository) :
    ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
