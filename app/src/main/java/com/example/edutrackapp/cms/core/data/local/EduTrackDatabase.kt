package com.example.edutrackapp.cms.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.edutrackapp.cms.core.data.local.dao.*
import com.example.edutrackapp.cms.core.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        NoticeEntity::class,
        ResultEntity::class,
        AssignmentEntity::class,
        SubmissionEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class EduTrackDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val noticeDao: NoticeDao
    abstract val resultDao: ResultDao
    abstract val assignmentDao: AssignmentDao
    abstract val submissionDao: SubmissionDao
}