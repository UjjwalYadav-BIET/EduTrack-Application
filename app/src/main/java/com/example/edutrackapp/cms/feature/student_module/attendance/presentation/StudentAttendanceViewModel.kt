package com.example.edutrackapp.cms.feature.student_module.attendance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    init { loadAttendance() }

    /**
     * Loads the student's attendance by:
     * 1. Getting the student's enrollmentId from users/{uid}
     * 2. Scanning all sessions under classes/CS-A/sessions
     * 3. For each session, reading the attendance doc keyed by uid
     *
     * This matches exactly how AttendanceRepository.saveAttendance() writes data:
     *   classes/{classId}/sessions/{sessionId}/attendance/{studentId}
     */
    private fun loadAttendance() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Step 1: get the student's classId / department from their user doc
                val userDoc = db.collection("users").document(uid).get().await()
                // We use "CS-A" as the default classId — same default used in AttendanceRepository
                val classId = "CS-A"

                // Step 2: fetch all session metadata docs (already contain date, time)
                val sessionsSnapshot = db
                    .collection("classes")
                    .document(classId)
                    .collection("sessions")
                    .orderBy("savedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val result = mutableListOf<AttendanceSession>()

                // Step 3: for each session, check this student's attendance doc
                for (sessionDoc in sessionsSnapshot.documents) {
                    val sessionId   = sessionDoc.id
                    val date        = sessionDoc.getString("date") ?: continue
                    val time        = sessionDoc.getString("time") ?: continue

                    val attendanceDoc = db
                        .collection("classes")
                        .document(classId)
                        .collection("sessions")
                        .document(sessionId)
                        .collection("attendance")
                        .document(uid)          // keyed by studentId (uid) in saveAttendance()
                        .get()
                        .await()

                    // If the doc exists, use its status; otherwise student was absent
                    val status = if (attendanceDoc.exists())
                        attendanceDoc.getString("status") ?: "absent"
                    else
                        "absent"

                    result.add(AttendanceSession(date = date, time = time, status = status))
                }

                _sessions.value = result

            } catch (e: Exception) {
                android.util.Log.e("StudentAttendanceVM", "Error loading attendance: ${e.message}", e)
            }
        }
    }
}