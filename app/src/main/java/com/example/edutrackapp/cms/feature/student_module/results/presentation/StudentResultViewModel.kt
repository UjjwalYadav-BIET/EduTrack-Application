package com.example.edutrackapp.cms.feature.student_module.results.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.ResultEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StudentResultViewModel @Inject constructor(
    database: EduTrackDatabase
) : ViewModel() {

    // Hardcoded Roll No "CS-101" to match what you entered as Teacher
    // In a real app, this comes from the logged-in User Session
    private val studentRollNo = "CS-101"

    val results: StateFlow<List<ResultEntity>> = database.resultDao.getResultsForStudent(studentRollNo)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}