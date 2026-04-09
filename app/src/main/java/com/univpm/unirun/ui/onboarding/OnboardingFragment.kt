package com.univpm.unirun.ui.onboarding

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.univpm.unirun.R
import com.univpm.unirun.ui.auth.AuthViewModelProvider
import com.univpm.unirun.viewmodel.AuthState
import com.univpm.unirun.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Fragment that collects user profile information after registration.
 * User must enter: name, weight (kg), height (cm).
 * Only then can proceed to Home.
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    // ViewModel will be provided by MainActivity/Activity-level factory
    // For now, we assume it's accessible via requireActivity()
    // In production, use hilt or other DI for proper ViewModelFactory
    private val authViewModel: AuthViewModel by lazy {
        (requireActivity() as? AuthViewModelProvider)?.authViewModel
            ?: throw IllegalStateException("Activity must implement AuthViewModelProvider")
    }

    private lateinit var etName: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var btnContinue: Button
    private lateinit var progressOnboarding: ProgressBar
    private lateinit var tvErrorMessage: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        etName = view.findViewById(R.id.etName)
        etWeight = view.findViewById(R.id.etWeight)
        etHeight = view.findViewById(R.id.etHeight)
        btnContinue = view.findViewById(R.id.btnContinue)
        progressOnboarding = view.findViewById(R.id.progressOnboarding)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)

        // Set up continue button click
        btnContinue.setOnClickListener { handleContinue() }

        // Observe auth state to handle success/error
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        progressOnboarding.visibility = View.GONE
                        btnContinue.isEnabled = true
                        // Navigate to Home after successful onboarding
                        findNavController().navigate(R.id.action_onboarding_to_home)
                    }
                    is AuthState.Error -> {
                        progressOnboarding.visibility = View.GONE
                        btnContinue.isEnabled = true
                        tvErrorMessage.text = state.message
                        tvErrorMessage.visibility = View.VISIBLE
                    }
                    AuthState.Loading -> {
                        progressOnboarding.visibility = View.VISIBLE
                        btnContinue.isEnabled = false
                    }
                    else -> {} // Other states not relevant for onboarding
                }
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    progressOnboarding.visibility = View.VISIBLE
                    btnContinue.isEnabled = false
                } else {
                    progressOnboarding.visibility = View.GONE
                    btnContinue.isEnabled = true
                }
            }
        }
    }

    /**
     * Validates form input and calls ViewModel to save profile.
     */
    private fun handleContinue() {
        val name = etName.text.toString().trim()
        val weightText = etWeight.text.toString().trim()
        val heightText = etHeight.text.toString().trim()

        // Validate inputs
        val error = validateInput(name, weightText, heightText)
        if (error != null) {
            tvErrorMessage.text = error
            tvErrorMessage.visibility = View.VISIBLE
            Snackbar.make(requireView(), error, Snackbar.LENGTH_SHORT).show()
            return
        }

        tvErrorMessage.visibility = View.GONE

        val weight = weightText.toFloatOrNull() ?: 70f
        val height = heightText.toIntOrNull() ?: 170

        // Call ViewModel to save profile
        authViewModel.completeOnboarding(name, weight, height)
    }

    /**
     * Validates all form fields.
     * Returns error message if validation fails, null if valid.
     */
    private fun validateInput(name: String, weightText: String, heightText: String): String? {
        return when {
            name.isEmpty() -> "Inserisci il tuo nome"
            name.length < 2 -> "Nome deve essere almeno 2 caratteri"
            weightText.isEmpty() -> "Inserisci il tuo peso (kg)"
            heightText.isEmpty() -> "Inserisci la tua altezza (cm)"
            else -> {
                val weight = weightText.toFloatOrNull()
                val height = heightText.toIntOrNull()
                when {
                    weight == null -> "Peso non valido (usa numeri decimali)"
                    height == null -> "Altezza non valida (usa numeri interi)"
                    weight < 30f || weight > 300f -> "Peso deve essere tra 30 e 300 kg"
                    height < 100 || height > 250 -> "Altezza deve essere tra 100 e 250 cm"
                    else -> null
                }
            }
        }
    }
}
