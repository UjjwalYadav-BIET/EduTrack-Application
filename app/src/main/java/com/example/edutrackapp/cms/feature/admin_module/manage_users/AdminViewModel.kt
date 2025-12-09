package com.example.edutrackapp.cms.feature.admin_module.manage_users // <--- Check if your folder is 'admin' or 'admin_module'

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val database: EduTrackDatabase
) : ViewModel() {

    // ===========================
    // 1. TEACHER VARIABLES
    // ===========================
    var name = mutableStateOf("")
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var department = mutableStateOf("Computer Science")

    fun onNameChange(text: String) { name.value = text }
    fun onEmailChange(text: String) { email.value = text }
    fun onPassChange(text: String) { password.value = text }

    fun createTeacherAccount(onSuccess: () -> Unit) {
        if (name.value.isNotEmpty() && email.value.isNotEmpty() && password.value.isNotEmpty()) {
            viewModelScope.launch {
                val newTeacher = UserEntity(
                    userId = "TCH_${System.currentTimeMillis()}",
                    name = name.value,
                    email = email.value,
                    password = password.value,
                    role = "TEACHER"
                )

                database.userDao.insertUser(newTeacher)
                onSuccess()
            }
        }
    }

    // ===========================
    // 2. STUDENT VARIABLES
    // ===========================
    var studentName = mutableStateOf("")
    var studentEmail = mutableStateOf("")
    var studentRollNo = mutableStateOf("")
    var studentPassword = mutableStateOf("")

    fun onStNameChange(text: String) { studentName.value = text }
    fun onStEmailChange(text: String) { studentEmail.value = text }
    fun onStRollChange(text: String) { studentRollNo.value = text }
    fun onStPassChange(text: String) { studentPassword.value = text }

    fun createStudentAccount(onSuccess: () -> Unit) {
        if (studentName.value.isNotEmpty() && studentRollNo.value.isNotEmpty() && studentPassword.value.isNotEmpty()) {
            viewModelScope.launch {
                val newStudent = UserEntity(
                    userId = studentRollNo.value.uppercase(),
                    name = studentName.value,
                    email = studentEmail.value,
                    password = studentPassword.value,
                    role = "STUDENT"
                )

                database.userDao.insertUser(newStudent)
                onSuccess()
            }
        }
    }
}