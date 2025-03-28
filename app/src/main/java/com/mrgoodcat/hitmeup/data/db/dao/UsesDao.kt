package com.mrgoodcat.hitmeup.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mrgoodcat.hitmeup.data.model.UserLocalModel

@Dao
interface UsesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(users: UserLocalModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserLocalModel>)

    @Query("SELECT * FROM user WHERE user_id == :id LIMIT 1")
    suspend fun getUser(id: String): UserLocalModel?

    @Query("SELECT * FROM user ")
    fun getUsers(): PagingSource<Int, UserLocalModel>

    @Update
    suspend fun updateUser(users: UserLocalModel)

    @Delete
    suspend fun deleteUser(users: UserLocalModel)

    @Query("DELETE FROM user")
    suspend fun clearDb()
}