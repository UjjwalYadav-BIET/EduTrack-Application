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
import com.example.edutrackapp.cms.feature.admin_module.notices.PostNoticeScreen
import com.example.edutrackapp.cms.feature.auth.presentation.LoginScreen
import com.example.edutrackapp.cms.feature.auth.presentation.OtpScreen
import com.example.edutrackapp.cms.feature.splash.SplashScreen
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
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.AttendanceHistoryScreen
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.EnrollFaceScreen
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.FaceScanScreen
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.MarkAttendanceScreen
import com.example.edutrackapp.cms.feature.teacher_Module.dashboard.TeacherDashboardScreen
import com.example.edutrackapp.cms.feature.teacher_Module.leave.TeacherLeaveRequestScreen
import com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation.CreateNoticeScreen
import com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation.TeacherNoticesListScreen
import com.example.edutrackapp.cms.feature.teacher_Module.profile.presentation.TeacherProfileScreen
import com.example.edutrackapp.cms.feature.teacher_Module.results.presentation.EnterMarksScreen
import com.example.edutrackapp.cms.feature.teacher_Module.timetable.presentation.TimeTableScreen

@Composable
fun EduTrackNavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route
    ) {

        // ── Splash ────────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        // ── Login ─────────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { result ->
                    when {
                        result.startsWith("otp:") -> {
                            val phone = result.removePrefix("otp:")
                            navController.navigate(Screen.Otp.createRoute(phone))
                        }
                        result.equals("teacher", ignoreCase = true) ->
                            navController.navigate(Screen.TeacherDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        result.equals("admin", ignoreCase = true) ->
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        result.equals("student", ignoreCase = true) ->
                            navController.navigate(Screen.StudentDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        else ->
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                    }
                }
            )
        }

        // ── OTP ───────────────────────────────────────────────────────────────
        composable(
            route     = Screen.Otp.route,
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpScreen(
                phoneNumber = phoneNumber,
                onVerified  = { role ->
                    when {
                        role.equals("teacher", ignoreCase = true) ->
                            navController.navigate(Screen.TeacherDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        role.equals("admin", ignoreCase = true) ->
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        else ->
                            navController.navigate(Screen.StudentDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Teacher ───────────────────────────────────────────────────────────

        composable(Screen.TeacherDashboard.route) {
            TeacherDashboardScreen(navController = navController)
        }

        // Mark Attendance — receives classId
        composable(
            route     = Screen.Attendance.route,
            arguments = listOf(navArgument("classId") { type = NavType.StringType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: "CS-A"
            MarkAttendanceScreen(navController = navController, classId = classId)
        }

        composable(Screen.AttendanceHistory.route) {
            AttendanceHistoryScreen(navController = navController)
        }

        // ✅ FaceScan — receives classId so it loads the right enrolled faces
        composable(
            route     = Screen.FaceScan.route,
            arguments = listOf(navArgument("classId") { type = NavType.StringType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: "CS-A"
            FaceScanScreen(navController = navController, classId = classId)
        }

        // ✅ NEW — EnrollFace — receives classId
        composable(
            route     = Screen.EnrollFace.route,
            arguments = listOf(navArgument("classId") { type = NavType.StringType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: "CS-A"
            EnrollFaceScreen(navController = navController, classId = classId)
        }

        composable(Screen.Timetable.route) {
            TimeTableScreen(navController = navController)
        }
        composable(Screen.CreateAssignment.route) {
            CreateAssignmentScreen(navController = navController)
        }
        composable(Screen.Notices.route) {
            TeacherNoticesListScreen(navController = navController)
        }
        composable(Screen.CreateNotice.route) {
            CreateNoticeScreen(navController = navController)
        }
        composable(Screen.Results.route) {
            EnterMarksScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            TeacherProfileScreen(navController = navController)
        }
        composable(Screen.TeacherProfile.route) {
            TeacherProfileScreen(navController = navController)
        }
        composable(Screen.LeaveRequest.route) {
            TeacherLeaveRequestScreen(navController = navController)
        }
        composable(Screen.TeacherAssignmentList.route) {
            TeacherAssignmentListScreen(navController = navController)
        }
        composable(
            route     = Screen.ViewSubmissions.route,
            arguments = listOf(navArgument("assignmentId") { type = NavType.IntType })
        ) {
            TeacherSubmissionScreen(navController = navController)
        }

        // ── Student ───────────────────────────────────────────────────────────

        composable(Screen.StudentDashboard.route) {
            StudentDashboardScreen(navController = navController)
        }
        composable(Screen.StudentAttendance.route) {
            StudentAttendanceScreen(navController = navController)
        }
        composable(Screen.StudentNotices.route) {
            StudentNoticeScreen(navController = navController)
        }
        composable(Screen.StudentResults.route) {
            StudentResultScreen(navController = navController)
        }
        composable(Screen.StudentAssignments.route) {
            StudentAssignmentScreen(navController = navController)
        }
        composable(Screen.StudentProfile.route) {
            StudentProfileScreen(navController = navController)
        }
        composable(Screen.StudentTimetable.route) {
            StudentTimeTableScreen(navController = navController)
        }

        // ── Admin ─────────────────────────────────────────────────────────────

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
        composable(Screen.AddTeacher.route) {
            AddTeacherScreen(navController = navController)
        }
        composable(Screen.AddStudent.route) {
            AddStudentScreen(navController = navController)
        }
        composable(Screen.PostNotice.route) {
            PostNoticeScreen(navController = navController)
        }
    }
}