package com.univpm.unirun.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.univpm.unirun.R
import com.univpm.unirun.viewmodel.HistoryItem
import com.univpm.unirun.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var ivProfileAvatar: ShapeableImageView
    private lateinit var tvProfileLevel: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileLocation: TextView
    private lateinit var tvProfileJoined: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalActivities: TextView
    private lateinit var tvAvgPace: TextView
    private lateinit var tvTotalCalories: TextView
    private lateinit var tvMonthLabel: TextView
    private lateinit var tvMonthKm: TextView
    private lateinit var tvMonthTime: TextView
    private lateinit var rvActivityHistory: RecyclerView
    private lateinit var btnEditProfile: CardView
    private lateinit var navHomeProfile: View
    private lateinit var navRecordProfile: View
    private lateinit var navProfileTab: View

    private val historyAdapter = HistoryAdapter { activityId ->
        val bundle = Bundle().apply { putLong("activityId", activityId) }
        findNavController().navigate(R.id.activityDetailFragment, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        rvActivityHistory.adapter = null
        super.onDestroyView()
    }

    private fun bindViews(view: View) {
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar)
        tvProfileLevel = view.findViewById(R.id.tvProfileLevel)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileLocation = view.findViewById(R.id.tvProfileLocation)
        tvProfileJoined = view.findViewById(R.id.tvProfileJoined)
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance)
        tvTotalActivities = view.findViewById(R.id.tvTotalActivities)
        tvAvgPace = view.findViewById(R.id.tvAvgPace)
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories)
        tvMonthLabel = view.findViewById(R.id.tvMonthLabel)
        tvMonthKm = view.findViewById(R.id.tvMonthKm)
        tvMonthTime = view.findViewById(R.id.tvMonthTime)
        rvActivityHistory = view.findViewById(R.id.rvActivityHistory)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        navHomeProfile = view.findViewById(R.id.navHomeProfile)
        navRecordProfile = view.findViewById(R.id.navRecordProfile)
        navProfileTab = view.findViewById(R.id.navProfileTab)
    }

    private fun setupRecyclerView() {
        rvActivityHistory.layoutManager = LinearLayoutManager(requireContext())
        rvActivityHistory.adapter = historyAdapter
    }

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            EditProfileBottomSheetFragment().show(childFragmentManager, "edit_profile")
        }
        navHomeProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_home)
        }
        navRecordProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_sport)
        }
        navProfileTab.setOnClickListener { }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userName.collect { tvProfileName.text = it.uppercase(Locale.getDefault()) }
                }
                launch {
                    viewModel.totalDistanceKm.collect {
                        tvTotalDistance.text = String.format(Locale.getDefault(), "%.1f", it)
                    }
                }
                launch {
                    viewModel.totalActivities.collect { tvTotalActivities.text = it.toString() }
                }
                launch {
                    viewModel.avgPaceFormatted.collect { tvAvgPace.text = it }
                }
                launch {
                    viewModel.totalCalories.collect { tvTotalCalories.text = it.toString() }
                }
                launch {
                    viewModel.currentMonthLabel.collect { tvMonthLabel.text = it }
                }
                launch {
                    viewModel.monthlyKm.collect {
                        tvMonthKm.text = String.format(Locale.getDefault(), "%.1f KM", it)
                    }
                }
                launch {
                    viewModel.monthlyTimeFormatted.collect { tvMonthTime.text = it }
                }
                launch {
                    viewModel.groupedHistory.collect { historyAdapter.submitList(it) }
                }
                launch {
                    viewModel.joinedLabel.collect { tvProfileJoined.text = it }
                }
                launch {
                    viewModel.locationLabel.collect { tvProfileLocation.text = it }
                }
            }
        }
    }
}

private class HistoryAdapter(
    private val onActivityClick: (activityId: Long) -> Unit
) : ListAdapter<HistoryItem, RecyclerView.ViewHolder>(HistoryDiffCallback()) {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ACTIVITY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HistoryItem.MonthHeader -> VIEW_TYPE_HEADER
            is HistoryItem.ActivityRow -> VIEW_TYPE_ACTIVITY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> MonthHeaderViewHolder(
                inflater.inflate(R.layout.item_history_month_header, parent, false)
            )
            else -> ActivityRowViewHolder(
                inflater.inflate(R.layout.item_activity_history, parent, false),
                onActivityClick
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HistoryItem.MonthHeader -> (holder as MonthHeaderViewHolder).bind(item)
            is HistoryItem.ActivityRow -> (holder as ActivityRowViewHolder).bind(item)
        }
    }

    private class MonthHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSeparatorMonth: TextView = itemView.findViewById(R.id.tvSeparatorMonth)
        private val tvSeparatorKm: TextView = itemView.findViewById(R.id.tvSeparatorKm)

        fun bind(item: HistoryItem.MonthHeader) {
            itemView.alpha = if (item.isCurrentMonth) 1f else 0.6f
            tvSeparatorMonth.text = item.label
            tvSeparatorKm.text = String.format(Locale.getDefault(), "%.1f KM", item.totalKm)
        }
    }

    private class ActivityRowViewHolder(
        itemView: View,
        private val onActivityClick: (activityId: Long) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvHistoryDay: TextView = itemView.findViewById(R.id.tvHistoryDay)
        private val tvHistoryMonth: TextView = itemView.findViewById(R.id.tvHistoryMonth)
        private val tvHistoryType: TextView = itemView.findViewById(R.id.tvHistoryType)
        private val tvHistorySubtitle: TextView = itemView.findViewById(R.id.tvHistorySubtitle)
        private val tvHistoryTitle: TextView = itemView.findViewById(R.id.tvHistoryTitle)
        private val tvHistoryKm: TextView = itemView.findViewById(R.id.tvHistoryKm)
        private val tvHistoryTime: TextView = itemView.findViewById(R.id.tvHistoryTime)

        fun bind(item: HistoryItem.ActivityRow) {
            itemView.alpha = if (item.isCurrentMonth) 1f else 0.6f
            tvHistoryDay.text = item.dayOfMonth.toString()
            tvHistoryMonth.text = item.monthAbbr
            tvHistoryType.text = item.sportType.uppercase(Locale.getDefault())
            tvHistorySubtitle.text = item.timeOfDay.uppercase(Locale.getDefault())
            tvHistoryTitle.text = item.titleName.uppercase(Locale.getDefault())
            tvHistoryKm.text = String.format(Locale.getDefault(), "%.1f", item.distanceKm)
            tvHistoryTime.text = item.durationFormatted
            itemView.setOnClickListener { onActivityClick(item.id) }
        }
    }
}

private class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
    override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return when {
            oldItem is HistoryItem.MonthHeader && newItem is HistoryItem.MonthHeader -> oldItem.label == newItem.label
            oldItem is HistoryItem.ActivityRow && newItem is HistoryItem.ActivityRow -> oldItem.id == newItem.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean = oldItem == newItem
}
