package com.example.edutrackapp.cms.feature.student_module.notices.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.repository.NoticeRepository
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.NoticeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StudentNoticeViewModel @Inject constructor(
    private val repository: NoticeRepository
) : ViewModel() {

    private val studentYear = 2
    private val studentBranch = "CSE"
    private val studentSection = "A"

    val notices = repository.getNoticesForStudent(
        studentYear,
        studentBranch,
        studentSection
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
}