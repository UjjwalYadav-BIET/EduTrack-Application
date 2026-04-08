package com.example.edutrackapp.cms.feature.teacher_Module.results.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    val students = mutableStateListOf<StudentResultUi>()

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("students").get().await()
                val loaded = snapshot.documents.mapNotNull { doc ->
                    val name   = doc.getString("name")   ?: return@mapNotNull null
                    val rollNo = doc.getString("rollNo") ?: return@mapNotNull null
                    StudentResultUi(name = name, rollNo = rollNo)
                }
                students.clear()
                students.addAll(loaded)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onMarksChange(index: Int, marks: String) {
        students[index] = students[index].copy(marks = marks)
    }

    fun toggleAbsent(index: Int) {
        val current = students[index]
        students[index] = current.copy(
            marks = if (current.marks.equals("AB", ignoreCase = true)) "" else "AB"
        )
    }

    fun markAllAbsent() {
        students.replaceAll { it.copy(marks = "AB") }
    }

    fun clearAllMarks() {
        students.replaceAll { it.copy(marks = "") }
    }

    fun saveResults(examType: String, subject: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                students.forEach { student ->
                    if (student.marks.isNotBlank()) {
                        val data = mapOf(
                            "studentName"   to student.name,
                            "rollNo"        to student.rollNo,
                            "subject"       to subject,
                            "examType"      to examType,
                            "marksObtained" to student.marks,
                            "maxMarks"      to "100"
                        )
                        db.collection("results")
                            .document("${student.rollNo}_${subject}_${examType}")
                            .set(data)
                            .await()
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}