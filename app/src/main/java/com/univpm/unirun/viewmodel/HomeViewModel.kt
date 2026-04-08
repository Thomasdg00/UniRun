package com.univpm.unirun.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
    val activities: Flow<List<ActivityEntity>> = db.activityDao().observeAllByUser(uid)
}