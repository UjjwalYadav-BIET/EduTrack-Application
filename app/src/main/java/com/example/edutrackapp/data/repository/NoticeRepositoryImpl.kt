package com.example.edutrackapp.data.repository

import com.example.edutrackapp.Domain.repository.NoticeRepository
import com.example.edutrackapp.cms.core.data.local.dao.NoticeDao
import com.example.edutrackapp.cms.core.data.local.entity.NoticeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoticeRepositoryImpl @Inject constructor(
    private val noticeDao: NoticeDao
) : NoticeRepository {

    override suspend fun insertNotice(notice: NoticeEntity) {
        noticeDao.insertNotice(notice)
    }

    override suspend fun updateNotice(notice: NoticeEntity) {
        noticeDao.updateNotice(notice)
    }

    override fun getNoticesByTeacher(teacherId: Int): Flow<List<NoticeEntity>> {
        return noticeDao.getNoticesByTeacher(teacherId)
    }

    override fun getActiveNoticesByTeacher(teacherId: Int): Flow<List<NoticeEntity>> {
        return noticeDao.getActiveNoticesByTeacher(teacherId)
    }

    override suspend fun getNoticeById(noticeId: Int): NoticeEntity? {
        return noticeDao.getNoticeById(noticeId)
    }

    override suspend fun deactivateNotice(noticeId: Int) {
        noticeDao.deactivateNotice(noticeId)
    }

    override suspend fun activateNotice(noticeId: Int) {
        noticeDao.activateNotice(noticeId)
    }

    override suspend fun deleteNotice(notice: NoticeEntity) {
        noticeDao.deleteNotice(notice)
    }

    override  fun getNoticesForStudent(
        year: Int,
        branch: String,
        section: String
    ): Flow<List<NoticeEntity>> {
        return noticeDao.getNoticesForStudent(year, branch, section)
    }

    override fun getTeacherNotices(teacherId: Int): Flow<List<NoticeEntity>> {
        return noticeDao.getNoticesByTeacher(teacherId)
    }


    override suspend fun updateNoticeStatus(noticeId: Int, isActive: Boolean) {
        noticeDao.updateNoticeStatus(noticeId, isActive)
    }
}