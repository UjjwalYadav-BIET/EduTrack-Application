package com.example.edutrackapp.cms.core.di

import com.example.edutrackapp.Domain.repository.AssignmentRepository
import com.example.edutrackapp.Domain.repository.AssignmentSubmissionRepository
import com.example.edutrackapp.Domain.repository.AttendanceRepository
import com.example.edutrackapp.data.repository.AssignmentRepositoryImpl
import com.example.edutrackapp.data.repository.AssignmentSubmissionRepositoryImpl
import com.example.edutrackapp.data.repository.AttendanceRepositoryImpl
import dagger.Binds
import dagger.Module
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
    abstract fun bindAssignmentRepository(
        impl: AssignmentRepositoryImpl
    ): AssignmentRepository

    @Binds
    @Singleton
    abstract fun bindAssignmentSubmissionRepository(
        impl: AssignmentSubmissionRepositoryImpl
    ): AssignmentSubmissionRepository

}