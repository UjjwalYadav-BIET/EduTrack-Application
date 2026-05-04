package com.example.edutrackapp.cms.core.di

import com.example.edutrackapp.cms.feature.teacher_Module.attendance.data.AttendanceRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AttendanceModule {

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        firestore: FirebaseFirestore
    ): AttendanceRepository = AttendanceRepository(firestore)
}