package com.example.edutrackapp.data.repository

import com.example.edutrackapp.Domain.Model.timeTable.TimeTableWithDetails
import com.example.edutrackapp.Domain.repository.TimeTableRepository
import com.example.edutrackapp.data.local.TimeTableDao
import javax.inject.Inject

class TimeTableRepositoryImpl @Inject constructor(
    private val timeTableDao: TimeTableDao
) : TimeTableRepository {

    override suspend fun getTimeTableForDay(
        day: String,
        facultyId: Int
    ): List<TimeTableWithDetails> {
        return timeTableDao.getTimeTableForDay(day, facultyId)
    }
}