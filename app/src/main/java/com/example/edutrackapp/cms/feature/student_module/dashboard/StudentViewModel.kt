package com.example.edutrackapp.cms.feature.student_module.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class StudentProfile(
    val name         : String = "",
    val email        : String = "",
    val enrollmentId : String = "",
    val department   : String = "",
    val phone        : String = ""
)

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val auth : FirebaseAuth,
    private val db   : FirebaseFirestore
) : ViewModel() {

    private val _profile = MutableStateFlow(StudentProfile())
    val profile: StateFlow<StudentProfile> = _profile

    private val _attendancePercent = MutableStateFlow("--")
    val attendancePercent: StateFlow<String> = _attendancePercent

    private val _cgpa = MutableStateFlow("--")
    val cgpa: StateFlow<String> = _cgpa

    private val _noticeCount = MutableStateFlow(0)
    val noticeCount: StateFlow<Int> = _noticeCount

    init {
        loadStudentData()
    }

    private fun loadStudentData() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Load profile
            val doc = db.collection("users").document(uid).get().await()
            _profile.value = StudentProfile(
                name         = doc.getString("name")         ?: "",
                email        = doc.getString("email")        ?: "",
                enrollmentId = doc.getString("enrollmentId") ?: "",
                department   = doc.getString("department")   ?: "",
                phone        = doc.getString("phone")        ?: ""
            )

            // Load attendance
            val attendanceDocs = db.collection("attendance")
                .whereEqualTo("studentId", uid)
                .get().await()
            val total   = attendanceDocs.size()
            val present = attendanceDocs.count { it.getString("status") == "present" }
            _attendancePercent.value = if (total > 0)
                "${((present.toFloat() / total) * 100).toInt()}%" else "N/A"

            // Load results / CGPA
            val resultDocs = db.collection("results")
                .whereEqualTo("studentId", uid)
                .get().await()
            if (resultDocs.isEmpty) {
                _cgpa.value = "N/A"
            } else {
                val avg = resultDocs.mapNotNull {
                    it.getDouble("marks")
                }.average()
                // Convert marks (out of 100) to 10-point CGPA
                _cgpa.value = String.format("%.1f", avg / 10.0)
            }

            // Load notice count
            val noticeDocs = db.collection("notices")
                .whereIn("target", listOf("students", "all"))
                .get().await()
            _noticeCount.value = noticeDocs.size()
        }
    }
}