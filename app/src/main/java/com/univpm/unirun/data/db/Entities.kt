package com.univpm.unirun.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val name: String,
    val weightKg: Float,
    val heightCm: Int
)

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["uid"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GearEntity::class,
            parentColumns = ["id"],
            childColumns = ["gearId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["gearId"])]
)
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val sportType: String,
    val startTimestamp: Long,
    val durationSeconds: Long,
    val distanceMeters: Float,
    val calories: Float,
    val polylineJson: String,
    val gearId: Long? = null
)

@Entity(
    tableName = "gears",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["uid"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class GearEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String,
    val type: String,
    val totalKm: Float = 0f,
    val wearThresholdKm: Float
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val dateEpochDay: Long,
    val mlConsumed: Int
)