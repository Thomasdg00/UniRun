package com.univpm.unirun.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.univpm.unirun.ui.theme.KineticTheme
import com.univpm.unirun.ui.onboarding.OnboardingRoute
import com.univpm.unirun.ui.auth.AuthViewModelProvider

class OnboardingFragment : Fragment() {

    private val authViewModel by lazy {
        (requireActivity() as? AuthViewModelProvider)?.authViewModel
            ?: throw IllegalStateException("Activity must implement AuthViewModelProvider")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val navController = findNavController()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KineticTheme {
                    OnboardingRoute(viewModel = authViewModel) {
                        val bundle = Bundle().apply { putString("userName", "") }
                        try {
                            navController.navigate(com.univpm.unirun.R.id.action_onboarding_to_home, bundle)
                        } catch (_: Exception) { }
                    }
                }
            }
        }
    }
}
