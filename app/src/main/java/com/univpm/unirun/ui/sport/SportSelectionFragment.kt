package com.univpm.unirun.ui.sport

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.univpm.unirun.R

class SportSelectionFragment : Fragment(R.layout.fragment_sport_selection) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnRun = view.findViewById<Button>(R.id.btnRun)
        val btnWalk = view.findViewById<Button>(R.id.btnWalk)
        val btnBike = view.findViewById<Button>(R.id.btnBike)

        btnRun.setOnClickListener { navigateToTracking("RUN") }
        btnWalk.setOnClickListener { navigateToTracking("WALK") }
        btnBike.setOnClickListener { navigateToTracking("BIKE") }
    }

    private fun navigateToTracking(sportType: String) {
        val bundle = Bundle().apply { putString("sportType", sportType) }
        findNavController().navigate(R.id.action_sport_to_tracking, bundle)
    }
}
