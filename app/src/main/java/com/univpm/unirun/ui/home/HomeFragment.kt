package com.univpm.unirun.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
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
        
        val btnProfile = view.findViewById<ImageButton>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }

        val btnLogout = view.findViewById<ImageButton>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_home_to_auth)
        }

        val btnWater = view.findViewById<Button>(R.id.btnWater)
        btnWater.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_water)
        }

        val btnGear = view.findViewById<Button>(R.id.btnGear)
        btnGear.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_gear)
        }

        val adapter = ActivityAdapter { activity ->
            val bundle = Bundle().apply { putLong("activityId", activity.id) }
            findNavController().navigate(R.id.action_home_to_detail, bundle)
        }
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

class ActivityAdapter(
    private val onItemClick: (ActivityEntity) -> Unit
) : ListAdapter<ActivityEntity, ActivityAdapter.VH>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ActivityEntity) {
            itemView.findViewById<TextView>(R.id.tvActivityIcon).text = when(item.sportType) {
                "RUN"  -> "🏃"
                "WALK" -> "🚶"
                "BIKE" -> "🚴"
                else   -> "🏃"
            }
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            itemView.findViewById<TextView>(R.id.tvActivityDate).text =
                sdf.format(Date(item.startTimestamp))

            itemView.findViewById<TextView>(R.id.tvActivityDistance).text =
                if (item.distanceMeters < 1000) "%.0f m".format(item.distanceMeters)
                else "%.2f km".format(item.distanceMeters / 1000f)

            val min = item.durationSeconds / 60
            val sec = item.durationSeconds % 60
            itemView.findViewById<TextView>(R.id.tvActivityDuration).text =
                "%d:%02d".format(min, sec)

            itemView.findViewById<TextView>(R.id.tvActivityCalories).text =
                "%.0f kcal".format(item.calories)

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}

class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityEntity>() {
    override fun areItemsTheSame(o: ActivityEntity, n: ActivityEntity) = o.id == n.id
    override fun areContentsTheSame(o: ActivityEntity, n: ActivityEntity) = o == n
}
