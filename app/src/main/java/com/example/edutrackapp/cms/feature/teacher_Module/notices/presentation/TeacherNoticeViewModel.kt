package com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.repository.NoticeRepository
import com.example.edutrackapp.cms.core.data.local.entity.NoticeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherNoticeViewModel @Inject constructor(
    private val repository: NoticeRepository
) : ViewModel() {

    private val teacherId = 1

    val notices = repository.getNoticesByTeacher(teacherId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun toggleNoticeStatus(notice: NoticeEntity) {
        viewModelScope.launch {
            repository.updateNoticeStatus(
                notice.id,
                !notice.isActive
            )
        }
    }
}