package com.example.edutrackapp.cms.feature.teacher_Module.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.feature.teacher_Module.TeacherProfileState
import com.example.edutrackapp.cms.feature.teacher_Module.TeacherViewModel
import com.example.edutrackapp.cms.ui.navigation.Screen
import java.util.Calendar

// ─── Color Palette ────────────────────────────────────────────────────────────
private val NavyDeep      = Color(0xFF0D1B2A)
private val NavyMid       = Color(0xFF1B2B3E)
private val NavyLight     = Color(0xFF243447)
private val AmberAccent   = Color(0xFFFFC107)
private val AmberLight    = Color(0xFFFFECB3)
private val BgPage        = Color(0xFFF0F4F8)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextDark      = Color(0xFF0D1B2A)
private val TextGray      = Color(0xFF6B7A8D)
private val GreenOnline   = Color(0xFF00E676)

private val ColorAttendance  = Color(0xFF1565C0)
private val ColorTimetable   = Color(0xFF2E7D32)
private val ColorNotices     = Color(0xFF6A1B9A)
private val ColorAssignments = Color(0xFFBF360C)
private val ColorResults     = Color(0xFF00695C)
private val ColorStudents    = Color(0xFF283593)
private val ColorLeave       = Color(0xFF4E342E)
private val ColorReports     = Color(0xFF37474F)

// ─── Greeting ─────────────────────────────────────────────────────────────────
private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 0..11  -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    else      -> "Good Evening"
}

// ─── Data Models ──────────────────────────────────────────────────────────────
data class TeacherFeatureItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val bgColor: Color,
    val badgeCount: Int = 0,
    val route: String = ""
)

data class TeacherStatItem(
    val value: String,
    val label: String,
    val color: Color,
    val icon: ImageVector
)

data class TodayClass(
    val subject: String,
    val time: String,
    val room: String,
    val batch: String,
    val isOngoing: Boolean = false
)

// ─── Entry Point: Handles Loading / Error / Success states ───────────────────
@Composable
fun TeacherDashboardScreen(
    navController: NavController,
    teacherViewModel: TeacherViewModel = viewModel()
) {
    val profileState by teacherViewModel.profileState.collectAsState()

    when (val state = profileState) {

        is TeacherProfileState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgPage),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AmberAccent)
            }
        }

        is TeacherProfileState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgPage),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        is TeacherProfileState.Success -> {
            val profile = state.profile
            TeacherDashboardContent(
                navController = navController,
                teacherName   = profile.name,
                department    = profile.department,
                employeeId    = profile.employeeId,
                subject       = profile.subject
            )
        }
    }
}

