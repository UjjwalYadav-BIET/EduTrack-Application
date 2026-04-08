package com.example.edutrackapp.cms.feature.student_module.results.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.core.data.local.entity.ResultEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class StudentResultViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db:   FirebaseFirestore
) : ViewModel() {

    private val _results = MutableStateFlow<List<ResultEntity>>(emptyList())
    val results: StateFlow<List<ResultEntity>> = _results.asStateFlow()

    init { loadRollNoThenListen() }

    private fun loadRollNoThenListen() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc    = db.collection("users").document(uid).get().await()
                val rollNo = doc.getString("enrollmentId") ?: return@launch
                listenToResults(rollNo)
            } catch (e: Exception) { /* handle */ }
        }
    }

    private fun listenToResults(rollNo: String) {
        db.collection("results")
            .whereEqualTo("rollNo", rollNo)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                // Fires the moment the teacher saves marks — no refresh needed
                _results.value = snapshot.documents.mapNotNull { doc ->
                    ResultEntity(
                        studentName   = doc.getString("studentName")   ?: "",
                        rollNo        = doc.getString("rollNo")         ?: "",
                        subject       = doc.getString("subject")        ?: "",
                        examType      = doc.getString("examType")       ?: "",
                        marksObtained = doc.getString("marksObtained")  ?: "0",
                        maxMarks      = doc.getString("maxMarks")       ?: "100"
                    )
                }
            }
    }
}