package com.univpm.unirun.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.univpm.unirun.data.db.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insert(activity: ActivityEntity): Long

    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY startTimestamp DESC")
    fun observeAllByUser(userId: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getById(id: Long): ActivityEntity?

    @Delete
    suspend fun delete(activity: ActivityEntity)

    // Per aggiornare i km del gear dopo ogni attività
    @Query("SELECT SUM(distanceMeters) FROM activities WHERE gearId = :gearId")
    suspend fun getTotalDistanceForGear(gearId: Long): Float?
}
