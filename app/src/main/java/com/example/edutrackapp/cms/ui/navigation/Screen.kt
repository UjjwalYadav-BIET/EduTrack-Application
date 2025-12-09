package com.example.edutrackapp.cms.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object TeacherDashboard : Screen("teacher_dashboard")
    object StudentDashboard : Screen("student_dashboard")
    object Attendance : Screen("attendance_screen")
    object Timetable : Screen("timetable_screen")
    object CreateAssignment : Screen("create_assignment_screen")
    object Notices : Screen("notice_screen")
    object Results : Screen("results_screen")
    object Profile : Screen("profile_screen")
    object StudentAttendance : Screen("student_attendance_screen")
    object StudentNotices : Screen("student_view_notices")
    object StudentResults : Screen("student_results_screen")
    object StudentAssignments : Screen("student_assignments_screen")
    object StudentProfile : Screen("student_profile_screen")
    object StudentTimetable : Screen("student_timetable_screen")
    // Admin Routes
    object AdminDashboard : Screen("admin_dashboard")
    object AddTeacher : Screen("add_teacher_screen") // <--- ADD THIS LINE
    object AddStudent : Screen("add_student_screen")
    object FaceScan : Screen("face_scan_screen")
    object Splash : Screen("splash_screen")

    object TeacherAssignmentList : Screen("teacher_assignment_list")
    object ViewSubmissions : Screen("view_submissions/{assignmentId}") {
        fun createRoute(id: Int) = "view_submissions/$id"
    }
}