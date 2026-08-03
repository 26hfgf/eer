package com.aibeautypro.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BeautyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BeautyRecord): Long

    @Update
    suspend fun update(record: BeautyRecord)

    @Delete
    suspend fun delete(record: BeautyRecord)

    @Query("SELECT * FROM beauty_records ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BeautyRecord>>

    @Query("SELECT * FROM beauty_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BeautyRecord?

    @Query("UPDATE beauty_records SET hairImagePath = :path WHERE id = :id")
    suspend fun updateHairPath(id: Long, path: String)

    @Query("UPDATE beauty_records SET pdfPath = :path WHERE id = :id")
    suspend fun updatePdfPath(id: Long, path: String)

    @Query("DELETE FROM beauty_records")
    suspend fun clearAll()
}
