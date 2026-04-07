package com.univpm.unirun.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.univpm.unirun.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val fabNewActivity: FloatingActionButton = view.findViewById(R.id.fabNewActivity)
        val rvActivities: RecyclerView = view.findViewById(R.id.rvActivities)

        fabNewActivity.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_tracking)
        }

        return view
    }
}
