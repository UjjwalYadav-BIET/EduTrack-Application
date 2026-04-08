package com.example.edutrackapp.cms.ui.navigation

sealed class Screen(val route: String) {

    // ── Auth ──────────────────────────────────────────────────────────────────
    object Splash : Screen("splash")
    object Login  : Screen("login")
    object Otp    : Screen("otp/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp/$phoneNumber"
    }

    // ── Teacher ───────────────────────────────────────────────────────────────
    object TeacherDashboard     : Screen("teacher_dashboard")
    object Attendance           : Screen("attendance")
    object AttendanceHistory    : Screen("attendance_history")
    object FaceScan             : Screen("face_scan")
    object Timetable            : Screen("timetable")
    object CreateAssignment     : Screen("create_assignment")
    object TeacherAssignmentList: Screen("teacher_assignment_list")
    object ViewSubmissions      : Screen("view_submissions/{assignmentId}") {
        fun createRoute(assignmentId: Int) = "view_submissions/$assignmentId"
    }
    object Notices              : Screen("teacher_notices")       // ← list screen (was "create_notice")
    object CreateNotice         : Screen("create_notice")         // ← post screen (new)
    object Results              : Screen("enter_marks")
    object Profile              : Screen("teacher_profile")
    object TeacherProfile       : Screen("teacher_profile_detail")
    object LeaveRequest         : Screen("leave_request")

    // ── Student ───────────────────────────────────────────────────────────────
    object StudentDashboard     : Screen("student_dashboard")
    object StudentAttendance    : Screen("student_attendance")
    object StudentNotices       : Screen("student_notices")
    object StudentResults       : Screen("student_results")
    object StudentAssignments   : Screen("student_assignments")
    object StudentProfile       : Screen("student_profile")
    object StudentTimetable     : Screen("student_timetable")

    // ── Admin ─────────────────────────────────────────────────────────────────
    object AdminDashboard       : Screen("admin_dashboard")
    object AddTeacher           : Screen("add_teacher")
    object AddStudent           : Screen("add_student")
    object PostNotice           : Screen("post_notice")
}