package com.example.edutrackapp.cms.feature.student_module.results.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.Model.StudentResultUi
import com.example.edutrackapp.Domain.repository.ResultRepository
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.ResultEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StudentResultViewModel @Inject constructor(
    private val repository: ResultRepository
) : ViewModel() {

    private val studentRollNo = 101

    val results: StateFlow<List<StudentResultUi>> =
        repository.getStudentResults(studentRollNo)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}