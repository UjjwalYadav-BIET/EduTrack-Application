package com.example.edutrackapp.cms.feature.student_module.attendance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AttendanceSession(
    val date:   String,
    val time:   String,
    val status: String   // "present" or "absent"
)

@HiltViewModel
class StudentAttendanceViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db:   FirebaseFirestore
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<AttendanceSession>>(emptyList())
    val sessions: StateFlow<List<AttendanceSession>> = _sessions.asStateFlow()

    // Derived percentage — recomputes whenever sessions change
    val overallPercentage: StateFlow<Int> = _sessions
        .map { list ->
            if (list.isEmpty()) 0
            else (list.count { it.status == "present" } * 100) / list.size
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init { loadRollNoThenListen() }

    private fun loadRollNoThenListen() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc    = db.collection("users").document(uid).get().await()
                val rollNo = doc.getString("enrollmentId") ?: return@launch
                listenToAttendance(rollNo)
            } catch (e: Exception) { /* handle */ }
        }
    }

    private fun listenToAttendance(rollNo: String) {
        db.collection("attendance")
            .whereEqualTo("rollNo", rollNo)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                // This fires instantly whenever the teacher submits attendance
                _sessions.value = snapshot.documents.map { doc ->
                    AttendanceSession(
                        date   = doc.getString("date")   ?: "",
                        time   = doc.getString("time")   ?: "",
                        status = doc.getString("status") ?: "absent"
                    )
                }
            }
    }
}