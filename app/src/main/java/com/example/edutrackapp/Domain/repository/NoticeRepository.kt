package com.example.edutrackapp.Domain.repository

import com.example.edutrackapp.cms.core.data.local.entity.NoticeEntity
import kotlinx.coroutines.flow.Flow

interface NoticeRepository {

    suspend fun insertNotice(notice: NoticeEntity)

    suspend fun updateNotice(notice: NoticeEntity)

    fun getNoticesByTeacher(teacherId: Int): Flow<List<NoticeEntity>>

    fun getActiveNoticesByTeacher(teacherId: Int): Flow<List<NoticeEntity>>

    suspend fun getNoticeById(noticeId: Int): NoticeEntity?

    suspend fun deactivateNotice(noticeId: Int)

    suspend fun activateNotice(noticeId: Int)

    fun getTeacherNotices(teacherId: Int): Flow<List<NoticeEntity>>

    suspend fun deleteNotice(notice: NoticeEntity)

    fun getNoticesForStudent(
        year: Int,
        branch: String,
        section: String
    ): Flow<List<NoticeEntity>>

    suspend fun updateNoticeStatus(noticeId: Int, isActive: Boolean)
}