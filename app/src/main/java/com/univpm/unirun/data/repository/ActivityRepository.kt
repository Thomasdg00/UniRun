package com.univpm.unirun.data.repository

import com.univpm.unirun.data.db.ActivityEntity
import com.univpm.unirun.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class ActivityRepository(private val db: AppDatabase) {

    suspend fun saveActivity(
        userId: String,
        sportType: String,
        durationSeconds: Long,
        distanceMeters: Float,
        pathPoints: List<LatLng>,
        weightKg: Float,
        gearId: Long? = null
    ): Long {
        val met = when (sportType.uppercase()) {
            "RUN" -> 9.8f
            "WALK" -> 3.5f
            "BIKE" -> 7.5f
            else -> 7.5f
        }

        val kcal = met * weightKg * (durationSeconds / 3600f)

        val jsonArray = JSONArray()
        for (point in pathPoints) {
            val p = JSONObject()
            p.put("lat", point.lat)
            p.put("lng", point.lon)
            jsonArray.put(p)
        }
        val polylineJson = jsonArray.toString()

        val activity = ActivityEntity(
            userId = userId,
            sportType = sportType,
            startTimestamp = System.currentTimeMillis() - (durationSeconds * 1000),
            durationSeconds = durationSeconds,
            distanceMeters = distanceMeters,
            calories = kcal,
            polylineJson = polylineJson,
            gearId = gearId
        )

        return db.activityDao().insert(activity)
    }

    fun observeActivities(userId: String): Flow<List<ActivityEntity>> {
        return db.activityDao().observeAllByUser(userId)
    }

    suspend fun getActivity(id: Long): ActivityEntity? {
        return db.activityDao().getById(id)
    }

    suspend fun deleteActivity(activity: ActivityEntity) {
        db.activityDao().delete(activity)
    }
}
