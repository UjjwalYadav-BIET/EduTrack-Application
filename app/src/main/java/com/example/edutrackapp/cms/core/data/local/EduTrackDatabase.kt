package com.example.edutrackapp.cms.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.edutrackapp.cms.core.data.local.dao.*
import com.example.edutrackapp.cms.core.data.local.entity.*
import com.example.edutrackapp.data.Attendence.AttendanceEntity
import com.example.edutrackapp.data.Attendence.FacultyEntity
import com.example.edutrackapp.data.Attendence.FacultySubjectEntity
import com.example.edutrackapp.data.Attendence.StudentEntity
import com.example.edutrackapp.data.Attendence.SubjectEntity
import com.example.edutrackapp.data.local.AttendanceDao
import com.example.edutrackapp.data.local.TimeTableDao
import com.example.edutrackapp.data.timeTable.TimeTableEntity

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
        TimeTableEntity::class
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
    abstract fun attendanceDao(): AttendanceDao
    abstract fun timeTableDao(): TimeTableDao

}