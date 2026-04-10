package com.univpm.unirun.ui.sport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.univpm.unirun.ui.theme.KineticTheme

class SportSelectionFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KineticTheme {
                    SportSelectionScreen { sport ->
                        val bundle = Bundle().apply { putString("sportType", sport) }
                        findNavController().navigate(com.univpm.unirun.R.id.action_sport_to_tracking, bundle)
                    }
                }
            }
        }
    }
}
