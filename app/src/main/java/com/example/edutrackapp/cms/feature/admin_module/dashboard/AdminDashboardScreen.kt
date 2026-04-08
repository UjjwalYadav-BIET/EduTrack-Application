package com.example.edutrackapp.cms.feature.admin_module.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.edutrackapp.cms.feature.admin_module.manage_users.AdminViewModel
import com.example.edutrackapp.cms.feature.admin_module.manage_users.UserSummary
import com.example.edutrackapp.cms.feature.admin_module.notices.AdminNoticeViewModel  // ← updated
import com.example.edutrackapp.cms.feature.admin_module.notices.NoticeUiState          // ← updated
import com.example.edutrackapp.cms.feature.notices.Notice                              // ← shared model
import com.example.edutrackapp.cms.feature.notices.NoticeAudience                     // ← shared enum
import com.example.edutrackapp.cms.feature.notices.NoticePriority                     // ← shared enum
import com.example.edutrackapp.cms.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

// ─── Brand Palette ────────────────────────────────────────────────────────────
private val Slate900  = Color(0xFF0F172A)
private val Slate800  = Color(0xFF1E293B)
private val Slate700  = Color(0xFF334155)
private val Slate100  = Color(0xFFF1F5F9)
private val Cyan400   = Color(0xFF22D3EE)
private val Cyan500   = Color(0xFF06B6D4)
private val Emerald   = Color(0xFF10B981)
private val Amber     = Color(0xFFF59E0B)
private val Rose      = Color(0xFFF43F5E)
private val Violet    = Color(0xFF8B5CF6)
private val TextWhite = Color(0xFFE2E8F0)
private val TextMuted = Color(0xFF94A3B8)

