package com.example.edutrackapp.cms.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.edutrackapp.cms.core.data.local.entity.NoticeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {

    // ✅ Insert new notice
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    // ✅ Update existing notice
    @Update
    suspend fun updateNotice(notice: NoticeEntity)

    // ✅ Get all notices posted by a specific teacher
    @Query("""
        SELECT * FROM notices 
        WHERE teacherId = :teacherId 
        ORDER BY createdAt DESC
    """)
    fun getNoticesByTeacher(teacherId: Int): Flow<List<NoticeEntity>>

    // ✅ Get only active notices of teacher
    @Query("""
        SELECT * FROM notices 
        WHERE teacherId = :teacherId 
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getActiveNoticesByTeacher(teacherId: Int): Flow<List<NoticeEntity>>

    // ✅ Get single notice (for edit screen)
    @Query("SELECT * FROM notices WHERE id = :noticeId")
    suspend fun getNoticeById(noticeId: Int): NoticeEntity?

    // ✅ Deactivate notice (soft delete 🔥)
    @Query("UPDATE notices SET isActive = 0 WHERE id = :noticeId")
    suspend fun deactivateNotice(noticeId: Int)

    // ✅ Reactivate notice (optional but useful)
    @Query("UPDATE notices SET isActive = 1 WHERE id = :noticeId")
    suspend fun activateNotice(noticeId: Int)

    // ❌ Hard delete (avoid in ERP, but keeping if needed)
    @Delete
    suspend fun deleteNotice(notice: NoticeEntity)





    @Query("""
SELECT * FROM notices 
WHERE isActive = 1
AND targetYear = :year
AND targetBranch = :branch
AND targetSection = :section
ORDER BY createdAt DESC
""")
    fun getNoticesForStudent(
        year: Int,
        branch: String,
        section: String
    ): Flow<List<NoticeEntity>>

    @Query("""
UPDATE notices 
SET isActive = :status 
WHERE id = :noticeId
""")
    suspend fun updateNoticeStatus(
        noticeId: Int,
        status: Boolean
    )
}