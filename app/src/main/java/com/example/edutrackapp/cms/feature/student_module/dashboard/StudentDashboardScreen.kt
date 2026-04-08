package com.example.edutrackapp.cms.feature.student_module.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.ui.navigation.Screen

// ─── Color Palette ─────────────────────────────────────────────────────────────
private val TealPrimary   = Color(0xFF009688)
private val TealDark      = Color(0xFF00695C)
private val TealLight     = Color(0xFF4DB6AC)
private val TealSurface   = Color(0xFFF0FAFA)
private val CardBlue      = Color(0xFF1565C0)
private val CardPink      = Color(0xFFC2185B)
private val TextPrimary   = Color(0xFF102027)
private val TextSecondary = Color(0xFF546E7A)
private val SurfaceWhite  = Color(0xFFFFFFFF)
private val DividerColor  = Color(0xFFECEFF1)

@Composable
fun StudentDashboardScreen(
    navController: NavController,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val profile     by viewModel.profile.collectAsState()
    val attendance  by viewModel.attendancePercent.collectAsState()
    val cgpa        by viewModel.cgpa.collectAsState()
    val noticeCount by viewModel.noticeCount.collectAsState()

    val studentName = profile.name.ifBlank { "Student" }

    val scrollState = rememberScrollState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TealSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            // ── HEADER ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(TealPrimary, TealDark, Color(0xFF004D40))
                            )
                        )
                ) {
                    // Decorative circles
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 40.dp, y = (-20).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
                    )
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = (-20).dp, y = 20.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    )

                    Column(modifier = Modifier.padding(start = 24.dp, top = 40.dp, end = 24.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier.fillMaxWidth()
                        ) {
                            // Avatar with pulse ring
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(66.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = pulseAlpha * 0.2f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.35f),
                                                    Color.White.copy(alpha = 0.15f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = studentName.firstOrNull()?.uppercaseChar()?.toString() ?: "S",
                                        fontSize   = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color      = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text          = "Good Morning 👋",
                                    color         = Color.White.copy(alpha = 0.75f),
                                    fontSize      = 12.sp,
                                    fontWeight    = FontWeight.Medium,
                                    letterSpacing = 0.3.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text       = studentName,
                                    color      = Color.White,
                                    fontSize   = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(TealLight)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        // Real department + enrollment ID from Firestore
                                        text  = "${profile.department.ifBlank { "Dept" }}  ·  ${profile.enrollmentId.ifBlank { "N/A" }}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Notification bell
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .clickable { navController.navigate(Screen.StudentNotices.route) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint               = Color.White,
                                    modifier           = Modifier.size(22.dp)
                                )
                                if (noticeCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFEB3B))
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-4).dp, y = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Floating Stats Card ─────────────────────────────────────────
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp)
                        .shadow(
                            elevation    = 16.dp,
                            shape        = RoundedCornerShape(20.dp),
                            ambientColor = TealPrimary.copy(alpha = 0.2f),
                            spotColor    = TealPrimary.copy(alpha = 0.2f)
                        ),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Real attendance from Firestore
                        StatItem(value = attendance, label = "Attendance", color = TealPrimary)
                        StatDivider()
                        // Real CGPA from Firestore results
                        StatItem(value = cgpa, label = "CGPA", color = CardBlue)
                        StatDivider()
                        // Real notice count from Firestore
                        StatItem(
                            value = "$noticeCount",
                            label = "Notices",
                            color = CardPink,
                            badge = noticeCount > 0
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section Title ───────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(500, delayMillis = 200)) +
                        slideInHorizontally(tween(500, delayMillis = 200))
            ) {
                Row(
                    modifier          = Modifier
                        .padding(horizontal = 22.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                brush = Brush.verticalGradient(listOf(TealPrimary, TealLight))
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text       = "Quick Access",
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text       = "See all",
                        fontSize   = 12.sp,
                        color      = TealPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Menu Grid ───────────────────────────────────────────────────────
            val menuItems = listOf(
                StudentMenuItem(
                    "My Attendance", Icons.Default.DateRange,
                    listOf(TealPrimary, TealDark), "Track daily presence"
                ),
                StudentMenuItem(
                    "My Results", Icons.Default.BarChart,
                    listOf(Color(0xFF1565C0), Color(0xFF0D47A1)), "Grades & marks"
                ),
                StudentMenuItem(
                    "Timetable", Icons.Default.Schedule,
                    listOf(Color(0xFFE65100), Color(0xFFBF360C)), "Class schedule"
                ),
                StudentMenuItem(
                    "Notices", Icons.Default.Campaign,
                    listOf(Color(0xFFC2185B), Color(0xFF880E4F)),
                    // Real notice count as subtitle
                    if (noticeCount > 0) "$noticeCount new updates" else "No new notices"
                ),
                StudentMenuItem(
                    "Assignments", Icons.Default.Assignment,
                    listOf(Color(0xFF512DA8), Color(0xFF311B92)), "Pending tasks"
                ),
                StudentMenuItem(
                    "Profile", Icons.Default.Person,
                    listOf(Color(0xFF37474F), Color(0xFF263238)), "Your details"
                )
            )

            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(600, delayMillis = 300)) +
                        slideInVertically(tween(600, delayMillis = 300)) { 40 }
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    menuItems.chunked(2).forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { item ->
                                StudentMenuCard(
                                    item     = item,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    when (item.title) {
                                        "My Attendance" -> navController.navigate(Screen.StudentAttendance.route)
                                        "Timetable"     -> navController.navigate(Screen.StudentTimetable.route)
                                        "Notices"       -> navController.navigate(Screen.StudentNotices.route)
                                        "My Results"    -> navController.navigate(Screen.StudentResults.route)
                                        "Assignments"   -> navController.navigate(Screen.StudentAssignments.route)
                                        "Profile"       -> navController.navigate(Screen.StudentProfile.route)
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ─── Stat Item ────────────────────────────────────────────────────────────────
@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color,
    badge: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text       = value,
                fontSize   = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = color
            )
            if (badge) {
                Box(
                    modifier = Modifier
                        .padding(start = 3.dp, top = 3.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252))
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text       = label,
            fontSize   = 11.sp,
            color      = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Stat Divider ─────────────────────────────────────────────────────────────
@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(DividerColor)
    )
}

// ─── Menu Card ────────────────────────────────────────────────────────────────
@Composable
fun StudentMenuCard(
    item    : StudentMenuItem,
    modifier: Modifier = Modifier,
    onClick : () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .shadow(
                elevation    = 6.dp,
                shape        = RoundedCornerShape(20.dp),
                ambientColor = item.gradientColors.first().copy(alpha = 0.25f),
                spotColor    = item.gradientColors.first().copy(alpha = 0.25f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = LocalIndication.current
            ) { onClick() },
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.linearGradient(colors = item.gradientColors))
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier         = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = item.title,
                        tint               = Color.White,
                        modifier           = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text       = item.title,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text     = item.subtitle,
                        fontSize = 11.sp,
                        color    = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─── Data Model ───────────────────────────────────────────────────────────────
data class StudentMenuItem(
    val title         : String,
    val icon          : ImageVector,
    val gradientColors: List<Color>,
    val subtitle      : String = ""
)