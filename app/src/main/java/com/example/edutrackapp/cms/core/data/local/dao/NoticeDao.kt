package com.example.edutrackapp.cms.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.edutrackapp.cms.core.data.local.entity.NoticeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    // We use Flow<> to get real-time updates in the Student App later
    @Query("SELECT * FROM notices ORDER BY id DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>
}