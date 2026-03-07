package com.example.edutrackapp.data.local

import androidx.room.Dao
import androidx.room.Query
import com.example.edutrackapp.Domain.Model.timeTable.TimeTableWithDetails

@Dao
interface TimeTableDao {

    @Query("""
        SELECT 
            t.id AS timetableId,
            t.day,
            t.startTime,
            t.endTime,
            s.subjectName AS subjectName,
            t.branch,
            t.semester,
            t.section
        FROM timetable t
        INNER JOIN subjects s
        ON t.subjectId = s.subjectId
        WHERE t.day = :day AND t.facultyId = :facultyId
        ORDER BY t.startTime
    """)
    suspend fun getTimeTableForDay(
        day: String,
        facultyId: Int
    ): List<TimeTableWithDetails>
}
