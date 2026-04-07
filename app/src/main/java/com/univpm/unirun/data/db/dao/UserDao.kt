package com.univpm.unirun.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.univpm.unirun.data.db.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getById(uid: String): UserEntity?

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun observeById(uid: String): Flow<UserEntity?>
}
