package com.mrgoodcat.hitmeup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrgoodcat.hitmeup.data.model.UserProfileLocalModel

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM profile LIMIT 1")
    suspend fun getMe(): UserProfileLocalModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMe(user: UserProfileLocalModel)

    @Query("DELETE FROM profile")
    suspend fun clearDb()
}