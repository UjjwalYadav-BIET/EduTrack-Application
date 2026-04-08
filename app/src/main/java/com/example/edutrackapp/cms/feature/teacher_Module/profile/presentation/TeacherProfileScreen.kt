package com.example.edutrackapp.cms.feature.teacher_Module.profile.presentation

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.feature.teacher_Module.TeacherProfileState
import com.example.edutrackapp.cms.feature.teacher_Module.TeacherViewModel
import com.example.edutrackapp.cms.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

// ─── Design Tokens ────────────────────────────────────────────────────────────
private val DarkNavy     = Color(0xFF1B2438)
private val NavyMid      = Color(0xFF243047)
private val BgLight      = Color(0xFFF5F6FA)
private val CardWhite    = Color(0xFFFFFFFF)
private val AccentYellow = Color(0xFFFFB800)
private val GreenOnline  = Color(0xFF2ECC71)
private val RedAbsent    = Color(0xFFE74C3C)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8A9A)
private val DividerColor = Color(0xFFEEEEEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    navController: NavController,
    teacherViewModel: TeacherViewModel = viewModel()   // ← shared ViewModel
) {
    val profileState by teacherViewModel.profileState.collectAsState()

    var showLogoutDialog     by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled      by remember { mutableStateOf(false) }

    // ── Logout confirmation dialog ─────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint     = RedAbsent,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Logout?", fontWeight = FontWeight.Bold, color = TextDark)
            },
            text = {
                Text(
                    "Are you sure you want to logout from EduTrack?",
                    color     = TextGray,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        FirebaseAuth.getInstance().signOut()   // ← sign out from Firebase
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAbsent),
                    shape  = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, Logout", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape   = RoundedCornerShape(10.dp),
                    border  = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                ) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = CardWhite,
            shape          = RoundedCornerShape(20.dp)
        )
    }

    // ── Resolve profile data (or fallback while loading) ───────────────────
    val profile = when (val s = profileState) {
        is TeacherProfileState.Success -> s.profile
        else                           -> null
    }

    val displayName   = profile?.name       ?: "Loading..."
    val displayDept   = profile?.department ?: "..."
    val displayEmpId  = profile?.employeeId ?: "..."
    val displayEmail  = profile?.email      ?: "..."
    val displayPhone  = profile?.phone      ?: "..."
    val displaySub    = profile?.subject    ?: "..."
    val avatarLetter  = if (displayName.isNotEmpty() && displayName != "Loading...")
        displayName.first().uppercaseChar().toString()
    else "T"

    Scaffold(
        containerColor = BgLight,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy)
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "My Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = Color.White
                            )
                            // Real department + employeeId from Firestore
                            Text(
                                "$displayDept  •  $displayEmpId",
                                fontSize = 11.sp,
                                color    = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* edit profile */ }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = AccentYellow
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── 1. Hero Header ──────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkNavy)
                        .background(
                            BgLight,
                            RoundedCornerShape(
                                topStart    = 0.dp, topEnd    = 0.dp,
                                bottomStart = 28.dp, bottomEnd = 28.dp
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(listOf(DarkNavy, NavyMid)),
                                RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.padding(bottom = 28.dp)
                        ) {
                            // Avatar — shows real first letter of teacher's name
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(AccentYellow.copy(alpha = 0.15f))
                                    .border(3.dp, AccentYellow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileState is TeacherProfileState.Loading) {
                                    CircularProgressIndicator(
                                        color  = AccentYellow,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        avatarLetter,
                                        fontSize   = 34.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = AccentYellow
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Real teacher name from Firestore
                            Text(
                                displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 20.sp,
                                color      = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Online status pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier          = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.10f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(GreenOnline, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Real department from Firestore
                                Text(
                                    "Professor  •  $displayDept Dept.",
                                    fontSize = 12.sp,
                                    color    = Color.White.copy(alpha = 0.80f)
                                )
                            }
                        }
                    }

                    // Floating stats strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 28.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardWhite)
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        ProfileStat("6",   "Classes\nToday",    AccentYellow)
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(DividerColor))
                        ProfileStat("142", "Total\nStudents",   Color(0xFF3498DB))
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(DividerColor))
                        ProfileStat("92%", "Avg\nAttendance",   GreenOnline)
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(DividerColor))
                        ProfileStat("12",  "Years\nExp.",        Color(0xFF9B59B6))
                    }
                }

                Spacer(modifier = Modifier.height(44.dp))
            }

            // ── 2. Personal Info Card ───────────────────────────────────────
            item {
                SectionHeader("Personal Information")
                ProfileCard {
                    // Real employeeId from Firestore
                    ProfileInfoRow(Icons.Default.Person,     "Teacher ID",   displayEmpId,  AccentYellow)
                    ProfileDivider()
                    // Real email from Firestore
                    ProfileInfoRow(Icons.Default.Email,      "Email",        displayEmail,  AccentYellow)
                    ProfileDivider()
                    // Real phone from Firestore
                    ProfileInfoRow(Icons.Default.Phone,      "Phone",        displayPhone.ifEmpty { "Not set" }, AccentYellow)
                    ProfileDivider()
                    ProfileInfoRow(Icons.Default.School,     "Designation",  "Professor",   AccentYellow)
                    ProfileDivider()
                    // Real department from Firestore
                    ProfileInfoRow(Icons.Default.LocationOn, "Department",   displayDept,   AccentYellow)
                }
            }

            // ── 3. Academic Info Card ───────────────────────────────────────
            item {
                SectionHeader("Academic Details")
                ProfileCard {
                    // Real subject from Firestore
                    ProfileInfoRow(Icons.Default.MenuBook,      "Subject",       displaySub,                AccentYellow.copy(alpha = 0f).let { Color(0xFF3498DB) })
                    ProfileDivider()
                    ProfileInfoRow(Icons.Default.Group,         "Classes",        "CS-A, CS-B, CS-C",        Color(0xFF3498DB))
                    ProfileDivider()
                    ProfileInfoRow(Icons.Default.CalendarToday, "Joined",         "August 2013",             Color(0xFF3498DB))
                    ProfileDivider()
                    ProfileInfoRow(Icons.Default.EmojiEvents,   "Qualification",  "Ph.D. Computer Science",  Color(0xFF3498DB))
                }
            }

            // ── 4. Quick Actions Card ───────────────────────────────────────
            item {
                SectionHeader("Quick Actions")
                ProfileCard {
                    QuickActionRow(
                        icon    = Icons.Default.History,
                        label   = "Attendance History",
                        color   = GreenOnline,
                        onClick = { navController.navigate(Screen.AttendanceHistory.route) }
                    )
                    ProfileDivider()
                    QuickActionRow(
                        icon    = Icons.Default.FileDownload,
                        label   = "Export My Data (CSV)",
                        color   = Color(0xFF3498DB),
                        onClick = { /* trigger export */ }
                    )
                    ProfileDivider()
                    QuickActionRow(
                        icon    = Icons.Default.Share,
                        label   = "Share Profile",
                        color   = Color(0xFF9B59B6),
                        onClick = { /* share */ }
                    )
                }
            }

            // ── 5. Settings Card ───────────────────────────────────────────
            item {
                SectionHeader("Settings")
                ProfileCard {
                    // Notifications toggle
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(AccentYellow.copy(0.12f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint     = AccentYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Push Notifications", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                            Text("Alerts for class & attendance", fontSize = 12.sp, color = TextGray)
                        }
                        Switch(
                            checked         = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor   = CardWhite,
                                checkedTrackColor   = AccentYellow,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = DividerColor
                            )
                        )
                    }

                    ProfileDivider()

                    // Dark mode toggle
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF9B59B6).copy(0.12f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DarkMode,
                                contentDescription = null,
                                tint     = Color(0xFF9B59B6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                            Text("Switch app appearance", fontSize = 12.sp, color = TextGray)
                        }
                        Switch(
                            checked         = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor   = CardWhite,
                                checkedTrackColor   = Color(0xFF9B59B6),
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = DividerColor
                            )
                        )
                    }

                    ProfileDivider()

                    QuickActionRow(
                        icon    = Icons.Default.Lock,
                        label   = "Change Password",
                        color   = Color(0xFFE67E22),
                        onClick = { /* navigate to change password */ }
                    )
                }
            }

            // ── 6. App Info Card ───────────────────────────────────────────
            item {
                SectionHeader("About")
                ProfileCard {
                    ProfileInfoRow(Icons.Default.Info,        "App Version",    "v2.4.1",       TextGray)
                    ProfileDivider()
                    ProfileInfoRow(Icons.Default.Policy,      "Privacy Policy", "View Policy",  TextGray)
                    ProfileDivider()
                    ProfileInfoRow(Icons.Default.HelpOutline, "Help & Support", "Contact Us",   TextGray)
                }
            }

            // ── 7. Logout Button ───────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick   = { showLogoutDialog = true },
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = RedAbsent),
                    shape     = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "LOGOUT",
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ─── Reusable Components ──────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .background(AccentYellow, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
    }
}

@Composable
private fun ProfileCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            content  = content
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon:  ImageVector,
    label: String,
    value: String,
    tint:  Color
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(tint.copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = TextGray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
        }
    }
}

@Composable
private fun QuickActionRow(
    icon:    ImageVector,
    label:   String,
    color:   Color,
    onClick: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(color.copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 14.sp,
            color      = TextDark,
            modifier   = Modifier.weight(1f)
        )
        Icon(Icons.Default.ChevronRight, null, tint = TextGray, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 52.dp),
        color     = DividerColor,
        thickness = 0.8.dp
    )
}

@Composable
private fun ProfileStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = TextGray, textAlign = TextAlign.Center, lineHeight = 13.sp)
    }
}