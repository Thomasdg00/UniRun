package com.univpm.unirun

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import com.univpm.unirun.data.repository.AuthenticationRepository
import com.univpm.unirun.ui.auth.AuthViewModelProvider
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
        window.statusBarColor = ContextCompat.getColor(this, R.color.kinetic_background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.kinetic_background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }
        // NavHostFragment è configurato direttamente nel layout XML.
        // Startup navigation logic is handled by AuthFragment/OnboardingFragment
        // which check auth state and navigate accordingly.
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
