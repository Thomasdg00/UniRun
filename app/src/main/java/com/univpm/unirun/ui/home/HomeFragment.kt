package com.univpm.unirun.ui.home

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
import com.univpm.unirun.R
import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var tvWeeklyKm: TextView
    private lateinit var tvActiveTime: TextView
    private lateinit var tvActiveTimeDelta: TextView
    private lateinit var tvIntensity: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvRecoveryStatus: TextView
    private lateinit var tvViewAll: TextView
    private lateinit var btnStartActivity: CardView
    private lateinit var navRecord: View
    private lateinit var navProfile: View
    private lateinit var rvActivities: RecyclerView

    private val adapter = KineticActivityAdapter { activity ->
        val bundle = Bundle().apply { putLong("activityId", activity.id) }
        findNavController().navigate(R.id.action_home_to_detail, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupRecyclerView()
        setupListeners()
        observeData()
    }

    override fun onDestroyView() {
        rvActivities.adapter = null
        super.onDestroyView()
    }

    private fun bindViews(view: View) {
        tvWeeklyKm = view.findViewById(R.id.tvWeeklyKm)
        tvActiveTime = view.findViewById(R.id.tvActiveTime)
        tvActiveTimeDelta = view.findViewById(R.id.tvActiveTimeDelta)
        tvIntensity = view.findViewById(R.id.tvIntensity)
        tvStreak = view.findViewById(R.id.tvStreak)
        tvRecoveryStatus = view.findViewById(R.id.tvRecoveryStatus)
        tvViewAll = view.findViewById(R.id.tvViewAll)
        btnStartActivity = view.findViewById(R.id.btnStartActivity)
        navRecord = view.findViewById(R.id.navRecord)
        navProfile = view.findViewById(R.id.navProfile)
        rvActivities = view.findViewById(R.id.rvActivities)
    }

    private fun setupRecyclerView() {
        rvActivities.layoutManager = LinearLayoutManager(requireContext())
        rvActivities.adapter = adapter
    }

    private fun setupListeners() {
        btnStartActivity.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_sport)
        }
        navRecord.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_sport)
        }
        navProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
        tvViewAll.setOnClickListener {
            rvActivities.smoothScrollToPosition(0)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activities.collect { activities ->
                    val sortedActivities = activities.sortedByDescending { it.startTimestamp }
                    updateStats(sortedActivities)
                    adapter.submitList(sortedActivities.take(10))
                }
            }
        }
    }

    private fun updateStats(activities: List<ActivityEntity>) {
        val nowMs = System.currentTimeMillis()
        val weekMs = TimeUnit.DAYS.toMillis(7)
        val thisWeek = activities.filter { nowMs - it.startTimestamp <= weekMs }
        val lastWeek = activities.filter {
            val elapsed = nowMs - it.startTimestamp
            elapsed > weekMs && elapsed <= weekMs * 2
        }

        val weeklyKm = thisWeek.sumOf { it.distanceMeters.toDouble() } / 1000.0
        tvWeeklyKm.text = String.format(Locale.getDefault(), "%.1f", weeklyKm)

        val thisWeekSeconds = thisWeek.sumOf { it.durationSeconds }
        val lastWeekSeconds = lastWeek.sumOf { it.durationSeconds }
        tvActiveTime.text = formatHoursMinutes(thisWeekSeconds)
        tvActiveTimeDelta.text = when {
            thisWeekSeconds == 0L && lastWeekSeconds == 0L -> "No activity yet"
            lastWeekSeconds <= 0L -> "First week!"
            else -> {
                val delta = (((thisWeekSeconds - lastWeekSeconds).toDouble() / lastWeekSeconds) * 100).roundToInt()
                String.format(Locale.getDefault(), "%+d%% vs LW", delta)
            }
        }

        tvIntensity.text = thisWeek.sumOf { it.calories.toDouble() }.roundToInt().toString()
        tvStreak.text = "STREAK: ${computeStreak(activities)}D"
        tvRecoveryStatus.text = computeRecoveryStatus(activities.firstOrNull(), nowMs)
    }

    private fun computeStreak(activities: List<ActivityEntity>): Int {
        if (activities.isEmpty()) return 0

        val activeEpochDays = activities.map { epochDayFromTimestamp(it.startTimestamp) }.toSet()
        var streak = 0
        var currentDay = epochDayFromTimestamp(System.currentTimeMillis())

        while (activeEpochDays.contains(currentDay)) {
            streak++
            currentDay--
        }

        return streak
    }

    private fun computeRecoveryStatus(lastActivity: ActivityEntity?, nowMs: Long): String {
        if (lastActivity == null) return "READY TO PUSH"

        val hoursSinceLast = TimeUnit.MILLISECONDS.toHours(nowMs - lastActivity.startTimestamp)
        return when {
            hoursSinceLast >= 36 -> "READY TO PUSH"
            hoursSinceLast >= 18 -> "BALANCED LOAD"
            else -> "RECOVERING NOW"
        }
    }

    private fun epochDayFromTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis)
    }

    private fun formatHoursMinutes(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }
}

private class KineticActivityAdapter(
    private val onItemClick: (ActivityEntity) -> Unit
) : ListAdapter<ActivityEntity, KineticActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ActivityViewHolder(
        itemView: View,
        private val onItemClick: (ActivityEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvActivityDate: TextView = itemView.findViewById(R.id.tvActivityDate)
        private val tvActivityName: TextView = itemView.findViewById(R.id.tvActivityName)
        private val tvActivityDistance: TextView = itemView.findViewById(R.id.tvActivityDistance)
        private val tvDistanceUnit: TextView = itemView.findViewById(R.id.tvDistanceUnit)
        private val tvActivityDuration: TextView = itemView.findViewById(R.id.tvActivityDuration)
        private val tvActivityPace: TextView = itemView.findViewById(R.id.tvActivityPace)

        fun bind(item: ActivityEntity) {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val calendar = Calendar.getInstance().apply { timeInMillis = item.startTimestamp }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val timeOfDay = when {
                hour < 12 -> "Morning"
                hour < 18 -> "Afternoon"
                else -> "Evening"
            }
            val sport = item.sportType.lowercase(Locale.getDefault()).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }

            tvActivityDate.text = "${dateFormat.format(Date(item.startTimestamp)).uppercase(Locale.getDefault())} · ${sport.uppercase(Locale.getDefault())}"
            tvActivityName.text = "$timeOfDay $sport"

            if (item.distanceMeters >= 1000f) {
                tvActivityDistance.text = String.format(Locale.getDefault(), "%.1f", item.distanceMeters / 1000f)
                tvDistanceUnit.text = "KM"
            } else {
                tvActivityDistance.text = item.distanceMeters.roundToInt().toString()
                tvDistanceUnit.text = "M"
            }

            tvActivityDuration.text = formatDuration(item.durationSeconds)
            tvActivityPace.text = formatPace(item.distanceMeters, item.durationSeconds)
            itemView.setOnClickListener { onItemClick(item) }
        }

        private fun formatDuration(totalSeconds: Long): String {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

        private fun formatPace(distanceMeters: Float, durationSeconds: Long): String {
            if (distanceMeters <= 0f || durationSeconds <= 0L) return "--:--"

            val secondsPerKm = durationSeconds / (distanceMeters / 1000f)
            if (!secondsPerKm.isFinite()) return "--:--"

            val totalSeconds = secondsPerKm.roundToInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        }
    }
}

private class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityEntity>() {
    override fun areItemsTheSame(oldItem: ActivityEntity, newItem: ActivityEntity): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ActivityEntity, newItem: ActivityEntity): Boolean = oldItem == newItem
}
