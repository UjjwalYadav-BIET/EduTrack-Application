package com.example.edutrackapp.cms.feature.teacher_Module

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ─── Data Model (matches your Firestore fields exactly) ──────────────────────
data class TeacherProfile(
    val name: String = "",
    val email: String = "",
    val department: String = "",
    val employeeId: String = "",
    val subject: String = "",
    val phone: String = "",
    val role: String = ""
)

// ─── UI State ─────────────────────────────────────────────────────────────────
sealed class TeacherProfileState {
    object Loading : TeacherProfileState()
    data class Success(val profile: TeacherProfile) : TeacherProfileState()
    data class Error(val message: String) : TeacherProfileState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────
class TeacherViewModel : ViewModel() {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private val _profileState =
        MutableStateFlow<TeacherProfileState>(TeacherProfileState.Loading)
    val profileState: StateFlow<TeacherProfileState> = _profileState.asStateFlow()

    init {
        listenToTeacherProfile()
    }

    // Attaches a real-time Firestore snapshot listener for the logged-in teacher
    private fun listenToTeacherProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _profileState.value = TeacherProfileState.Error("Teacher not logged in.")
            return
        }

        listenerRegistration = firestore
            .collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _profileState.value =
                        TeacherProfileState.Error(error.message ?: "Unknown Firestore error")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val profile = TeacherProfile(
                        name       = snapshot.getString("name")       ?: "",
                        email      = snapshot.getString("email")      ?: "",
                        department = snapshot.getString("department") ?: "",
                        employeeId = snapshot.getString("employeeId") ?: "",
                        subject    = snapshot.getString("subject")    ?: "",
                        phone      = snapshot.getString("phone")      ?: "",
                        role       = snapshot.getString("role")       ?: ""
                    )
                    _profileState.value = TeacherProfileState.Success(profile)
                } else {
                    _profileState.value =
                        TeacherProfileState.Error("Teacher profile not found in Firestore.")
                }
            }
    }

    // Removes listener when ViewModel is destroyed to prevent memory leaks
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}