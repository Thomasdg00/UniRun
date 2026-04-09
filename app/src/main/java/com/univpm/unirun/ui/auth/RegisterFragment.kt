package com.univpm.unirun.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.univpm.unirun.R
import com.univpm.unirun.viewmodel.AuthState
import com.univpm.unirun.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val authViewModel: AuthViewModel by lazy {
        (requireActivity() as? AuthViewModelProvider)?.authViewModel
            ?: throw IllegalStateException("Activity must implement AuthViewModelProvider")
    }
    
    // View references
    private lateinit var etRegisterName: EditText
    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText
    private lateinit var etRegisterConfirmPassword: EditText
    private lateinit var checkTerms: CheckBox
    private lateinit var btnRegister: Button
    private lateinit var btnRegisterGoogleSignIn: Button
    private lateinit var tvGoToLogin: TextView
    private lateinit var progressRegister: ProgressBar
    private lateinit var tvRegisterError: TextView

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(Exception::class.java)
            if (account != null) {
                authViewModel.signInWithGoogle(account)
            }
        } catch (e: Exception) {
            showError("Errore Google Sign-In: ${e.localizedMessage}")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize view references
        etRegisterName = view.findViewById(R.id.etRegisterName)
        etRegisterEmail = view.findViewById(R.id.etRegisterEmail)
        etRegisterPassword = view.findViewById(R.id.etRegisterPassword)
        etRegisterConfirmPassword = view.findViewById(R.id.etRegisterConfirmPassword)
        checkTerms = view.findViewById(R.id.checkTerms)
        btnRegister = view.findViewById(R.id.btnRegister)
        btnRegisterGoogleSignIn = view.findViewById(R.id.btnRegisterGoogleSignIn)
        tvGoToLogin = view.findViewById(R.id.tvGoToLogin)
        progressRegister = view.findViewById(R.id.progressRegister)
        tvRegisterError = view.findViewById(R.id.tvRegisterError)
        
        // Wire up click listeners
        btnRegister.setOnClickListener { handleRegister() }
        btnRegisterGoogleSignIn.setOnClickListener { handleGoogleSignIn() }
        tvGoToLogin.setOnClickListener { findNavController().navigateUp() }
        
        // Observe authentication state
        observeAuthState()
        observeLoadingState()
    }

    private fun handleRegister() {
        val name = etRegisterName.text.toString().trim()
        val email = etRegisterEmail.text.toString().trim()
        val password = etRegisterPassword.text.toString().trim()
        val confirmPassword = etRegisterConfirmPassword.text.toString().trim()
        
        // Local validation
        when {
            name.isEmpty() -> {
                showError("Please enter your full name")
                return
            }
            email.isEmpty() -> {
                showError("Please enter your email address")
                return
            }
            password.isEmpty() -> {
                showError("Please enter a password")
                return
            }
            password != confirmPassword -> {
                showError("Passwords do not match")
                return
            }
            !checkTerms.isChecked -> {
                showError("You must agree to the Terms & Conditions")
                return
            }
        }
        
        // Call ViewModel to perform registration
        authViewModel.signUpWithEmail(email, password)
    }

    private fun observeAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        showProgress()
                    }
                    is AuthState.OnboardingNeeded -> {
                        // Navigate to onboarding after successful registration
                        hideProgress()
                        findNavController().navigate(R.id.action_register_to_onboarding)
                    }
                    is AuthState.Authenticated -> {
                        // Also handle the case where they're already authenticated (shouldn't happen on register)
                        hideProgress()
                        findNavController().navigate(R.id.action_register_to_onboarding)
                    }
                    is AuthState.Error -> {
                        hideProgress()
                        showError(state.message)
                        authViewModel.clearError()
                    }
                    else -> {
                        hideProgress()
                    }
                }
            }
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    showProgress()
                } else {
                    hideProgress()
                }
            }
        }
    }

    private fun showProgress() {
        progressRegister.visibility = View.VISIBLE
        btnRegister.isEnabled = false
        btnRegisterGoogleSignIn.isEnabled = false
    }

    private fun hideProgress() {
        progressRegister.visibility = View.GONE
        btnRegister.isEnabled = true
        btnRegisterGoogleSignIn.isEnabled = true
    }

    private fun handleGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun showError(message: String) {
        tvRegisterError.text = message
        tvRegisterError.visibility = View.VISIBLE
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
}