// ─── Main Dashboard UI ────────────────────────────────────────────────────────
@Composable
private fun TeacherDashboardContent(
    navController: NavController,
    teacherName: String,
    department: String,
    employeeId: String,
    subject: String
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = EaseInOut), RepeatMode.Reverse
        ), label = "pulseAlpha"
    )

    val stats = listOf(
        TeacherStatItem("6",   "Classes Today",   AmberAccent,       Icons.Default.School),
        TeacherStatItem("142", "Total Students",  Color(0xFF42A5F5), Icons.Default.Groups),
        TeacherStatItem("3",   "Pending Reviews", Color(0xFFEF5350), Icons.Default.PendingActions),
        TeacherStatItem("92%", "Avg Attendance",  Color(0xFF66BB6A), Icons.Default.BarChart)
    )

    val todayClasses = listOf(
        TodayClass("Data Structures", "09:00 – 10:00", "Lab 3",    "CS-A", isOngoing = true),
        TodayClass("Algorithms",      "11:00 – 12:00", "Room 204", "CS-B"),
        TodayClass("DBMS",            "02:00 – 03:00", "Room 101", "CS-C")
    )

    val features = listOf(
        TeacherFeatureItem("Attendance",    "Mark & track",   Icons.Default.HowToReg,     ColorAttendance,  0, Screen.Attendance.route),
        TeacherFeatureItem("Timetable",     "Your schedule",  Icons.Default.CalendarMonth, ColorTimetable,   0, Screen.Timetable.route),
        TeacherFeatureItem("Notices",       "Post & manage",  Icons.Default.Campaign,      ColorNotices,     3, Screen.Notices.route),
        TeacherFeatureItem("Assignments",   "Review & grade", Icons.Default.Assignment,    ColorAssignments, 5, Screen.TeacherAssignmentList.route),
        TeacherFeatureItem("Results",       "Publish marks",  Icons.Default.Grading,       ColorResults,     0, Screen.Results.route),
        TeacherFeatureItem("Students",      "Class roster",   Icons.Default.Groups,        ColorStudents,    0, Screen.Attendance.route),
        TeacherFeatureItem("Leave Request", "Apply leave",    Icons.Default.EventBusy,     ColorLeave,       0, Screen.LeaveRequest.route),
        TeacherFeatureItem("Reports",       "Analytics",      Icons.Default.Insights,      ColorReports,     0, Screen.Results.route)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
            .verticalScroll(scrollState)
    ) {

        // ── 1. Header ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(Brush.linearGradient(listOf(NavyDeep, NavyMid, NavyLight)))
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(AmberAccent.copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-40).dp, y = 40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 26.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Avatar + name — clickable to go to profile
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.TeacherProfile.route)
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(AmberAccent.copy(alpha = 0.25f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(AmberAccent.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Shows first letter of the teacher's real name from Firestore
                                Text(
                                    text = if (teacherName.isNotEmpty())
                                        teacherName.first().uppercaseChar().toString()
                                    else "T",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmberAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "${greeting()} 👋",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            // Real teacher name from Firestore
                            Text(
                                text = teacherName,
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(GreenOnline)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                // Real department + employeeId from Firestore
                                Text(
                                    text = "$department  ·  $employeeId",
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Notification bell
                    Box {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable { navController.navigate(Screen.Notices.route) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(AmberAccent)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3", color = NavyDeep, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subject chip — real subject from Firestore
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AmberAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "📚  Subject: $subject",
                            color = AmberAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // ── 2. Stats Row ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            stats.forEach { stat ->
                StatCard(stat = stat, modifier = Modifier.weight(1f))
            }
        }

        // ── 3. Today's Classes ────────────────────────────────────────────────
        SectionHeader(title = "Today's Classes", actionLabel = "Full Schedule") {
            navController.navigate(Screen.Timetable.route)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            todayClasses.forEach { cls ->
                TodayClassCard(cls = cls, pulseAlpha = pulseAlpha)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── 4. Quick Actions ──────────────────────────────────────────────────
        SectionHeader(title = "Quick Actions", actionLabel = "See all") {}

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            features.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        FeatureCard(
                            item = item,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (item.route.isNotEmpty()) {
                                    navController.navigate(item.route)
                                }
                            }
                        )
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // ── 5. Quick Mark Attendance Banner ───────────────────────────────────
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF0D47A1))))
                .clickable { navController.navigate(Screen.Attendance.route) }
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Mark Attendance",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Uses real subject from Firestore
                    Text(
                        "CS-A  ·  $subject  ·  Now",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HowToReg,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────
@Composable
fun StatCard(stat: TeacherStatItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(stat.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = null,
                    tint = stat.color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stat.value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = stat.color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stat.label,
                fontSize = 9.sp,
                color = TextGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ─── Today Class Card ─────────────────────────────────────────────────────────
@Composable
fun TodayClassCard(cls: TodayClass, pulseAlpha: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (cls.isOngoing) AmberAccent else Color(0xFFB0BEC5))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = cls.subject,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${cls.time}  ·  ${cls.room}  ·  ${cls.batch}",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
            if (cls.isOngoing) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AmberAccent.copy(alpha = pulseAlpha * 0.18f + 0.10f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "● LIVE",
                        color = AmberAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFCFD8DC),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Feature Card ─────────────────────────────────────────────────────────────
@Composable
fun FeatureCard(
    item: TeacherFeatureItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(item.bgColor, item.bgColor.copy(alpha = 0.72f))
                )
            )
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 18.dp, y = 18.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.09f))
        )
        if (item.badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(AmberAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.badgeCount.toString(),
                    color = NavyDeep,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AmberAccent)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextDark
            )
        }
        Text(
            text = actionLabel,
            color = Color(0xFF1565C0),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onAction() }
        )
    }
}