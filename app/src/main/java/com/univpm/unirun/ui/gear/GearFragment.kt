package com.univpm.unirun.ui.gear

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.univpm.unirun.ui.theme.KineticTheme

class GearFragment : Fragment() {

    private val gearViewModel: com.univpm.unirun.viewmodel.GearViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KineticTheme {
                    GearRoute(gearViewModel = gearViewModel)
                }
            }
        }
    }
}
