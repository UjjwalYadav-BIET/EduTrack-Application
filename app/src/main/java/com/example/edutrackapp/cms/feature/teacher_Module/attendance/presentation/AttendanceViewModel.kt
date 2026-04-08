package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _students = mutableStateListOf<StudentUiModel>()
    val students: List<StudentUiModel> get() = _students

    var searchQuery by mutableStateOf("")
        private set
    var selectedDate by mutableStateOf(getTodayDate())
        private set
    var selectedTime by mutableStateOf(getCurrentTime())
        private set

    var isLoadingStudents by mutableStateOf(true)
        private set

    private val _attendanceHistory = mutableStateListOf<AttendanceRecord>()
    val attendanceHistory: List<AttendanceRecord> get() = _attendanceHistory

    val filteredStudents: List<StudentUiModel>
        get() = if (searchQuery.isBlank()) _students
        else _students.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.rollNo.contains(searchQuery, ignoreCase = true)
        }

    init { listenToStudents() }

    // ── Real-time listener — pulls from admin-enrolled students only ───────────
    private fun listenToStudents() {
        db.collection("users")
            .whereEqualTo("role", "student")
            .orderBy("name")          // order by name — safe even if rollNo is missing
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AttendanceVM", "Student listener error: ${error.message}")
                    isLoadingStudents = false
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                // Preserve any present/absent toggles already made this session
                val existingStates = _students.associate { it.id to it.isPresent }

                _students.clear()
                snapshot.documents.forEach { doc ->
                    val uid  = doc.id
                    val name = doc.getString("name") ?: return@forEach
                    // rollNo preferred — enrollmentId as fallback for older records
                    val rollNo = doc.getString("rollNo")
                        ?.takeIf { it.isNotBlank() }
                        ?: doc.getString("enrollmentId")
                            ?.takeIf { it.isNotBlank() }
                        ?: return@forEach

                    _students.add(
                        StudentUiModel(
                            id        = uid,
                            name      = name,
                            rollNo    = rollNo,
                            isPresent = existingStates[uid] ?: false
                        )
                    )
                }
                isLoadingStudents = false
                Log.d("AttendanceVM", "Loaded ${_students.size} students from Firestore")
            }
    }

    fun onSearchQueryChange(query: String) { searchQuery = query }
    fun onDateChange(date: String)         { selectedDate = date  }
    fun onTimeChange(time: String)         { selectedTime = time  }

    fun toggleAttendance(studentId: String) {
        val index = _students.indexOfFirst { it.id == studentId }
        if (index != -1)
            _students[index] = _students[index].copy(isPresent = !_students[index].isPresent)
    }

    fun markStudentsBasedOnCount(count: Int) {
        for (i in _students.indices)
            _students[i] = _students[i].copy(isPresent = i < count)
    }

    fun saveAttendanceRecord() {
        val record = AttendanceRecord(
            id               = System.currentTimeMillis().toString(),
            date             = selectedDate,
            time             = selectedTime,
            className        = "CS-A",
            presentCount     = getPresentCount(),
            totalCount       = getTotalCount(),
            studentSnapshots = _students.map {
                StudentSnapshot(it.id, it.name, it.rollNo, it.isPresent)
            }
        )
        _attendanceHistory.add(0, record)

        viewModelScope.launch {
            try {
                val batch = db.batch()
                _students.forEach { student ->
                    val docId = "${student.rollNo}_${selectedDate.replace("/", "")}_${selectedTime.replace(":", "")}"
                    val ref   = db.collection("attendance").document(docId)
                    batch.set(ref, mapOf(
                        "rollNo"      to student.rollNo,
                        "studentName" to student.name,
                        "date"        to selectedDate,
                        "time"        to selectedTime,
                        "status"      to if (student.isPresent) "present" else "absent",
                        "className"   to "CS-A",
                        "timestamp"   to System.currentTimeMillis()
                    ))
                }
                batch.commit().await()
                Log.d("AttendanceVM", "Firestore batch success — ${_students.size} records")
            } catch (e: Exception) {
                Log.e("AttendanceVM", "Firestore write failed: ${e.message}", e)
            }
        }
    }

    fun exportToCSV(context: Context) {
        try {
            val sb = StringBuilder()
            sb.appendLine("Roll No,Name,Status,Date,Time,Class")
            _students.forEach { s ->
                sb.appendLine(
                    "${s.rollNo},\"${s.name}\"," +
                            "${if (s.isPresent) "Present" else "Absent"}," +
                            "$selectedDate,$selectedTime,CS-A"
                )
            }
            val dir      = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val fileName = "Attendance_CSA_${selectedDate.replace("/", "-")}.csv"
            val file     = File(dir, fileName)
            file.writeText(sb.toString())
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type    = "text/csv"
                putExtra(Intent.EXTRA_STREAM,  uri)
                putExtra(Intent.EXTRA_SUBJECT, "Attendance – CS-A – $selectedDate")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Attendance CSV"))
        } catch (e: Exception) {
            Log.e("AttendanceVM", "CSV export failed: ${e.message}", e)
        }
    }

    fun getPresentCount(): Int = _students.count {  it.isPresent }
    fun getAbsentCount():  Int = _students.count { !it.isPresent }
    fun getTotalCount():   Int = _students.size

    private fun getTodayDate() =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    private fun getCurrentTime() =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}