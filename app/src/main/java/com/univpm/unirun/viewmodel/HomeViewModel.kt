package com.univpm.unirun.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    // TODO Parte 4: sostituire "local_user" con Firebase Auth UID
    val activities: Flow<List<ActivityEntity>> =
        db.activityDao().observeAllByUser("local_user")
}
