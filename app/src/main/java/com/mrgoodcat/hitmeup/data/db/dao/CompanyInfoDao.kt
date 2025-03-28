package com.mrgoodcat.hitmeup.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mrgoodcat.hitmeup.data.model.CompanyInfoLocalModel

@Dao
interface CompanyInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfo(info: CompanyInfoLocalModel)

    @Query("SELECT * FROM company_info")
    suspend fun getInfo(): CompanyInfoLocalModel?

    @Update
    suspend fun updateInfo(info: CompanyInfoLocalModel)

    @Delete
    suspend fun deleteInfo(info: CompanyInfoLocalModel)

    @Query("DELETE FROM company_info")
    suspend fun clearDb()

}