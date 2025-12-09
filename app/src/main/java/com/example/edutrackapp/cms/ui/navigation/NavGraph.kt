package com.example.edutrackapp.cms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.edutrackapp.cms.feature.admin_module.dashboard.AdminDashboardScreen
import com.example.edutrackapp.cms.feature.admin_module.manage_users.AddStudentScreen
import com.example.edutrackapp.cms.feature.admin_module.manage_users.AddTeacherScreen
import com.example.edutrackapp.cms.feature.auth.presentation.LoginScreen
import com.example.edutrackapp.cms.feature.splash.SplashScreen // <--- NEW IMPORT
import com.example.edutrackapp.cms.feature.student_module.assignments.presentation.StudentAssignmentScreen
import com.example.edutrackapp.cms.feature.student_module.attendance.presentation.StudentAttendanceScreen
import com.example.edutrackapp.cms.feature.student_module.dashboard.StudentDashboardScreen
import com.example.edutrackapp.cms.feature.student_module.notices.presentation.StudentNoticeScreen
import com.example.edutrackapp.cms.feature.student_module.profile.presentation.StudentProfileScreen
import com.example.edutrackapp.cms.feature.student_module.results.presentation.StudentResultScreen
import com.example.edutrackapp.cms.feature.student_module.timetable.presentation.StudentTimeTableScreen
import com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation.CreateAssignmentScreen
import com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation.TeacherAssignmentListScreen
import com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation.TeacherSubmissionScreen
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.FaceScanScreen
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.MarkAttendanceScreen
import com.example.edutrackapp.cms.feature.teacher_Module.dashboard.TeacherDashboardScreen
import com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation.CreateNoticeScreen
import com.example.edutrackapp.cms.feature.teacher_Module.profile.presentation.TeacherProfileScreen
import com.example.edutrackapp.cms.feature.teacher_Module.results.presentation.EnterMarksScreen
import com.example.edutrackapp.cms.feature.teacher_Module.timetable.presentation.TimeTableScreen

@Composable
fun EduTrackNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        // FIX: Change Start Destination to Splash
        startDestination = Screen.Splash.route
    ) {

        // --- NEW: SPLASH SCREEN ROUTE ---
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        // --------------------------------

        // Route 1: Login
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    when (role) {
                        "TEACHER" -> {
                            navController.navigate(Screen.TeacherDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        "ADMIN" -> {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        else -> {
                            // "STUDENT" falls here
                            navController.navigate(Screen.StudentDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // Route 2: Teacher Dashboard
        composable(route = Screen.TeacherDashboard.route) {
            TeacherDashboardScreen(navController = navController)
        }
        // Attendance
        composable(route = Screen.Attendance.route) {
            MarkAttendanceScreen(navController = navController)
        }
        // TimeTable
        composable(route = Screen.Timetable.route) {
            TimeTableScreen(navController = navController)
        }
        // Assignment
        composable(route = Screen.CreateAssignment.route) {
            CreateAssignmentScreen(navController = navController)
        }
        // Notices
        composable(route = Screen.Notices.route) {
            CreateNoticeScreen(navController = navController)
        }
        // Result
        composable(route = Screen.Results.route) {
            EnterMarksScreen(navController = navController)
        }
        // Profile
        composable(route = Screen.Profile.route) {
            TeacherProfileScreen(navController = navController)
        }

        // Student Dashboard
        composable(route = Screen.StudentDashboard.route) {
            StudentDashboardScreen(navController = navController)
        }
        // Attendance
        composable(route = Screen.StudentAttendance.route) {
            StudentAttendanceScreen(navController = navController)
        }
        // Notices
        composable(route = Screen.StudentNotices.route) {
            StudentNoticeScreen(navController = navController)
        }
        // Results
        composable(route = Screen.StudentResults.route) {
            StudentResultScreen(navController = navController)
        }
        // Assignments
        composable(route = Screen.StudentAssignments.route) {
            StudentAssignmentScreen(navController = navController)
        }
        // Profile
        composable(route = Screen.StudentProfile.route) {
            StudentProfileScreen(navController = navController)
        }
        // TimeTable
        composable(route = Screen.StudentTimetable.route) {
            StudentTimeTableScreen(navController = navController)
        }

        // Admin Routes
        composable(route = Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
        composable(route = Screen.AddTeacher.route) {
            AddTeacherScreen(navController = navController)
        }
        composable(route = Screen.AddStudent.route) {
            AddStudentScreen(navController = navController)
        }

        // Face Scan Route
        composable(route = Screen.FaceScan.route) {
            FaceScanScreen(navController = navController)
        }

        composable(route = Screen.TeacherAssignmentList.route) {
            TeacherAssignmentListScreen(navController = navController)
        }

        composable(
            route = Screen.ViewSubmissions.route,
            arguments = listOf(navArgument("assignmentId") { type = NavType.IntType })
        ) {
            TeacherSubmissionScreen(navController = navController)
        }
    }
}