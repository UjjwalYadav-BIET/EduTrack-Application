package com.example.edutrackapp.cms.feature.teacher_Module.attendance.data

import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.AttendanceRecord
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.StudentUiModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class EnrolledFace(
    val studentName: String = "",
    val embedding: List<Float> = emptyList()
)

data class StudentAttendanceRecord(
    val studentName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "present"
)

@Singleton
class AttendanceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // ── Enrollment ──────────────────────────────────────────────────────────

    suspend fun enrollStudent(
        classId: String,
        studentName: String,
        embedding: FloatArray
    ) {
        val data = mapOf(
            "studentName" to studentName,
            "embedding"   to embedding.toList()
        )
        firestore
            .collection("classes")
            .document(classId)
            .collection("enrolled_faces")
            .document(studentName)
            .set(data)
            .await()
    }

    // ── Fetch enrolled faces ────────────────────────────────────────────────

    suspend fun getEnrolledFaces(classId: String): List<EnrolledFace> {
        val snapshot = firestore
            .collection("classes")
            .document(classId)
            .collection("enrolled_faces")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(EnrolledFace::class.java)
        }
    }

    // ── Student roster ──────────────────────────────────────────────────────

    /**
     * ✅ FIXED: Now queries the top-level "users" collection filtering by
     * role = "student", instead of the non-existent "classes/{id}/students"
     * sub-collection.
     *
     * Firestore document fields used:
     *   - role        : String  ("student")
     *   - name        : String
     *   - rollNo      : String
     *   - department  : String  (optional filter — pass non-null to narrow by dept)
     */
    suspend fun getStudents(
        classId: String,
        department: String? = null          // e.g. "CSE" — pass null to load all students
    ): List<StudentUiModel> {
        android.util.Log.d("REPO_DEBUG", "Querying users collection | role=student | dept=$department")

        var query = firestore
            .collection("users")
            .whereEqualTo("role", "student")

        // Optionally narrow by department so each teacher only sees their class
        if (!department.isNullOrBlank()) {
            query = query.whereEqualTo("department", department)
        }

        val snapshot = query.get().await()

        android.util.Log.d("REPO_DEBUG", "Documents found: ${snapshot.documents.size}")

        snapshot.documents.forEach { doc ->
            android.util.Log.d("REPO_DEBUG", "Doc ID: ${doc.id}, Data: ${doc.data}")
        }

        return snapshot.documents.mapNotNull { doc ->
            val id     = doc.id
            val name   = doc.getString("name")   ?: return@mapNotNull null
            val rollNo = doc.getString("rollNo") ?: ""
            StudentUiModel(id = id, name = name, rollNo = rollNo, isPresent = false)
        }
    }

    // ── Attendance ──────────────────────────────────────────────────────────

    suspend fun markPresent(
        classId: String,
        sessionId: String,
        studentName: String
    ) {
        val record = StudentAttendanceRecord(
            studentName = studentName,
            timestamp   = System.currentTimeMillis(),
            status      = "present"
        )
        firestore
            .collection("classes")
            .document(classId)
            .collection("sessions")
            .document(sessionId)
            .collection("attendance")
            .document(studentName)
            .set(record)
            .await()
    }

    /**
     * Saves a full attendance record for a date/time session.
     */
    suspend fun saveAttendance(
        date: String,
        time: String,
        students: List<StudentUiModel>,
        classId: String = "CS-A"
    ) {
        val sessionId = "${date.replace("/", "-")}_${time.replace(":", "-")}"
        val sessionRef = firestore
            .collection("classes")
            .document(classId)
            .collection("sessions")
            .document(sessionId)

        sessionRef.set(
            mapOf(
                "date"      to date,
                "time"      to time,
                "className" to classId,
                "savedAt"   to System.currentTimeMillis(),
                "total"     to students.size,
                "present"   to students.count { it.isPresent },
                "absent"    to students.count { !it.isPresent }
            )
        ).await()

        val batch = firestore.batch()
        students.forEach { student ->
            val docRef = sessionRef
                .collection("attendance")
                .document(student.id)
            batch.set(
                docRef,
                mapOf(
                    "studentId"   to student.id,
                    "studentName" to student.name,
                    "rollNo"      to student.rollNo,
                    "status"      to if (student.isPresent) "present" else "absent",
                    "timestamp"   to System.currentTimeMillis()
                )
            )
        }
        batch.commit().await()
    }

    suspend fun getSessionAttendance(
        classId: String,
        sessionId: String
    ): List<StudentAttendanceRecord> {
        val snapshot = firestore
            .collection("classes")
            .document(classId)
            .collection("sessions")
            .document(sessionId)
            .collection("attendance")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(StudentAttendanceRecord::class.java)
        }
    }

    suspend fun getAttendanceHistory(classId: String): List<AttendanceRecord> {
        return try {
            val snapshot = firestore
                .collection("classes")
                .document(classId)
                .collection("sessions")
                .orderBy("savedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                AttendanceRecord(
                    id               = doc.id,
                    date             = doc.getString("date")      ?: return@mapNotNull null,
                    time             = doc.getString("time")      ?: return@mapNotNull null,
                    className        = doc.getString("className") ?: classId,
                    presentCount     = (doc.getLong("present")    ?: 0L).toInt(),
                    totalCount       = (doc.getLong("total")      ?: 0L).toInt(),
                    studentSnapshots = emptyList()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createSession(classId: String, sessionId: String) {
        firestore
            .collection("classes")
            .document(classId)
            .collection("sessions")
            .document(sessionId)
            .set(mapOf("startedAt" to System.currentTimeMillis()))
            .await()
    }

    suspend fun closeSession(classId: String, sessionId: String) {
        firestore
            .collection("classes")
            .document(classId)
            .collection("sessions")
            .document(sessionId)
            .update("closedAt", System.currentTimeMillis())
            .await()
    }
}