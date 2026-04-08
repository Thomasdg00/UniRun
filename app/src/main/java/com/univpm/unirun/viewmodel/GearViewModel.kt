package com.univpm.unirun.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.univpm.unirun.data.db.AppDatabase
import com.univpm.unirun.data.db.GearEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GearViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"

    val gearList: Flow<List<GearEntity>> = db.gearDao().observeAllByUser(uid)

    fun addGear(name: String, type: String, thresholdKm: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            db.gearDao().insert(
                GearEntity(
                    userId = uid,
                    name = name,
                    type = type,
                    totalKm = 0f,
                    wearThresholdKm = thresholdKm
                )
            )
        }
    }

    fun deleteGear(gear: GearEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.gearDao().delete(gear)
        }
    }

    fun addKmToGear(gearId: Long, km: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            db.gearDao().addKm(gearId, km)
        }
    }
}
