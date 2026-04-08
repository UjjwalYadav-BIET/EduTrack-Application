package com.example.edutrackapp.cms.feature.admin_module.manage_users

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────
sealed class AdminUiState {
    object Idle    : AdminUiState()
    object Loading : AdminUiState()
    data class Success(val message: String) : AdminUiState()
    data class Error(val message: String)   : AdminUiState()
}

// ─── User summary for listing ─────────────────────────────────────────────────
data class UserSummary(
    val uid          : String = "",
    val name         : String = "",
    val email        : String = "",
    val role         : String = "",
    val department   : String = "",
    val enrollmentId : String = "",
    val rollNo       : String = "",   // ← added
    val employeeId   : String = "",
    val subject      : String = "",
    val phone        : String = ""
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val firebaseAuth : FirebaseAuth,
    private val firestore    : FirebaseFirestore
) : ViewModel() {

    // ─── UI State ──────────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState

    // ─── User lists ────────────────────────────────────────────────────────────
    private val _teachers = MutableStateFlow<List<UserSummary>>(emptyList())
    val teachers: StateFlow<List<UserSummary>> = _teachers

    private val _students = MutableStateFlow<List<UserSummary>>(emptyList())
    val students: StateFlow<List<UserSummary>> = _students

    // ─── Stats ─────────────────────────────────────────────────────────────────
    private val _totalTeachers = MutableStateFlow(0)
    val totalTeachers: StateFlow<Int> = _totalTeachers

    private val _totalStudents = MutableStateFlow(0)
    val totalStudents: StateFlow<Int> = _totalStudents

    // ══════════════════════════════════════════════════════════════════════════
    // TEACHER FORM FIELDS
    // ══════════════════════════════════════════════════════════════════════════
    var name       = mutableStateOf("")
    var email      = mutableStateOf("")
    var password   = mutableStateOf("")
    var phone      = mutableStateOf("")
    var department = mutableStateOf("")
    var employeeId = mutableStateOf("")
    var subject    = mutableStateOf("")

    fun onNameChange(v: String)    { name.value       = v }
    fun onEmailChange(v: String)   { email.value      = v }
    fun onPassChange(v: String)    { password.value   = v }
    fun onPhoneChange(v: String)   { phone.value      = v }
    fun onDeptChange(v: String)    { department.value = v }
    fun onEmpIdChange(v: String)   { employeeId.value = v }
    fun onSubjectChange(v: String) { subject.value    = v }

    // ══════════════════════════════════════════════════════════════════════════
    // STUDENT FORM FIELDS
    // ══════════════════════════════════════════════════════════════════════════
    var studentName         = mutableStateOf("")
    var studentEmail        = mutableStateOf("")
    var studentPhone        = mutableStateOf("")
    var studentPassword     = mutableStateOf("")
    var studentDepartment   = mutableStateOf("")
    var studentEnrollmentId = mutableStateOf("")
    var studentRollNo       = mutableStateOf("")

    fun onStNameChange(v: String)       { studentName.value         = v }
    fun onStEmailChange(v: String)      { studentEmail.value        = v }
    fun onStPhoneChange(v: String)      { studentPhone.value        = v }
    fun onStPassChange(v: String)       { studentPassword.value     = v }
    fun onStDeptChange(v: String)       { studentDepartment.value   = v }
    fun onStEnrollmentChange(v: String) { studentEnrollmentId.value = v }
    fun onStRollChange(v: String)       { studentRollNo.value       = v }

    // ══════════════════════════════════════════════════════════════════════════
    // CREATE TEACHER
    // ══════════════════════════════════════════════════════════════════════════
    fun createTeacherAccount(onSuccess: () -> Unit) {
        if (!validateTeacherFields()) return

        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val result = firebaseAuth
                    .createUserWithEmailAndPassword(email.value.trim(), password.value.trim())
                    .await()
                val uid = result.user?.uid
                    ?: throw Exception("UID missing after account creation.")

                val doc = mapOf(
                    "name"         to name.value.trim(),
                    "email"        to email.value.trim(),
                    "phone"        to phone.value.trim(),
                    "role"         to "teacher",
                    "department"   to department.value.trim(),
                    "employeeId"   to employeeId.value.trim(),
                    "subject"      to subject.value.trim(),
                    "enrollmentId" to "",
                    "rollNo"       to "",
                    "createdAt"    to Timestamp.now()
                )
                firestore.collection("users").document(uid).set(doc).await()

                _uiState.value = AdminUiState.Success("Teacher account created successfully!")
                clearTeacherFields()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(
                    when {
                        e.message?.contains("email address is already") == true ->
                            "This email is already registered."
                        e.message?.contains("password") == true ->
                            "Password must be at least 6 characters."
                        else -> e.message ?: "Failed to create teacher account."
                    }
                )
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CREATE STUDENT
    // ══════════════════════════════════════════════════════════════════════════
    fun createStudentAccount(onSuccess: () -> Unit) {
        if (!validateStudentFields()) return

        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val result = firebaseAuth
                    .createUserWithEmailAndPassword(
                        studentEmail.value.trim(),
                        studentPassword.value.trim()
                    ).await()
                val uid = result.user?.uid
                    ?: throw Exception("UID missing after account creation.")

                val doc = mapOf(
                    "name"         to studentName.value.trim(),
                    "email"        to studentEmail.value.trim(),
                    "phone"        to studentPhone.value.trim(),
                    "role"         to "student",
                    "department"   to studentDepartment.value.trim(),
                    "enrollmentId" to studentEnrollmentId.value.trim(),
                    "rollNo"       to studentRollNo.value.trim(),  // ← WAS MISSING
                    "employeeId"   to "",
                    "subject"      to "",
                    "createdAt"    to Timestamp.now()
                )
                firestore.collection("users").document(uid).set(doc).await()

                _uiState.value = AdminUiState.Success("Student account created successfully!")
                clearStudentFields()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(
                    when {
                        e.message?.contains("email address is already") == true ->
                            "This email is already registered."
                        else -> e.message ?: "Failed to create student account."
                    }
                )
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DELETE USER
    // ══════════════════════════════════════════════════════════════════════════
    fun deleteUser(uid: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                firestore.collection("users").document(uid).delete().await()
                _uiState.value = AdminUiState.Success("User removed.")
                loadUsers()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Delete failed.")
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LOAD ALL USERS
    // ══════════════════════════════════════════════════════════════════════════
    fun loadUsers() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").get().await()
                val allUsers = snapshot.documents.mapNotNull { doc ->
                    UserSummary(
                        uid          = doc.id,
                        name         = doc.getString("name")         ?: "",
                        email        = doc.getString("email")        ?: "",
                        role         = doc.getString("role")         ?: "",
                        department   = doc.getString("department")   ?: "",
                        enrollmentId = doc.getString("enrollmentId") ?: "",
                        rollNo       = doc.getString("rollNo")       ?: "",  // ← added
                        employeeId   = doc.getString("employeeId")   ?: "",
                        subject      = doc.getString("subject")      ?: "",
                        phone        = doc.getString("phone")        ?: ""
                    )
                }
                _teachers.value      = allUsers.filter { it.role == "teacher" }
                _students.value      = allUsers.filter { it.role == "student" }
                _totalTeachers.value = _teachers.value.size
                _totalStudents.value = _students.value.size
            } catch (_: Exception) { }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RESET STATE
    // ══════════════════════════════════════════════════════════════════════════
    fun resetState() { _uiState.value = AdminUiState.Idle }

    // ─── Validation ────────────────────────────────────────────────────────────
    private fun validateTeacherFields(): Boolean {
        return when {
            name.value.isBlank()       -> err("Name cannot be empty.")
            email.value.isBlank()      -> err("Email cannot be empty.")
            password.value.length < 6  -> err("Password must be at least 6 characters.")
            department.value.isBlank() -> err("Department cannot be empty.")
            employeeId.value.isBlank() -> err("Employee ID cannot be empty.")
            subject.value.isBlank()    -> err("Subject cannot be empty.")
            else -> true
        }
    }

    private fun validateStudentFields(): Boolean {
        return when {
            studentName.value.isBlank()         -> err("Name cannot be empty.")
            studentEmail.value.isBlank()        -> err("Email cannot be empty.")
            studentPassword.value.length < 6    -> err("Password must be at least 6 characters.")
            studentDepartment.value.isBlank()   -> err("Department cannot be empty.")
            studentEnrollmentId.value.isBlank() -> err("Enrollment ID cannot be empty.")
            studentRollNo.value.isBlank()       -> err("Roll Number cannot be empty.")  // ← added
            else -> true
        }
    }

    private fun err(msg: String): Boolean {
        _uiState.value = AdminUiState.Error(msg)
        return false
    }

    // ─── Clear fields ──────────────────────────────────────────────────────────
    private fun clearTeacherFields() {
        name.value = ""; email.value = ""; password.value = ""
        phone.value = ""; department.value = ""
        employeeId.value = ""; subject.value = ""
    }

    private fun clearStudentFields() {
        studentName.value = ""; studentEmail.value = ""; studentPhone.value = ""
        studentPassword.value = ""; studentDepartment.value = ""
        studentEnrollmentId.value = ""; studentRollNo.value = ""
    }
}