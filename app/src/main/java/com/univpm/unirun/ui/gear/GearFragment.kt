package com.univpm.unirun.ui.gear

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.univpm.unirun.R
import com.univpm.unirun.data.db.GearEntity
import com.univpm.unirun.viewmodel.GearViewModel
import kotlinx.coroutines.launch

class GearFragment : Fragment(R.layout.fragment_gear) {

    private val gearViewModel: GearViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvGear = view.findViewById<RecyclerView>(R.id.rvGear)
        val fabAddGear = view.findViewById<FloatingActionButton>(R.id.fabAddGear)

        val adapter = GearAdapter { gear ->
            AlertDialog.Builder(requireContext())
                .setTitle("Conferma eliminazione")
                .setMessage("Vuoi davvero eliminare ${gear.name}?")
                .setPositiveButton("Elimina") { _, _ ->
                    gearViewModel.deleteGear(gear)
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        rvGear.layoutManager = LinearLayoutManager(requireContext())
        rvGear.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                gearViewModel.gearList.collect { list ->
                    adapter.submitList(list)
                }
            }
        }

        fabAddGear.setOnClickListener {
            showAddGearDialog()
        }
    }

    private fun showAddGearDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_gear, null)
        val etName = dialogView.findViewById<EditText>(R.id.etGearName)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerGearType)
        val etThreshold = dialogView.findViewById<EditText>(R.id.etGearThreshold)

        val types = arrayOf("Scarpe", "Bici")
        spinnerType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)

        AlertDialog.Builder(requireContext())
            .setTitle("Nuova Attrezzatura")
            .setView(dialogView)
            .setPositiveButton("Aggiungi") { _, _ ->
                val name = etName.text.toString().trim()
                val type = spinnerType.selectedItem.toString()
                val limitStr = etThreshold.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    val defaultLimit = if (type == "Scarpe") 500f else 3000f
                    val limit = limitStr.toFloatOrNull() ?: defaultLimit
                    gearViewModel.addGear(name, type, limit)
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}

class GearAdapter(
    private val onDeleteClick: (GearEntity) -> Unit
) : ListAdapter<GearEntity, GearAdapter.GearViewHolder>(GearDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GearViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gear, parent, false)
        return GearViewHolder(view)
    }

    override fun onBindViewHolder(holder: GearViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GearViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvGearName)
        private val tvType: TextView = itemView.findViewById(R.id.tvGearType)
        private val progressWear: ProgressBar = itemView.findViewById(R.id.progressGearWear)
        private val tvKm: TextView = itemView.findViewById(R.id.tvGearKm)
        private val tvAlert: TextView = itemView.findViewById(R.id.tvGearAlert)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteGear)

        fun bind(gear: GearEntity) {
            tvName.text = gear.name
            tvType.text = if (gear.type == "Scarpe") "👟 Scarpe" else "🚴 Bici"
            
            progressWear.max = gear.wearThresholdKm.toInt()
            progressWear.progress = gear.totalKm.toInt()
            
            tvKm.text = "%.1f / %.0f km".format(gear.totalKm, gear.wearThresholdKm)
            
            if (gear.totalKm >= gear.wearThresholdKm * 0.9f) {
                tvAlert.visibility = View.VISIBLE
            } else {
                tvAlert.visibility = View.GONE
            }

            btnDelete.setOnClickListener {
                onDeleteClick(gear)
            }
        }
    }
}

class GearDiffCallback : DiffUtil.ItemCallback<GearEntity>() {
    override fun areItemsTheSame(oldItem: GearEntity, newItem: GearEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GearEntity, newItem: GearEntity): Boolean {
        return oldItem == newItem
    }
}
