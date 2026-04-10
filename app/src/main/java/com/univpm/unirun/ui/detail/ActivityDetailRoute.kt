package com.univpm.unirun.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ActivityDetailRoute(
    activityId: Long,
    appDatabase: AppDatabase,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var activity by remember { mutableStateOf<ActivityEntity?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    LaunchedEffect(activityId) {
        activity = withContext(Dispatchers.IO) {
            appDatabase.activityDao().getById(activityId)
        }
    }

    if (isDeleting && activity != null) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                appDatabase.activityDao().delete(activity!!)
            }
            navController.navigateUp()
        }
    }

    ActivityDetailScreen(
        activity = activity,
        onDeleteClick = {
            isDeleting = true
        },
        onBackClick = {
            navController.navigateUp()
        },
        modifier = modifier
    )
}
