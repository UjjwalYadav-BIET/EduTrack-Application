package com.example.edutrackapp.cms.feature.teacher_Module.timetable.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.Model.timeTable.TimeTableWithDetails
import com.example.edutrackapp.data.local.TimeTableDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TimeTableViewModel @Inject constructor(
    private val dao: TimeTableDao
) : ViewModel() {

    var selectedDay by mutableStateOf("Mon")
        private set

    var currentClasses by mutableStateOf<List<TimeTableWithDetails>>(emptyList())
        private set

    private val loginIdfaculty = 1

    init {
        loadTimeTable()
    }

    fun selectDay(day: String) {
        selectedDay = day
        loadTimeTable()
    }

    fun loadTimeTable() {

        val day = selectedDay

        viewModelScope.launch {
            currentClasses = dao.getTimeTableForDay(day, loginIdfaculty)
        }
    }
}