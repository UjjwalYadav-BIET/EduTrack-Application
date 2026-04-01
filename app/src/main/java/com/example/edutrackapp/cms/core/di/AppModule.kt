package com.example.edutrackapp.cms.core.di

import android.app.Application
import androidx.room.Room
import com.example.edutrackapp.Domain.repository.ResultRepository
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.dao.AssignmentDao
import com.example.edutrackapp.cms.core.data.local.dao.AssignmentSubmissionDao
import com.example.edutrackapp.cms.core.data.local.dao.NoticeDao
import com.example.edutrackapp.cms.core.data.local.dao.ResultDao
import com.example.edutrackapp.cms.core.data.local.dao.TestDao
import com.example.edutrackapp.data.local.AttendanceDao
import com.example.edutrackapp.data.local.StudentDao
import com.example.edutrackapp.data.local.TimeTableDao
import com.example.edutrackapp.data.repository.ResultRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): EduTrackDatabase {
        return Room.databaseBuilder(
            app,
            EduTrackDatabase::class.java,
            "edutrack_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: EduTrackDatabase) = db.userDao

    @Provides
    @Singleton
    fun provideAttendanceDao(
        db: EduTrackDatabase
    ): AttendanceDao {
        return db.attendanceDao()
    }

    @Provides
    @Singleton
    fun provideTimeTableDao(
        db: EduTrackDatabase
    ): TimeTableDao {
        return db.timeTableDao()
    }

    @Provides
    @Singleton
    fun provideAssignmentDao(db: EduTrackDatabase): AssignmentDao {
        return db.assignmentDao
    }

    @Provides
    fun provideNoticeDao(
        db: EduTrackDatabase
    ): NoticeDao {
        return db.noticeDao
    }



    @Provides
    @Singleton
    fun provideAssignmentSubmissionDao(db: EduTrackDatabase): AssignmentSubmissionDao {
        return db.assignmentSubmissionDao
    }

    @Provides
    @Singleton
    fun provideResultRepository(
        studentDao: StudentDao,
        testDao: TestDao,
        resultDao: ResultDao
    ): ResultRepository {
        return ResultRepositoryImpl(
            studentDao,
            testDao,
            resultDao
        )
    }

    @Provides
    @Singleton
    fun provideResultDao(db: EduTrackDatabase): ResultDao {
        return db.resultDao()
    }

    @Provides
    @Singleton
    fun provideTestDao(db: EduTrackDatabase): TestDao {
        return db.testDao()
    }

    @Provides
    @Singleton
    fun provideStudentDao(db: EduTrackDatabase): StudentDao {
        return db.studentDao()
    }
}