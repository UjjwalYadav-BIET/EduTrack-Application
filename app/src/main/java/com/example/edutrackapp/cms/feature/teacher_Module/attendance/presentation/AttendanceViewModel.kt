package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.data.AttendanceRepository
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.data.EnrolledFace
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.ml.FaceNetModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AttendanceUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val message: String? = null,
    val lastRecognisedName: String? = null,
    val markedPresent: Set<String> = emptySet()
)

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    private var _faceNetModel: FaceNetModel? = null
    private fun getFaceNetModel(): FaceNetModel {
        if (_faceNetModel == null) _faceNetModel = FaceNetModel(context)
        return _faceNetModel!!
    }

    private var enrolledFaces: List<EnrolledFace> = emptyList()
    private var lastProcessedMs: Long = 0L
    private val throttleMs = 1_500L

    // ── Attendance History ────────────────────────────────────────────────────

    var attendanceHistory = mutableStateListOf<AttendanceRecord>()
        private set

    var isLoadingHistory by mutableStateOf(false)
        private set

    fun loadAttendanceHistory(classId: String) {
        viewModelScope.launch {
            isLoadingHistory = true
            try {
                val records = repository.getAttendanceHistory(classId)
                attendanceHistory.clear()
                attendanceHistory.addAll(records)
            } catch (e: Exception) {
                _uiState.update { it.copy(isError = true, message = "Failed to load history: ${e.message}") }
            } finally {
                isLoadingHistory = false
            }
        }
    }

    // ── Manual Attendance ─────────────────────────────────────────────────────

    private val _students = MutableStateFlow<List<StudentUiModel>>(emptyList())
    val students: StateFlow<List<StudentUiModel>> = _students.asStateFlow()

    var isLoadingStudents by mutableStateOf(false)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var selectedDate by mutableStateOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    )
        private set

    var selectedTime by mutableStateOf(
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    )
        private set

    fun filteredStudents(allStudents: List<StudentUiModel>): List<StudentUiModel> =
        if (searchQuery.isBlank()) allStudents
        else allStudents.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.rollNo.contains(searchQuery, ignoreCase = true)
        }

    fun loadStudents(classId: String, department: String? = "CSE") {
        viewModelScope.launch {
            isLoadingStudents = true
            try {
                val list = repository.getStudents(classId, department)
                _students.value = list
            } catch (e: Exception) {
                _uiState.update { it.copy(isError = true, message = e.message) }
            } finally {
                isLoadingStudents = false
            }
        }
    }

    fun toggleAttendance(studentId: String) {
        _students.update { list ->
            list.map { if (it.id == studentId) it.copy(isPresent = !it.isPresent) else it }
        }
    }

    fun markStudentsBasedOnCount(count: Int) {
        _students.update { list ->
            list.mapIndexed { index, student -> student.copy(isPresent = index < count) }
        }
    }

    fun getPresentCount(): Int = _students.value.count { it.isPresent }
    fun getAbsentCount(): Int  = _students.value.count { !it.isPresent }
    fun getTotalCount(): Int   = _students.value.size

    fun onSearchQueryChange(query: String) { searchQuery = query }
    fun onDateChange(date: String)         { selectedDate = date }
    fun onTimeChange(time: String)         { selectedTime = time }

    fun saveAttendanceRecord() {
        viewModelScope.launch {
            try {
                repository.saveAttendance(
                    date     = selectedDate,
                    time     = selectedTime,
                    students = _students.value
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isError = true, message = e.message) }
            }
        }
    }

    fun exportToCSV(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val csv = buildString {
                    appendLine("Name,Roll No,Status,Date,Time")
                    _students.value.forEach {
                        appendLine(
                            "${it.name},${it.rollNo}," +
                                    "${if (it.isPresent) "Present" else "Absent"}," +
                                    "$selectedDate,$selectedTime"
                        )
                    }
                }
                val fileName = "attendance_${selectedDate.replace("/", "-")}.csv"
                val file = java.io.File(context.getExternalFilesDir(null), fileName)
                file.writeText(csv)
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context, "Exported to ${file.path}", android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isError = true, message = "Export failed: ${e.message}") }
            }
        }
    }

    // ── Enrollment ────────────────────────────────────────────────────────────

    // Collects multiple frames and averages them for a more reliable embedding
    private val enrollmentFrames = mutableListOf<FloatArray>()
    private val ENROLL_FRAME_COUNT = 5  // capture 5 frames and average

    fun enrollStudent(classId: String, studentName: String, faceBitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Capturing face…") }
            try {
                val embedding = withContext(Dispatchers.Default) {
                    getFaceNetModel().getEmbedding(faceBitmap)
                }
                enrollmentFrames.add(embedding)

                if (enrollmentFrames.size < ENROLL_FRAME_COUNT) {
                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            message   = "Capturing… (${enrollmentFrames.size}/$ENROLL_FRAME_COUNT)"
                        )
                    }
                    return@launch
                }

                // Average all captured embeddings for better accuracy
                val avgEmbedding = averageEmbeddings(enrollmentFrames)
                enrollmentFrames.clear()

                repository.enrollStudent(classId, studentName, avgEmbedding)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError   = false,
                        message   = "✅ $studentName enrolled successfully!"
                    )
                }
            } catch (e: Exception) {
                enrollmentFrames.clear()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError   = true,
                        message   = "❌ Enrollment failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun averageEmbeddings(embeddings: List<FloatArray>): FloatArray {
        val size = embeddings[0].size
        val avg  = FloatArray(size)
        for (emb in embeddings) {
            for (i in 0 until size) avg[i] += emb[i]
        }
        for (i in 0 until size) avg[i] /= embeddings.size
        // L2 normalise the average
        val norm = Math.sqrt(avg.map { (it * it).toDouble() }.sum()).toFloat()
        return if (norm == 0f) avg else FloatArray(size) { avg[it] / norm }
    }

    // ── Face Recognition ──────────────────────────────────────────────────────

    fun loadEnrolledFaces(classId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                enrolledFaces = repository.getEnrolledFaces(classId)
                android.util.Log.d("FaceRecog", "Loaded ${enrolledFaces.size} enrolled faces")
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, isError = true, message = "Failed to load enrolled faces: ${e.message}")
                }
            }
        }
    }

    // Rolling buffer of recent match results to avoid single-frame false positives
    private val recentMatches = mutableListOf<String>()
    private val MATCH_BUFFER_SIZE = 3  // needs 3 consecutive matches to confirm

    fun processFrameEmbedding(classId: String, sessionId: String, faceBitmap: Bitmap) {
        val now = System.currentTimeMillis()
        if (now - lastProcessedMs < throttleMs) return
        lastProcessedMs = now

        viewModelScope.launch(Dispatchers.Default) {
            val embedding = getFaceNetModel().getEmbedding(faceBitmap)
            val result    = findBestMatch(embedding)

            if (result == null) {
                // No confident match — clear buffer
                recentMatches.clear()
                return@launch
            }

            val (matchName, matchScore) = result
            android.util.Log.d("FaceRecog", "Match: $matchName | Score: $matchScore")

            // Add to rolling buffer
            recentMatches.add(matchName)
            if (recentMatches.size > MATCH_BUFFER_SIZE) {
                recentMatches.removeAt(0)
            }

            // Only confirm if all recent matches agree on the same person
            val confirmedName = if (
                recentMatches.size == MATCH_BUFFER_SIZE &&
                recentMatches.all { it == matchName }
            ) matchName else null

            if (confirmedName == null) return@launch

            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    state.copy(
                        lastRecognisedName = confirmedName,
                        markedPresent      = state.markedPresent + confirmedName
                    )
                }
                // Match by studentId (more reliable than name string matching)
                _students.update { list ->
                    list.map { student ->
                        if (student.name.trim().equals(confirmedName.trim(), ignoreCase = true))
                            student.copy(isPresent = true)
                        else student
                    }
                }
            }

            try {
                repository.markPresent(classId, sessionId, confirmedName)
            } catch (_: Exception) { }
        }
    }

    private data class MatchResult(val name: String, val score: Float)

    private fun findBestMatch(embedding: FloatArray): MatchResult? {
        if (enrolledFaces.isEmpty()) return null

        // Raised threshold — critical for accuracy
        val THRESHOLD   = 0.82f
        // Minimum gap between best and second-best match
        // prevents ambiguous faces from triggering a match
        val MIN_GAP     = 0.08f

        var bestScore   = 0f
        var bestName    = ""
        var secondScore = 0f

        for (face in enrolledFaces) {
            val stored = face.embedding.toFloatArray()
            val score  = getFaceNetModel().cosineSimilarity(embedding, stored)
            android.util.Log.d("FaceRecog", "  vs ${face.studentName}: $score")

            if (score > bestScore) {
                secondScore = bestScore
                bestScore   = score
                bestName    = face.studentName
            } else if (score > secondScore) {
                secondScore = score
            }
        }

        val gap = bestScore - secondScore
        android.util.Log.d("FaceRecog", "Best: $bestName=$bestScore | 2nd=$secondScore | gap=$gap")

        return if (bestScore >= THRESHOLD && gap >= MIN_GAP)
            MatchResult(bestName, bestScore)
        else null
    }

    fun finaliseSession(classId: String, sessionId: String) {
        viewModelScope.launch {
            try {
                repository.closeSession(classId, sessionId)
                _uiState.update {
                    it.copy(message = "Session saved. ${it.markedPresent.size} student(s) marked present.")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isError = true, message = "Failed to close session: ${e.message}") }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    override fun onCleared() {
        super.onCleared()
        _faceNetModel?.close()
        _faceNetModel = null
    }
}