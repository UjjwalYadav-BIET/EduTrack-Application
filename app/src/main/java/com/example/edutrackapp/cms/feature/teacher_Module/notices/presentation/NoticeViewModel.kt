package com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.NoticeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val database: EduTrackDatabase // Injecting DB directly for speed
) : ViewModel() {

    var title = mutableStateOf("")
    var description = mutableStateOf("")

    // "ALL" means everyone sees it. "CS-A" means only that batch.
    var targetBatch = mutableStateOf("ALL")

    fun onTitleChange(text: String) { title.value = text }
    fun onDescChange(text: String) { description.value = text }

    fun postNotice(onSuccess: () -> Unit) {
        if (title.value.isNotEmpty() && description.value.isNotEmpty()) {
            viewModelScope.launch {
                val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

                val notice = NoticeEntity(
                    title = title.value,
                    description = description.value,
                    date = currentDate,
                    postedBy = "Prof. Ujjwal",
                    targetBatch = targetBatch.value
                )

                database.noticeDao.insertNotice(notice)
                onSuccess()
            }
        }
    }
}