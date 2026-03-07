package com.example.edutrackapp.Domain.repository

import com.example.edutrackapp.Domain.Model.timeTable.TimeTableWithDetails

interface TimeTableRepository {

    suspend fun getTimeTableForDay(
        day: String,
        facultyId: Int
    ): List<TimeTableWithDetails>

}