@Composable
fun AdminDashboardScreen(
    navController  : NavController,
    viewModel      : AdminViewModel      = hiltViewModel(),
    noticeViewModel: AdminNoticeViewModel = hiltViewModel()   // ← updated type
) {
    val totalTeachers by viewModel.totalTeachers.collectAsState()
    val totalStudents by viewModel.totalStudents.collectAsState()
    val teachers      by viewModel.teachers.collectAsState()
    val students      by viewModel.students.collectAsState()
    val notices       by noticeViewModel.notices.collectAsState()   // ← real-time StateFlow

    // 0=Home  1=Teachers  2=Students  3=Notices
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
        // No loadNotices() needed — AdminNoticeViewModel uses real-time Firestore listener
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            AdminHeader(
                totalTeachers = totalTeachers,
                totalStudents = totalStudents,
                totalNotices  = notices.size,
                onLogout = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )

            AdminTabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            when (selectedTab) {
                0 -> HomeTab(
                    navController = navController,
                    totalTeachers = totalTeachers,
                    totalStudents = totalStudents,
                    totalNotices  = notices.size
                )
                1 -> UsersTab(
                    users         = teachers,
                    role          = "Teacher",
                    navController = navController,
                    viewModel     = viewModel
                )
                2 -> UsersTab(
                    users         = students,
                    role          = "Student",
                    navController = navController,
                    viewModel     = viewModel
                )
                3 -> NoticesTab(
                    notices       = notices,
                    navController = navController,
                    viewModel     = noticeViewModel
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// HEADER
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun AdminHeader(
    totalTeachers : Int,
    totalStudents : Int,
    totalNotices  : Int,
    onLogout      : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.linearGradient(listOf(Slate800, Slate900)))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text          = "EduTrack CMS",
                        fontSize      = 11.sp,
                        color         = Cyan400,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text       = "Admin Portal",
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = TextWhite
                    )
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Slate700),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = Cyan400, modifier = Modifier.size(22.dp))
                    }
                    IconButton(
                        onClick  = onLogout,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Rose.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Rose, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill(label = "Faculty",  value = totalTeachers, color = Cyan500, icon = Icons.Default.School)
                StatPill(label = "Students", value = totalStudents, color = Emerald, icon = Icons.Default.People)
                StatPill(label = "Notices",  value = totalNotices,  color = Amber,   icon = Icons.Default.Campaign)
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: Int, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Text(text = "$value $label", fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// TAB ROW
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun AdminTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        Triple("Overview", Icons.Outlined.Dashboard, Cyan400),
        Triple("Teachers", Icons.Outlined.School,    Cyan400),
        Triple("Students", Icons.Outlined.People,    Cyan400),
        Triple("Notices",  Icons.Outlined.Campaign,  Amber)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate800)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, (label, icon, color) ->
            val selected = selectedTab == index
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) color.copy(alpha = 0.15f) else Color.Transparent)
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) color.copy(alpha = 0.4f) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, null, tint = if (selected) color else TextMuted, modifier = Modifier.size(16.dp))
                Text(
                    text       = label,
                    fontSize   = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (selected) color else TextMuted
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// HOME TAB
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun HomeTab(
    navController : NavController,
    totalTeachers : Int,
    totalStudents : Int,
    totalNotices  : Int
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text          = "Quick Actions",
                fontSize      = 13.sp,
                color         = TextMuted,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title    = "Add Teacher",
                    subtitle = "Register new faculty",
                    icon     = Icons.Default.PersonAdd,
                    gradient = listOf(Color(0xFF0EA5E9), Color(0xFF6366F1)),
                    onClick  = { navController.navigate(Screen.AddTeacher.route) }
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title    = "Add Student",
                    subtitle = "Enroll new student",
                    icon     = Icons.Default.School,
                    gradient = listOf(Color(0xFF10B981), Color(0xFF0EA5E9)),
                    onClick  = { navController.navigate(Screen.AddStudent.route) }
                )
            }
        }
        item {
            ActionCard(
                modifier = Modifier.fillMaxWidth(),
                title    = "Post Notice",
                subtitle = "Announce to students or faculty",
                icon     = Icons.Default.Campaign,
                gradient = listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
                onClick  = { navController.navigate(Screen.PostNotice.route) }
            )
        }
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item {
            Text(
                text          = "Institution Stats",
                fontSize      = 13.sp,
                color         = TextMuted,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(label = "Total Faculty Members",      value = totalTeachers,                 icon = Icons.Default.School,    color = Cyan500)
                StatCard(label = "Total Students Enrolled",    value = totalStudents,                 icon = Icons.Default.People,    color = Emerald)
                StatCard(label = "Total Users in System",      value = totalTeachers + totalStudents, icon = Icons.Default.Group,     color = Violet)
                StatCard(label = "Notices Published",          value = totalNotices,                  icon = Icons.Default.Campaign,  color = Amber)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// NOTICES TAB
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun NoticesTab(
    notices       : List<Notice>,
    navController : NavController,
    viewModel     : AdminNoticeViewModel       // ← updated type
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (notices.isEmpty()) {
            Column(
                modifier            = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Campaign, null, tint = TextMuted, modifier = Modifier.size(56.dp))
                Text("No notices posted yet", fontSize = 16.sp, color = TextMuted)
                Text("Tap + to post one",     fontSize = 13.sp, color = TextMuted.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text     = "${notices.size} notice${if (notices.size != 1) "s" else ""} posted",
                        fontSize = 13.sp,
                        color    = TextMuted
                    )
                }
                items(notices, key = { it.id }) { notice ->
                    AdminDashNoticeCard(
                        notice   = notice,
                        onDelete = { viewModel.deleteNotice(notice.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }

        FloatingActionButton(
            onClick        = { navController.navigate(Screen.PostNotice.route) },
            modifier       = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Amber,
            contentColor   = Color.White,
            shape          = CircleShape,
            elevation      = FloatingActionButtonDefaults.elevation(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Post Notice")
        }
    }
}

@Composable
private fun AdminDashNoticeCard(notice: Notice, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Map audience value → color + label using shared NoticeAudience enum
    val (accentColor, audienceLabel) = when (notice.audience) {
        NoticeAudience.TEACHERS.value -> Cyan500 to "Faculty"
        NoticeAudience.STUDENTS.value -> Emerald to "Students"
        else                          -> Violet  to "Everyone"
    }

    // Priority color using shared NoticePriority enum
    val priorityColor = when (notice.priority) {
        NoticePriority.URGENT.value -> Rose
        NoticePriority.LOW.value    -> Emerald
        else                        -> Amber
    }

    val dateFormatted = remember(notice.createdAt) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(notice.createdAt.toDate())
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = Slate800,
            title            = { Text("Delete Notice", color = TextWhite) },
            text             = { Text("Remove \"${notice.title}\"? This cannot be undone.", color = TextMuted) },
            confirmButton    = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = Rose)
                }
            },
            dismissButton    = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Slate800),
        border   = BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header row
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Amber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Campaign, null, tint = Amber, modifier = Modifier.size(22.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        notice.title,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextWhite,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(dateFormatted, fontSize = 11.sp, color = TextMuted)
                }

                // Audience chip
                Text(
                    text       = audienceLabel,
                    fontSize   = 10.sp,
                    color      = accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Priority dot
                androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = priorityColor)
                }

                // Delete button
                IconButton(
                    onClick  = { showDeleteDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Rose.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.DeleteOutline, null, tint = Rose, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate700, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text(notice.message, fontSize = 13.sp, color = TextMuted, lineHeight = 20.sp)

            // Attachment indicator
            if (notice.attachmentUrl.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.AttachFile, null, tint = Cyan500, modifier = Modifier.size(12.dp))
                    Text("Has attachment", fontSize = 11.sp, color = Cyan500)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val roleIcon = if (notice.postedByRole == "admin") Icons.Default.AdminPanelSettings
                else Icons.Default.School
                Icon(roleIcon, null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                Text(
                    "Posted by: ${notice.postedBy}",
                    fontSize = 11.sp,
                    color    = TextMuted.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SHARED COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ActionCard(
    modifier : Modifier = Modifier,
    title    : String,
    subtitle : String,
    icon     : ImageVector,
    gradient : List<Color>,
    onClick  : () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .padding(18.dp)
        ) {
            Column(
                modifier            = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(title,    fontSize = 15.sp, fontWeight = FontWeight.Bold,  color = Color.White)
                    Text(subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Slate800),
        border   = BorderStroke(1.dp, Slate700)
    ) {
        Row(
            modifier              = Modifier.padding(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label,    fontSize = 13.sp, color = TextMuted)
                Text("$value", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
            }
        }
    }
}

@Composable
private fun UsersTab(
    users         : List<UserSummary>,
    role          : String,
    navController : NavController,
    viewModel     : AdminViewModel
) {
    val addRoute = if (role == "Teacher") Screen.AddTeacher.route else Screen.AddStudent.route

    Box(modifier = Modifier.fillMaxSize()) {
        if (users.isEmpty()) {
            Column(
                modifier            = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (role == "Teacher") Icons.Default.School else Icons.Default.People,
                    contentDescription = null,
                    tint     = TextMuted,
                    modifier = Modifier.size(56.dp)
                )
                Text("No ${role}s registered yet", fontSize = 16.sp, color = TextMuted)
                Text("Tap + to add one",           fontSize = 13.sp, color = TextMuted.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "${users.size} ${role}${if (users.size != 1) "s" else ""} registered",
                        fontSize = 13.sp,
                        color    = TextMuted
                    )
                }
                items(users, key = { it.uid }) { user ->
                    UserCard(user = user, onDelete = { viewModel.deleteUser(user.uid) {} })
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }

        FloatingActionButton(
            onClick        = { navController.navigate(addRoute) },
            modifier       = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Cyan500,
            contentColor   = Color.White,
            shape          = CircleShape,
            elevation      = FloatingActionButtonDefaults.elevation(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add $role")
        }
    }
}

@Composable
private fun UserCard(user: UserSummary, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isTeacher   = user.role == "teacher"
    val accentColor = if (isTeacher) Cyan500 else Emerald

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = Slate800,
            title            = { Text("Remove User", color = TextWhite) },
            text             = { Text("Remove ${user.name} from the system? This only deletes the Firestore record.", color = TextMuted) },
            confirmButton    = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Remove", color = Rose)
                }
            },
            dismissButton    = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Slate800),
        border   = BorderStroke(1.dp, Slate700)
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name,  fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(user.email, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniChip(
                        text  = if (isTeacher) user.subject.ifBlank { "Faculty" } else user.enrollmentId.ifBlank { "Student" },
                        color = accentColor
                    )
                    if (user.department.isNotBlank()) MiniChip(text = user.department, color = Violet)
                }
            }
            IconButton(
                onClick  = { showDeleteDialog = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Rose.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.DeleteOutline, null, tint = Rose, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MiniChip(text: String, color: Color) {
    Text(
        text       = text,
        fontSize   = 10.sp,
        color      = color,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}