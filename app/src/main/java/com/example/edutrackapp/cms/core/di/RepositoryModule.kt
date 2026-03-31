package com.example.edutrackapp.cms.core.di

import com.example.edutrackapp.Domain.repository.AssignmentRepository
import com.example.edutrackapp.Domain.repository.AssignmentSubmissionRepository
import com.example.edutrackapp.Domain.repository.AttendanceRepository
import com.example.edutrackapp.Domain.repository.NoticeRepository
import com.example.edutrackapp.cms.core.data.local.dao.NoticeDao
import com.example.edutrackapp.data.repository.AssignmentRepositoryImpl
import com.example.edutrackapp.data.repository.AssignmentSubmissionRepositoryImpl
import com.example.edutrackapp.data.repository.AttendanceRepositoryImpl
import com.example.edutrackapp.data.repository.NoticeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        impl: AttendanceRepositoryImpl
    ): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindNoticeRepository(
        impl: NoticeRepositoryImpl
    ): NoticeRepository


    @Binds
    @Singleton
    abstract fun bindAssignmentRepository(
        impl: AssignmentRepositoryImpl
    ): AssignmentRepository

    @Binds
    @Singleton
    abstract fun bindAssignmentSubmissionRepository(
        impl: AssignmentSubmissionRepositoryImpl
    ): AssignmentSubmissionRepository

}