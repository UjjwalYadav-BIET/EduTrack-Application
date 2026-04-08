package com.example.edutrackapp.cms.feature.student_module.notices.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.feature.notices.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StudentNoticeViewModel @Inject constructor(
    repository: NoticeRepository
) : ViewModel() {

    // Real-time Firestore listener — students see "all" + "student" notices
    val notices = repository.getNoticesForRole("student")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}