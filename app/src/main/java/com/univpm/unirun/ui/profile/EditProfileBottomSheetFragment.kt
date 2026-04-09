package com.univpm.unirun.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.univpm.unirun.R
import com.univpm.unirun.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditProfileBottomSheetFragment : BottomSheetDialogFragment(R.layout.bottom_sheet_edit_profile) {

    private lateinit var prefsRepo: UserPreferencesRepository
    private lateinit var etName: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var btnSaveProfile: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsRepo = UserPreferencesRepository(requireContext())
        etName = view.findViewById(R.id.etName)
        etWeight = view.findViewById(R.id.etWeight)
        etHeight = view.findViewById(R.id.etHeight)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                prefsRepo.userPreferencesFlow.collectLatest { prefs ->
                    if (etName.text.isEmpty()) etName.setText(prefs.name)
                    if (etWeight.text.isEmpty()) etWeight.setText(prefs.weightKg.toString())
                    if (etHeight.text.isEmpty()) etHeight.setText(prefs.heightCm.toString())
                }
            }
        }

        btnSaveProfile.setOnClickListener {
            val name = etName.text.toString().trim()
            val weight = etWeight.text.toString().trim().toFloatOrNull()
            val height = etHeight.text.toString().trim().toIntOrNull()

            when {
                name.isEmpty() -> showError("Il nome non può essere vuoto")
                weight == null || weight < 30f || weight > 300f -> showError("Inserire un peso valido (30-300 kg)")
                height == null || height < 100 || height > 250 -> showError("Inserire un'altezza valida (100-250 cm)")
                else -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        prefsRepo.saveProfile(name, weight, height)
                        Snackbar.make(requireParentFragment().requireView(), "Profilo salvato", Snackbar.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
}
