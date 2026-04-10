package com.univpm.unirun.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.univpm.unirun.R
import com.univpm.unirun.ui.theme.KineticTheme
import com.univpm.unirun.viewmodel.AuthViewModel

/**
 * Compose-based AuthFragment bridge. Keeps Google Sign-In launcher here
 * and delegates UI to `AuthRoute`.
 */
class AuthFragment : Fragment() {

    private val authViewModel: AuthViewModel by lazy {
        (requireActivity() as? AuthViewModelProvider)?.authViewModel
            ?: throw IllegalStateException("Activity must implement AuthViewModelProvider")
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(Exception::class.java)
            if (account != null) {
                authViewModel.signInWithGoogle(account)
            }
        } catch (e: Exception) {
            Snackbar.make(requireView(), "Errore Google Sign-In: ${e.localizedMessage}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val navController = findNavController()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KineticTheme {
                    AuthRoute(
                        viewModel = authViewModel,
                        onNavigateToHome = { navController.navigate(R.id.action_auth_to_home) },
                        onNavigateToOnboarding = { navController.navigate(R.id.action_auth_to_onboarding) },
                        onNavigateToRegister = { navController.navigate(R.id.action_auth_to_register) },
                        onGoogleSignInRequest = { launchGoogleSignIn() }
                    )
                }
            }
        }
    }

    private fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut().addOnCompleteListener {
            val intent: Intent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(intent)
        }
    }
}

interface AuthViewModelProvider {
    val authViewModel: AuthViewModel
}
