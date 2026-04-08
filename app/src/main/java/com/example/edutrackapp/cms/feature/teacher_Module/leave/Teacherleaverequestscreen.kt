package com.example.edutrackapp.cms.feature.teacher_Module.leave

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

// ─── Color Palette (matches TeacherDashboard) ────────────────────────────────
private val NavyDeep    = Color(0xFF0D1B2A)
private val NavyMid     = Color(0xFF1B2B3E)
private val NavyLight   = Color(0xFF243447)
private val AmberAccent = Color(0xFFFFC107)
private val AmberLight  = Color(0xFFFFECB3)
private val BgPage      = Color(0xFFF0F4F8)
private val CardWhite   = Color(0xFFFFFFFF)
private val TextDark    = Color(0xFF0D1B2A)
private val TextGray    = Color(0xFF6B7A8D)
private val ErrorRed    = Color(0xFFE53935)
private val SuccessGreen= Color(0xFF2E7D32)
private val WarnOrange  = Color(0xFFF57C00)

// ─── Leave type colors ───────────────────────────────────────────────────────
private val CasualColor  = Color(0xFF1565C0)
private val MedicalColor = Color(0xFFC62828)
private val EarnedColor  = Color(0xFF2E7D32)
private val HalfDayColor = Color(0xFF6A1B9A)
private val CompOffColor = Color(0xFF00695C)
private val MatPatColor  = Color(0xFF4E342E)

// ─── Data Models ─────────────────────────────────────────────────────────────
data class LeaveBalance(
    val type: String,
    val icon: ImageVector,
    val used: Int,
    val total: Int,
    val color: Color
)

