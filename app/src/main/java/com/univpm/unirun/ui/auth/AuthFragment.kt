package com.univpm.unirun.ui.auth

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.univpm.unirun.R
import com.univpm.unirun.viewmodel.AuthState
import com.univpm.unirun.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Authentication Fragment handling email/password and Google Sign-In.
 * Observes AuthViewModel state to manage navigation flow.
 */
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private val authViewModel: AuthViewModel by lazy {
        (requireActivity() as? AuthViewModelProvider)?.authViewModel
            ?: throw IllegalStateException("Activity must implement AuthViewModelProvider")
    }

    // View references
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToRegister: TextView
    private lateinit var btnGoogleSignIn: Button
    private lateinit var btnForgotPassword: TextView
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var tvAuthError: TextView
    private lateinit var progressAuth: ProgressBar
    private var passwordVisible = false

    // Google Sign-In launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(Exception::class.java)
            if (account != null) {
                authViewModel.signInWithGoogle(account)
            }
        } catch (e: Exception) {
            showError("Errore Google Sign-In: ${e.localizedMessage}")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if user is already authenticated - if yes, skip to home/onboarding
        if (authViewModel.isUserAuthenticated()) {
            navigateBasedOnAuthState()
            return
        }

        // Initialize views
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        tvGoToRegister = view.findViewById(R.id.tvGoToRegister)
        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn)
        btnForgotPassword = view.findViewById(R.id.btnForgotPassword)
        btnTogglePassword = view.findViewById(R.id.btnTogglePassword)
        tvAuthError = view.findViewById(R.id.tvAuthError)
        progressAuth = view.findViewById(R.id.progressAuth)

        // Set up click listeners
        btnLogin.setOnClickListener { handleLogin() }
        tvGoToRegister.setOnClickListener { findNavController().navigate(R.id.action_auth_to_register) }
        btnGoogleSignIn.setOnClickListener { handleGoogleSignIn() }
        btnForgotPassword.setOnClickListener { handleForgotPassword() }
        btnTogglePassword.setOnClickListener { togglePasswordVisibility() }

        // Observe auth state
        observeAuthState()
        observeLoadingState()
    }

    /**
     * Handles email/password login.
     */
    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        authViewModel.signInWithEmail(email, password)
    }

    /**
     * Initiates Google Sign-In flow.
     */
    private fun handleGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        // Forza il sign-out dal client Google per mostrare sempre il selettore account
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    /**
     * Shows password reset dialog.
     */
    private fun handleForgotPassword() {
        val emailEditText = EditText(requireContext()).apply {
            hint = "Inserisci la tua email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Password dimenticata?")
            .setMessage("Inserisci la tua email per ricevere un link di reset.")
            .setView(emailEditText)
            .setPositiveButton("Invia") { dialog, _ ->
                val email = emailEditText.text.toString().trim()
                if (email.isNotEmpty()) {
                    authViewModel.resetPassword(email)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annulla") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Observes AuthViewModel's authState and navigates based on result.
     */
    private fun observeAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        progressAuth.visibility = View.GONE
                        enableButtons(true)
                        tvAuthError.visibility = View.GONE
                        findNavController().navigate(R.id.action_auth_to_home)
                    }
                    is AuthState.OnboardingNeeded -> {
                        progressAuth.visibility = View.GONE
                        enableButtons(true)
                        tvAuthError.visibility = View.GONE
                        findNavController().navigate(R.id.action_auth_to_onboarding)
                    }
                    is AuthState.Error -> {
                        progressAuth.visibility = View.GONE
                        enableButtons(true)
                        showError(state.message)
                        authViewModel.clearError()
                    }
                    AuthState.Loading -> {
                        progressAuth.visibility = View.VISIBLE
                        enableButtons(false)
                        tvAuthError.visibility = View.GONE
                    }
                    AuthState.Unauthenticated -> {
                        // Neutral state, do nothing
                    }
                }
            }
        }
    }

    /**
     * Observes loading state changes.
     */
    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    progressAuth.visibility = View.VISIBLE
                    enableButtons(false)
                } else {
                    progressAuth.visibility = View.GONE
                    enableButtons(true)
                }
            }
        }
    }

    /**
     * Navigates based on current auth state.
     * Used on fragment start if user is already authenticated.
     */
    private fun navigateBasedOnAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = authViewModel.authState.first { 
                it is AuthState.Authenticated || it is AuthState.OnboardingNeeded 
            }
            when (state) {
                is AuthState.Authenticated -> findNavController().navigate(R.id.action_auth_to_home)
                is AuthState.OnboardingNeeded -> findNavController().navigate(R.id.action_auth_to_onboarding)
                else -> {}
            }
        }
    }

    /**
     * Enables/disables all auth buttons.
     */
    private fun enableButtons(enabled: Boolean) {
        btnLogin.isEnabled = enabled
        tvGoToRegister.isEnabled = enabled
        btnGoogleSignIn.isEnabled = enabled
        btnForgotPassword.isEnabled = enabled
    }

    /**
     * Toggles password field visibility between plain text and dots.
     */
    private fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
        etPassword.inputType = if (passwordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        etPassword.setSelection(etPassword.text.length)
    }

    /**
     * Shows error message to user.
     */
    private fun showError(message: String) {
        tvAuthError.text = message
        tvAuthError.visibility = View.VISIBLE
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
}

/**
 * Interface that Activity must implement to provide AuthViewModel.
 */
interface AuthViewModelProvider {
    val authViewModel: AuthViewModel
}
