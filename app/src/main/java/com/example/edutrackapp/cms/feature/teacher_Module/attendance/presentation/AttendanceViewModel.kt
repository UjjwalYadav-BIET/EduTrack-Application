package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackapp.Domain.Model.attendance.Attendance
import com.example.edutrackapp.Domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject



@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _students = mutableStateListOf<StudentUiModel>()
    val students: List<StudentUiModel> = _students
    private val _subjects = MutableStateFlow<List<SubjectUiModel>>(emptyList())
    val subjects = _subjects.asStateFlow()

    var selectedSubjectId by mutableStateOf(1)
    var selectedLecture by mutableStateOf(1)
    var selectedDate by mutableStateOf(LocalDate.now())

    val loggedInFacultyId = 1
    var selectedSemester by mutableStateOf(5)
    var selectedSection by mutableStateOf("A")
    // Update semester/section and reload students
    fun onSemesterOrSectionChanged(semester: Int, section: String) {
        selectedSemester = semester
        selectedSection = section
        loadStudents(semester, section)
    }


    fun loadStudents(semester: Int, section: String) {
        viewModelScope.launch {
            val studentsFromDb = repository.getStudentsForFaculty(
                facultyId = loggedInFacultyId,
                semester = semester,
                section = section
            )
            _students.clear()
            _students.addAll(
                studentsFromDb.map { StudentUiModel(it.id, it.name, it.rollNo) }
            )
        }
    }

    fun toggleAttendance(studentId: Int) {
        val index = _students.indexOfFirst { it.id == studentId }
        if (index != -1) {
            _students[index] = _students[index].copy(isPresent = !_students[index].isPresent)
        }
    }

    fun markStudentsBasedOnCount(count: Int) {
        for (i in _students.indices) {
            _students[i] = _students[i].copy(isPresent = i < count)
        }
    }
    fun loadSubjects() {
        viewModelScope.launch {
            val domainSubjects = repository.getSubjectsForFaculty(loggedInFacultyId)
            _subjects.value = domainSubjects.map {
                SubjectUiModel(
                    id = it.id,
                    name = it.name,
                    branch = it.branch
                )
            }
        }
    }

    fun submitAttendance() {
        viewModelScope.launch {
            val alreadyTaken = repository.isAttendanceAlreadyTaken(
                facultyId = loggedInFacultyId,
                subjectId = selectedSubjectId,
                date = selectedDate.toEpochDay(),
                lecturePeriod = selectedLecture
            )

            if (alreadyTaken) {
                errorMessage = "Attendance already taken for this lecture ❌"
                return@launch
            }

            // 2️⃣ 60-minute rule
            val canTake = repository.canTakeAttendance(loggedInFacultyId)
            if (!canTake) {
                errorMessage = "You can take attendance only after 60 minutes ⏳"
                return@launch
            }

            // 3️⃣ Insert attendance
            val attendanceList = _students.map { student ->
                Attendance(
                    studentId = student.id,
                    subjectId = selectedSubjectId,
                    facultyId = loggedInFacultyId,
                    date = selectedDate.toEpochDay(),
                    lecturePeriod = selectedLecture,
                    isPresent = student.isPresent
                )
            }

            repository.insertAttendance(attendanceList)
            errorMessage = null
        }
    }
}
