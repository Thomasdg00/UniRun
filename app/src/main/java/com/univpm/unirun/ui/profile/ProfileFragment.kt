package com.univpm.unirun.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.univpm.unirun.R
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var etName: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var tvProfileInfo: TextView

    private lateinit var prefsRepo: UserPreferencesRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etName = view.findViewById(R.id.etName)
        etWeight = view.findViewById(R.id.etWeight)
        etHeight = view.findViewById(R.id.etHeight)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)
        tvProfileInfo = view.findViewById(R.id.tvProfileInfo)

        prefsRepo = UserPreferencesRepository(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                prefsRepo.userPreferencesFlow.collectLatest { prefs ->
                    if (etName.text.isEmpty() && prefs.name.isNotEmpty()) {
                        etName.setText(prefs.name)
                    }
                    if (etWeight.text.isEmpty()) {
                        etWeight.setText(prefs.weightKg.toString())
                    }
                    if (etHeight.text.isEmpty()) {
                        etHeight.setText(prefs.heightCm.toString())
                    }
                    
                    tvProfileInfo.text = "Nome: ${prefs.name} | Peso: ${prefs.weightKg} kg | Altezza: ${prefs.heightCm} cm"
                }
            }
        }

        btnSaveProfile.setOnClickListener {
            val name = etName.text.toString().trim()
            val weightStr = etWeight.text.toString().trim()
            val heightStr = etHeight.text.toString().trim()

            if (name.isEmpty()) {
                showError("Il nome non può essere vuoto")
                return@setOnClickListener
            }

            val weight = weightStr.toFloatOrNull()
            if (weight == null || weight < 30f || weight > 300f) {
                showError("Inserire un peso valido (30-300 kg)")
                return@setOnClickListener
            }

            val height = heightStr.toIntOrNull()
            if (height == null || height < 100 || height > 250) {
                showError("Inserire un'altezza valida (100-250 cm)")
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                prefsRepo.saveProfile(name, weight, height)
                Snackbar.make(requireView(), "Profilo salvato", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
}
