package com.univpm.unirun.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.univpm.unirun.data.db.GearEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GearDao {
    @Insert
    suspend fun insert(gear: GearEntity): Long

    @Update
    suspend fun update(gear: GearEntity)

    @Delete
    suspend fun delete(gear: GearEntity)

    @Query("SELECT * FROM gears WHERE userId = :userId")
    fun observeAllByUser(userId: String): Flow<List<GearEntity>>

    @Query("UPDATE gears SET totalKm = totalKm + :km WHERE id = :gearId")
    suspend fun addKm(gearId: Long, km: Float)
}
