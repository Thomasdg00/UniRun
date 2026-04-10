package com.univpm.unirun.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.ui.theme.KineticTheme

class ActivityDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activityId = arguments?.getLong("activityId") ?: return ComposeView(requireContext())
        
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KineticTheme {
                    ActivityDetailRoute(
                        activityId = activityId,
                        appDatabase = AppDatabase.getInstance(requireContext()),
                        navController = findNavController()
                    )
                }
            }
        }
    }
}