data class LeaveType(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

data class LeaveHistoryItem(
    val id: String,
    val type: String,
    val fromDate: String,
    val toDate: String,
    val days: Int,
    val reason: String,
    val status: LeaveStatus,
    val appliedOn: String,
    val adminNote: String = "",
    val color: Color
)

enum class LeaveStatus { PENDING, APPROVED, REJECTED, CANCELLED }

// ─── Sample Data ─────────────────────────────────────────────────────────────
private val leaveBalances = listOf(
    LeaveBalance("Casual Leave",   Icons.Default.BeachAccess,   4,  12, CasualColor),
    LeaveBalance("Medical Leave",  Icons.Default.LocalHospital, 2,  10, MedicalColor),
    LeaveBalance("Earned Leave",   Icons.Default.WorkHistory,   6,  18, EarnedColor),
    LeaveBalance("Comp Off",       Icons.Default.SwapHoriz,     1,   5, CompOffColor)
)

private val leaveTypes = listOf(
    LeaveType("Casual Leave",    Icons.Default.BeachAccess,    CasualColor,  "Personal / general purposes"),
    LeaveType("Medical Leave",   Icons.Default.LocalHospital,  MedicalColor, "Health / illness related"),
    LeaveType("Earned Leave",    Icons.Default.WorkHistory,    EarnedColor,  "Accumulated paid leave"),
    LeaveType("Half Day",        Icons.Default.WbTwilight,     HalfDayColor, "Morning or afternoon only"),
    LeaveType("Comp Off",        Icons.Default.SwapHoriz,      CompOffColor, "Compensatory off day"),
    LeaveType("Mat / Pat Leave", Icons.Default.ChildCare,      MatPatColor,  "Maternity / Paternity")
)

private val sampleHistory = listOf(
    LeaveHistoryItem(
        "LR-2024-018", "Casual Leave",
        "Dec 22, 2024", "Dec 23, 2024", 2,
        "Family function out of city",
        LeaveStatus.APPROVED, "Dec 18, 2024",
        "Approved. Arrange substitute for both days.", CasualColor
    ),
    LeaveHistoryItem(
        "LR-2024-011", "Medical Leave",
        "Nov 10, 2024", "Nov 11, 2024", 2,
        "Fever and flu — doctor advised rest",
        LeaveStatus.APPROVED, "Nov 09, 2024",
        "Approved with medical certificate.", MedicalColor
    ),
    LeaveHistoryItem(
        "LR-2024-007", "Earned Leave",
        "Oct 15, 2024", "Oct 17, 2024", 3,
        "Annual pilgrimage trip",
        LeaveStatus.REJECTED, "Oct 12, 2024",
        "Rejected – exam preparation period.", EarnedColor
    ),
    LeaveHistoryItem(
        "LR-2025-003", "Casual Leave",
        "Jan 30, 2025", "Jan 30, 2025", 1,
        "Bank-related urgent work",
        LeaveStatus.PENDING, "Jan 28, 2025",
        "", CasualColor
    )
)

// ─── Main Screen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLeaveRequestScreen(
    navController: NavController,
    teacherName: String = "Prof. Ujjwal Yadav"
) {
    val scrollState = rememberScrollState()

    // ── Form state ────────────────────────────────────────────────────────────
    var selectedLeaveType   by remember { mutableStateOf<LeaveType?>(null) }
    var fromDate            by remember { mutableStateOf("") }
    var toDate              by remember { mutableStateOf("") }
    var reason              by remember { mutableStateOf("") }
    var emergencyContact    by remember { mutableStateOf(false) }
    var substituteTeacher   by remember { mutableStateOf("") }
    var notifyHOD           by remember { mutableStateOf(true) }
    var halfDaySession      by remember { mutableStateOf("Morning") }
    var showSuccessDialog   by remember { mutableStateOf(false) }
    var showHistoryDetail   by remember { mutableStateOf<LeaveHistoryItem?>(null) }
    var activeTab           by remember { mutableStateOf(0) } // 0=Apply, 1=History
    var showLeaveTypeSheet  by remember { mutableStateOf(false) }
    var formError           by remember { mutableStateOf("") }

    // ── Pulse animation ───────────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOut), RepeatMode.Reverse),
        label = "pulseScale"
    )

    // ── Submission handler ────────────────────────────────────────────────────
    fun handleSubmit() {
        formError = ""
        when {
            selectedLeaveType == null -> formError = "Please select a leave type."
            fromDate.isBlank()        -> formError = "Please pick a From date."
            toDate.isBlank()          -> formError = "Please pick a To date."
            reason.length < 10        -> formError = "Reason must be at least 10 characters."
            else                      -> showSuccessDialog = true
        }
    }

    // ── Success Dialog ────────────────────────────────────────────────────────
    if (showSuccessDialog) {
        LeaveSubmittedDialog(
            leaveType = selectedLeaveType?.name ?: "",
            fromDate = fromDate,
            toDate = toDate,
            onDismiss = {
                showSuccessDialog = false
                selectedLeaveType = null
                fromDate = ""
                toDate = ""
                reason = ""
                activeTab = 1          // Switch to history to show pending
            }
        )
    }

    // ── History Detail Bottom-style dialog ────────────────────────────────────
    showHistoryDetail?.let { item ->
        LeaveDetailDialog(item = item, onDismiss = { showHistoryDetail = null })
    }

    // ── Leave Type Bottom Sheet ───────────────────────────────────────────────
    if (showLeaveTypeSheet) {
        LeaveTypeSheet(
            types = leaveTypes,
            selected = selectedLeaveType,
            onSelect = { selectedLeaveType = it; showLeaveTypeSheet = false },
            onDismiss = { showLeaveTypeSheet = false }
        )
    }

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
                .wrapContentHeight()          // ← let height grow with content
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(Brush.linearGradient(listOf(NavyDeep, NavyMid, NavyLight)))
                .statusBarsPadding()          // ← pushes content below status bar
        ) {
            // Decorative blobs
            Box(
                modifier = Modifier.size(160.dp).align(Alignment.TopEnd)
                    .offset(x = 50.dp, y = (-50).dp).clip(CircleShape)
                    .background(AmberAccent.copy(alpha = 0.07f))
            )
            Box(
                modifier = Modifier.size(90.dp).align(Alignment.BottomStart)
                    .offset(x = (-30).dp, y = 30.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable { navController.popBackStack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBack, contentDescription = "Back",
                                tint = Color.White, modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "Leave Request",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                "Apply & track your leave applications",
                                color = Color.White.copy(alpha = 0.60f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Pending count badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberAccent.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "1 Pending",
                                color = AmberAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Tab switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    listOf("Apply Leave", "My History").forEachIndexed { index, label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (activeTab == index) AmberAccent
                                    else Color.Transparent
                                )
                                .clickable { activeTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (activeTab == index) NavyDeep else Color.White.copy(alpha = 0.65f),
                                fontSize = 13.sp,
                                fontWeight = if (activeTab == index) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ── 2. Leave Balance Cards ─────────────────────────────────────────────
        Text(
            text = "Leave Balance",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextDark,
            modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            leaveBalances.forEach { bal ->
                LeaveBalanceCard(bal = bal, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── 3. Tab Content ────────────────────────────────────────────────────
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                (fadeIn(tween(220)) + slideInHorizontally { if (targetState > initialState) 60 else -60 })
                    .togetherWith(fadeOut(tween(150)))
            },
            label = "tab"
        ) { tab ->
            when (tab) {
                0 -> ApplyLeaveContent(
                    selectedLeaveType     = selectedLeaveType,
                    fromDate              = fromDate,
                    toDate                = toDate,
                    reason                = reason,
                    emergencyContact      = emergencyContact,
                    substituteTeacher     = substituteTeacher,
                    notifyHOD             = notifyHOD,
                    halfDaySession        = halfDaySession,
                    formError             = formError,
                    onLeaveTypeClick      = { showLeaveTypeSheet = true },
                    onFromDateChange      = { fromDate = it },
                    onToDateChange        = { toDate = it },
                    onReasonChange        = { reason = it; if (formError.isNotEmpty()) formError = "" },
                    onEmergencyToggle     = { emergencyContact = it },
                    onSubstituteChange    = { substituteTeacher = it },
                    onNotifyHODToggle     = { notifyHOD = it },
                    onHalfDaySessionChange= { halfDaySession = it },
                    onSubmit              = { handleSubmit() }
                )
                1 -> LeaveHistoryContent(
                    history = sampleHistory,
                    onItemClick = { showHistoryDetail = it }
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ─── Leave Balance Card ───────────────────────────────────────────────────────
@Composable
fun LeaveBalanceCard(bal: LeaveBalance, modifier: Modifier = Modifier) {
    val remaining = bal.total - bal.used
    val fraction = bal.used.toFloat() / bal.total.toFloat()

    // Animate progress bar on entry
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "progress"
    )

    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(bal.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = bal.icon, contentDescription = null,
                    tint = bal.color, modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$remaining",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = bal.color
            )
            Text(
                text = "/ ${bal.total}",
                fontSize = 10.sp,
                color = TextGray
            )
            Spacer(Modifier.height(6.dp))
            // Progress bar
            Box(
                modifier = Modifier.fillMaxWidth().height(4.dp)
                    .clip(RoundedCornerShape(2.dp)).background(bal.color.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(bal.color)
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = bal.type,
                fontSize = 8.5.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 11.sp
            )
        }
    }
}

// ─── Apply Leave Tab ──────────────────────────────────────────────────────────
@Composable
fun ApplyLeaveContent(
    selectedLeaveType     : LeaveType?,
    fromDate              : String,
    toDate                : String,
    reason                : String,
    emergencyContact      : Boolean,
    substituteTeacher     : String,
    notifyHOD             : Boolean,
    halfDaySession        : String,
    formError             : String,
    onLeaveTypeClick      : () -> Unit,
    onFromDateChange      : (String) -> Unit,
    onToDateChange        : (String) -> Unit,
    onReasonChange        : (String) -> Unit,
    onEmergencyToggle     : (Boolean) -> Unit,
    onSubstituteChange    : (String) -> Unit,
    onNotifyHODToggle     : (Boolean) -> Unit,
    onHalfDaySessionChange: (String) -> Unit,
    onSubmit              : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ── Quick Duration Chips ──────────────────────────────────────────────
        QuickDurationChips(
            onSelect = { preset ->
                val cal = Calendar.getInstance()
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                when (preset) {
                    "Today"    -> {
                        onFromDateChange(sdf.format(cal.time))
                        onToDateChange(sdf.format(cal.time))
                    }
                    "Tomorrow" -> {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        onFromDateChange(sdf.format(cal.time))
                        onToDateChange(sdf.format(cal.time))
                    }
                    "This Week" -> {
                        onFromDateChange(sdf.format(cal.time))
                        cal.add(Calendar.DAY_OF_YEAR, 4)
                        onToDateChange(sdf.format(cal.time))
                    }
                }
            }
        )

        // ── Leave Type Selector ───────────────────────────────────────────────
        FormSection(title = "Leave Type", icon = Icons.Default.Category) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.5.dp,
                        if (selectedLeaveType != null) selectedLeaveType.color.copy(alpha = 0.4f)
                        else Color(0xFFDDE3EA),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onLeaveTypeClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedLeaveType != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                    .background(selectedLeaveType.color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedLeaveType.icon,
                                    contentDescription = null,
                                    tint = selectedLeaveType.color,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    selectedLeaveType.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp, color = TextDark
                                )
                                Text(
                                    selectedLeaveType.description,
                                    fontSize = 11.sp, color = TextGray
                                )
                            }
                        }
                    } else {
                        Text(
                            "Tap to select leave type",
                            color = TextGray, fontSize = 14.sp
                        )
                    }
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null, tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Half-Day Session (visible only when Half Day selected) ────────────
        AnimatedVisibility(
            visible = selectedLeaveType?.name == "Half Day",
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FormSection(title = "Session", icon = Icons.Default.WbTwilight) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Morning", "Afternoon").forEach { session ->
                        val isSelected = halfDaySession == session
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) HalfDayColor.copy(alpha = 0.12f)
                                    else Color(0xFFF5F5F5)
                                )
                                .border(
                                    1.5.dp,
                                    if (isSelected) HalfDayColor else Color(0xFFDDE3EA),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onHalfDaySessionChange(session) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                session,
                                color = if (isSelected) HalfDayColor else TextGray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // ── Date Range ────────────────────────────────────────────────────────
        FormSection(title = "Duration", icon = Icons.Default.DateRange) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DatePickerField(
                    label = "From Date",
                    value = fromDate,
                    onValueChange = onFromDateChange,
                    modifier = Modifier.weight(1f)
                )
                DatePickerField(
                    label = "To Date",
                    value = toDate,
                    onValueChange = onToDateChange,
                    modifier = Modifier.weight(1f)
                )
            }
            // Days count badge
            if (fromDate.isNotBlank() && toDate.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberAccent.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info, contentDescription = null,
                            tint = WarnOrange, modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Selected duration: $fromDate → $toDate",
                            fontSize = 11.sp, color = WarnOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // ── Reason ────────────────────────────────────────────────────────────
        FormSection(title = "Reason for Leave", icon = Icons.Default.Notes) {
            OutlinedTextField(
                value = reason,
                onValueChange = onReasonChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Briefly describe your reason…", color = TextGray, fontSize = 13.sp) },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AmberAccent,
                    unfocusedBorderColor = Color(0xFFDDE3EA),
                    focusedTextColor     = TextDark,
                    unfocusedTextColor   = TextDark
                )
            )
            Text(
                text = "${reason.length} / 300 chars",
                fontSize = 10.sp, color = TextGray,
                modifier = Modifier.align(Alignment.End)
            )
        }

        // ── Substitute Teacher ────────────────────────────────────────────────
        FormSection(title = "Substitute Arrangement", icon = Icons.Default.PersonAdd) {
            OutlinedTextField(
                value = substituteTeacher,
                onValueChange = onSubstituteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Name of substitute teacher (optional)", color = TextGray, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person, contentDescription = null,
                        tint = TextGray, modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AmberAccent,
                    unfocusedBorderColor = Color(0xFFDDE3EA),
                    focusedTextColor     = TextDark,
                    unfocusedTextColor   = TextDark
                )
            )
        }

        // ── Notification Toggles ──────────────────────────────────────────────
        FormSection(title = "Notifications", icon = Icons.Default.NotificationsActive) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FB)),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, Color(0xFFEBEEF2))
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    ToggleRow(
                        label = "Notify Head of Department",
                        sub = "HOD receives copy of your application",
                        checked = notifyHOD,
                        onToggle = onNotifyHODToggle
                    )
                    HorizontalDivider(color = Color(0xFFEBEEF2))
                    ToggleRow(
                        label = "Mark as Emergency Leave",
                        sub = "Flags leave as urgent for quick approval",
                        checked = emergencyContact,
                        onToggle = onEmergencyToggle
                    )
                }
            }
        }

        // ── Admin Info Banner ─────────────────────────────────────────────────
        AdminInfoBanner()

        // ── Error Message ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = formError.isNotEmpty(),
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ErrorRed.copy(alpha = 0.09f))
                    .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Error, contentDescription = null,
                        tint = ErrorRed, modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(formError, color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // ── Submit Button ─────────────────────────────────────────────────────
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Send, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Submit to Admin Portal",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

// ─── Quick Duration Chips ─────────────────────────────────────────────────────
@Composable
fun QuickDurationChips(onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Quick Select",
            fontSize = 12.sp,
            color = TextGray,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("Today",     Icons.Default.Today,         AmberAccent),
                Triple("Tomorrow",  Icons.Default.CalendarToday, Color(0xFF42A5F5)),
                Triple("This Week", Icons.Default.DateRange,     EarnedColor)
            ).forEach { (label, icon, color) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(color.copy(alpha = 0.10f))
                        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clickable { onSelect(label) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─── Date Picker Field ────────────────────────────────────────────────────────
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    // Simple date wheel simulation (replace with real DatePickerDialog in production)
    val options = listOf("30 Jan 2025","31 Jan 2025","01 Feb 2025","02 Feb 2025","03 Feb 2025","04 Feb 2025","05 Feb 2025")

    if (showSheet) {
        AlertDialog(
            onDismissRequest = { showSheet = false },
            confirmButton = {},
            title = { Text(label, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    options.forEach { date ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (value == date) AmberAccent.copy(0.15f) else Color.Transparent)
                                .clickable { onValueChange(date); showSheet = false }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday, null,
                                tint = if (value == date) AmberAccent else TextGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                date,
                                color = if (value == date) TextDark else TextGray,
                                fontWeight = if (value == date) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = CardWhite
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.5.dp,
                if (value.isNotBlank()) AmberAccent.copy(0.5f) else Color(0xFFDDE3EA),
                RoundedCornerShape(12.dp)
            )
            .clickable { showSheet = true }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(label, fontSize = 10.sp, color = TextGray)
                Spacer(Modifier.height(2.dp))
                Text(
                    if (value.isBlank()) "DD MMM YYYY" else value,
                    fontSize = 13.sp,
                    color = if (value.isBlank()) Color(0xFFB0BEC5) else TextDark,
                    fontWeight = if (value.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            Icon(
                Icons.Default.EditCalendar, null,
                tint = if (value.isNotBlank()) AmberAccent else Color(0xFFB0BEC5),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Toggle Row ───────────────────────────────────────────────────────────────
@Composable
fun ToggleRow(label: String, sub: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(label, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.SemiBold)
            Text(sub, fontSize = 10.sp, color = TextGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor  = Color.White,
                checkedTrackColor  = AmberAccent,
                uncheckedTrackColor= Color(0xFFCFD8DC)
            )
        )
    }
}

// ─── Admin Info Banner ────────────────────────────────────────────────────────
@Composable
fun AdminInfoBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(listOf(NavyMid, NavyLight))
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(AmberAccent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Sent to Admin Portal",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    "Your application will be reviewed by\nAdmin. Approval within 24–48 hrs.",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

// ─── Form Section Wrapper ─────────────────────────────────────────────────────
@Composable
fun FormSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, contentDescription = null,
                tint = AmberAccent, modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
        }
        content()
    }
}

// ─── Leave Type Bottom Sheet ──────────────────────────────────────────────────
@Composable
fun LeaveTypeSheet(
    types: List<LeaveType>,
    selected: LeaveType?,
    onSelect: (LeaveType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Category, null,
                    tint = AmberAccent, modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Select Leave Type", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { type ->
                    val isSelected = selected?.name == type.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) type.color.copy(0.10f) else Color(0xFFF8F9FB)
                            )
                            .border(
                                1.5.dp,
                                if (isSelected) type.color.copy(0.4f) else Color(0xFFEBEEF2),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelect(type) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                .background(type.color.copy(0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(type.icon, null, tint = type.color, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(type.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                            Text(type.description, fontSize = 11.sp, color = TextGray)
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle, null,
                                tint = type.color, modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = CardWhite
    )
}

// ─── Leave History Tab ────────────────────────────────────────────────────────
@Composable
fun LeaveHistoryContent(
    history: List<LeaveHistoryItem>,
    onItemClick: (LeaveHistoryItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary stats row
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val approved = history.count { it.status == LeaveStatus.APPROVED }
            val pending  = history.count { it.status == LeaveStatus.PENDING }
            val rejected = history.count { it.status == LeaveStatus.REJECTED }
            listOf(
                Triple("$approved", "Approved", SuccessGreen),
                Triple("$pending",  "Pending",  WarnOrange),
                Triple("$rejected", "Rejected", ErrorRed)
            ).forEach { (val_, label, color) ->
                Card(
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(val_, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
                        Text(label, fontSize = 10.sp, color = TextGray)
                    }
                }
            }
        }

        // History items
        history.sortedByDescending { it.appliedOn }.forEach { item ->
            LeaveHistoryCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

// ─── History Card ─────────────────────────────────────────────────────────────
@Composable
fun LeaveHistoryCard(item: LeaveHistoryItem, onClick: () -> Unit) {
    val (statusColor, statusIcon, statusBg) = when (item.status) {
        LeaveStatus.APPROVED  -> Triple(SuccessGreen, Icons.Default.CheckCircle, SuccessGreen.copy(0.08f))
        LeaveStatus.PENDING   -> Triple(WarnOrange,   Icons.Default.HourglassTop, WarnOrange.copy(0.08f))
        LeaveStatus.REJECTED  -> Triple(ErrorRed,     Icons.Default.Cancel,       ErrorRed.copy(0.08f))
        LeaveStatus.CANCELLED -> Triple(TextGray,     Icons.Default.Block,        TextGray.copy(0.08f))
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(18.dp)).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top color bar
            Box(
                modifier = Modifier.fillMaxWidth().height(4.dp)
                    .background(Brush.horizontalGradient(listOf(item.color, item.color.copy(0.35f))))
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                .background(item.color.copy(0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.BeachAccess, null,
                                tint = item.color, modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                item.type,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp, color = TextDark
                            )
                            Text(
                                "Applied: ${item.appliedOn}",
                                fontSize = 11.sp, color = TextGray
                            )
                        }
                    }
                    // Status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                statusIcon, null,
                                tint = statusColor, modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                item.status.name,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFF0F2F5))
                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Date range
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DateRange, null,
                            tint = TextGray, modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "${item.fromDate} → ${item.toDate}",
                            fontSize = 12.sp, color = TextGray
                        )
                    }
                    // Days count
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(item.color.copy(0.10f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "${item.days} day${if (item.days > 1) "s" else ""}",
                            color = item.color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    item.reason,
                    fontSize = 12.sp, color = TextGray,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                // Ref ID row
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ref: ${item.id}",
                        fontSize = 10.sp, color = Color(0xFFB0BEC5),
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("View Details", color = Color(0xFF1565C0), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Icon(
                            Icons.Default.ChevronRight, null,
                            tint = Color(0xFF1565C0), modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Leave Detail Dialog ──────────────────────────────────────────────────────
@Composable
fun LeaveDetailDialog(item: LeaveHistoryItem, onDismiss: () -> Unit) {
    val (statusColor, statusIcon) = when (item.status) {
        LeaveStatus.APPROVED  -> Pair(SuccessGreen, Icons.Default.CheckCircle)
        LeaveStatus.PENDING   -> Pair(WarnOrange,   Icons.Default.HourglassTop)
        LeaveStatus.REJECTED  -> Pair(ErrorRed,     Icons.Default.Cancel)
        LeaveStatus.CANCELLED -> Pair(TextGray,     Icons.Default.Block)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                        .background(item.color.copy(0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EventNote, null, tint = item.color, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(item.type, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(item.id, fontSize = 11.sp, color = TextGray)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Status timeline
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(0.08f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Status: ${item.status.name}",
                        color = statusColor, fontWeight = FontWeight.Bold, fontSize = 14.sp
                    )
                }

                DetailRow("Duration", "${item.fromDate}  →  ${item.toDate}  (${item.days} days)", Icons.Default.DateRange)
                DetailRow("Applied On", item.appliedOn, Icons.Default.Schedule)
                DetailRow("Reason", item.reason, Icons.Default.Notes)

                if (item.adminNote.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyMid)
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AdminPanelSettings, null,
                                    tint = AmberAccent, modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Admin Note", color = AmberAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(item.adminNote, color = Color.White.copy(0.8f), fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = CardWhite
    )
}

@Composable
private fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = AmberAccent, modifier = Modifier.size(15.dp).padding(top = 1.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
            Text(value, fontSize = 13.sp, color = TextDark, lineHeight = 17.sp)
        }
    }
}

// ─── Success / Submission Confirmation Dialog ─────────────────────────────────
@Composable
fun LeaveSubmittedDialog(
    leaveType: String,
    fromDate: String,
    toDate: String,
    onDismiss: () -> Unit
) {
    // Tick animation scale
    val tickScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tick"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Big tick circle
                Box(
                    modifier = Modifier.size(72.dp).scale(tickScale).clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle, null,
                        tint = SuccessGreen, modifier = Modifier.size(44.dp)
                    )
                }
                Text(
                    "Application Submitted!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = TextDark
                )
                Text(
                    "Your $leaveType request has been sent to the Admin Portal for approval.",
                    fontSize = 13.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                // Summary box
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F4F8))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Type", fontSize = 12.sp, color = TextGray)
                            Text(leaveType, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("From", fontSize = 12.sp, color = TextGray)
                            Text(fromDate.ifBlank { "—" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("To", fontSize = 12.sp, color = TextGray)
                            Text(toDate.ifBlank { "—" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, null, tint = WarnOrange, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Approval expected within 24–48 hrs", fontSize = 11.sp, color = WarnOrange)
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = CardWhite
    )
}