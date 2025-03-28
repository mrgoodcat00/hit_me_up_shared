package com.mrgoodcat.hitmeup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mrgoodcat.hitmeup.data.model.AppSettingsLocalModel

@Dao
interface AppSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSettings(settings: AppSettingsLocalModel)

    @Query("SELECT * FROM app_settings")
    suspend fun getAppSettings(): AppSettingsLocalModel?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAppSettings(settings: AppSettingsLocalModel)

    @Query("DELETE FROM app_settings")
    suspend fun clearDb()
}