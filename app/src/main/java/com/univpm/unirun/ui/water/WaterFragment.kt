package com.univpm.unirun.ui.water

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.univpm.unirun.R
import com.univpm.unirun.viewmodel.WaterViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WaterFragment : Fragment(R.layout.fragment_water) {

    private val waterViewModel: WaterViewModel by viewModels()

    private lateinit var tvWaterGoal: TextView
    private lateinit var tvWaterConsumed: TextView
    private lateinit var progressWater: ProgressBar
    private lateinit var tvWaterPercentage: TextView
    private lateinit var btnAddWater: Button
    private lateinit var btnRemoveWater: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvWaterGoal = view.findViewById(R.id.tvWaterGoal)
        tvWaterConsumed = view.findViewById(R.id.tvWaterConsumed)
        progressWater = view.findViewById(R.id.progressWater)
        tvWaterPercentage = view.findViewById(R.id.tvWaterPercentage)
        btnAddWater = view.findViewById(R.id.btnAddWater)
        btnRemoveWater = view.findViewById(R.id.btnRemoveWater)

        btnAddWater.setOnClickListener {
            waterViewModel.addWater(250)
        }

        btnRemoveWater.setOnClickListener {
            waterViewModel.removeWater(250)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    waterViewModel.consumedMl,
                    waterViewModel.dailyGoalMl
                ) { consumed, goal -> Pair(consumed, goal) }
                .collect { (consumed, goal) ->
                    updateWaterUI(consumed, goal)
                }
            }
        }
    }

    private fun updateWaterUI(consumed: Int, goal: Int) {
        tvWaterGoal.text = "Obiettivo: $goal ml"
        tvWaterConsumed.text = "$consumed ml"

        progressWater.max = goal
        progressWater.progress = consumed

        val percentage = if (goal > 0) (consumed * 100 / goal).coerceAtMost(100) else 0
        tvWaterPercentage.text = "$percentage%"

        if (consumed >= goal) {
            tvWaterConsumed.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            val typedValue = android.util.TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
            tvWaterConsumed.setTextColor(typedValue.data)
        }
    }
}
