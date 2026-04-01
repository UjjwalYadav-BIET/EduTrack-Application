package com.example.edutrackapp.cms.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.edutrackapp.cms.core.data.local.dao.*
import com.example.edutrackapp.cms.core.data.local.entity.*
import com.example.edutrackapp.data.local.Attendence.AttendanceEntity
import com.example.edutrackapp.data.local.Attendence.FacultyEntity
import com.example.edutrackapp.data.local.Attendence.FacultySubjectEntity
import com.example.edutrackapp.data.local.Attendence.StudentEntity
import com.example.edutrackapp.data.local.Attendence.SubjectEntity
import com.example.edutrackapp.data.local.AttendanceDao
import com.example.edutrackapp.data.local.StudentDao
import com.example.edutrackapp.data.local.TimeTableDao
import com.example.edutrackapp.data.local.timeTable.TimeTableEntity

@Database(
    entities = [
        UserEntity::class,
        NoticeEntity::class,
        ResultEntity::class,
        AssignmentEntity::class,
        SubmissionEntity::class,
        StudentEntity::class,
        SubjectEntity::class,
        FacultyEntity::class,
        AttendanceEntity::class,
        FacultySubjectEntity::class,
        TimeTableEntity::class,
        TestEntity::class,
        AssignmentSubmissionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class EduTrackDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val noticeDao: NoticeDao
    abstract val assignmentDao: AssignmentDao
    abstract val submissionDao: SubmissionDao
    abstract val assignmentSubmissionDao: AssignmentSubmissionDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun timeTableDao(): TimeTableDao

    abstract fun resultDao(): ResultDao
    abstract fun testDao(): TestDao
    abstract fun studentDao(): StudentDao

}