package com.univpm.unirun.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.univpm.unirun.R
import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val fab: FloatingActionButton = view.findViewById(R.id.fabNewActivity)
        val rv: RecyclerView = view.findViewById(R.id.rvActivities)

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_sport)
        }

        val adapter = ActivityAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activities.collect { list ->
                    adapter.submitList(list)
                }
            }
        }
        return view
    }
}

class ActivityAdapter : RecyclerView.Adapter<ActivityAdapter.VH>() {
    private var items: List<ActivityEntity> = emptyList()

    fun submitList(list: List<ActivityEntity>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            setPadding(32, 20, 32, 20)
            textSize = 14f
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = sdf.format(Date(item.startTimestamp))
        val km = "%.2f km".format(item.distanceMeters / 1000f)
        val min = item.durationSeconds / 60
        (holder.itemView as TextView).text = "$date — $km — ${min}min"
    }

    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v)
}
