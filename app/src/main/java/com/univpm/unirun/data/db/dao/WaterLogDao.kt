package com.univpm.unirun.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.univpm.unirun.data.db.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: WaterLogEntity)

    // Legge il log del giorno corrente
    @Query("SELECT * FROM water_logs WHERE userId = :userId AND dateEpochDay = :day")
    fun observeForDay(userId: String, day: Long): Flow<WaterLogEntity?>

    // Per il reset automatico: recupera tutti i giorni vecchi
    @Query("DELETE FROM water_logs WHERE userId = :userId AND dateEpochDay < :today")
    suspend fun deleteOldLogs(userId: String, today: Long)
}
