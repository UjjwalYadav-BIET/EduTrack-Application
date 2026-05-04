package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

// ─── Design Tokens ────────────────────────────────────────────────────────────
private val DarkNavy     = Color(0xFF1B2438)
private val BgLight      = Color(0xFFF5F6FA)
private val CardWhite    = Color(0xFFFFFFFF)
private val AccentYellow = Color(0xFFFFB800)
private val GreenPresent = Color(0xFF2ECC71)
private val RedAbsent    = Color(0xFFE74C3C)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8A9A)
private val DividerColor = Color(0xFFEEEEEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    navController: NavController,
    classId: String,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val context   = LocalContext.current
    val backStack = navController.currentBackStackEntry

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Collect students as Compose state so every toggle triggers recompose
    val allStudents     by viewModel.students.collectAsState()
    val displayStudents  = viewModel.filteredStudents(allStudents)

    // Load students when screen opens
    LaunchedEffect(classId) {
        viewModel.loadStudents(classId)
    }

    // Receive face-scan result from FaceScanScreen
    val faceCount by backStack?.savedStateHandle
        ?.getStateFlow<Int?>("face_count", null)
        ?.collectAsState()
        ?: remember { mutableStateOf(null) }

    LaunchedEffect(faceCount) {
        faceCount?.let { count ->
            if (count > 0) {
                viewModel.markStudentsBasedOnCount(count)
                Toast.makeText(context, "Auto-marked $count students present!", Toast.LENGTH_LONG).show()
                backStack?.savedStateHandle?.remove<Int>("face_count")
            }
        }
    }

    // ── Date picker ───────────────────────────────────────────────────────
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.onDateChange(
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
                        )
                    }
                    showDatePicker = false
                }) { Text("OK", color = DarkNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextGray)
                }
            },
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = AccentYellow,
                selectedDayContentColor   = DarkNavy,
                todayDateBorderColor      = AccentYellow
            )
        ) { DatePicker(state = datePickerState) }
    }

    // ── Time picker ───────────────────────────────────────────────────────
    val cal             = remember { Calendar.getInstance() }
    val timePickerState = rememberTimePickerState(
        initialHour   = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE)
    )
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onTimeChange(
                        String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    )
                    showTimePicker = false
                }) { Text("OK", color = DarkNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = TextGray) }
            },
            title = { Text("Select Session Time", fontWeight = FontWeight.Bold, color = TextDark) },
            text  = { TimePicker(state = timePickerState) }
        )
    }

    Scaffold(
        containerColor = BgLight,
        topBar = {
            Column {
                // ── Dark-navy header ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkNavy)
                        .statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 4.dp, end = 8.dp, top = 8.dp, bottom = 16.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Mark Attendance",
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 18.sp,
                                    color      = Color.White
                                )
                                Text(
                                    if (viewModel.isLoadingStudents)
                                        "Computer Science  •  $classId  •  Loading…"
                                    else
                                        "Computer Science  •  $classId  •  ${allStudents.size} Students",
                                    fontSize = 11.sp,
                                    color    = Color.White.copy(alpha = 0.55f)
                                )
                            }
                            // ✅ Enroll faces button
                            IconButton(
                                onClick = {
                                    navController.navigate(Screen.EnrollFace.createRoute(classId))
                                }
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = "Enroll Faces",
                                    tint = AccentYellow
                                )
                            }
                            IconButton(
                                onClick = {
                                    navController.navigate(Screen.AttendanceHistory.route)
                                }
                            ) {
                                Icon(Icons.Default.History, null, tint = Color.White.copy(0.85f))
                            }
                            IconButton(onClick = { viewModel.exportToCSV(context) }) {
                                Icon(Icons.Default.FileDownload, null, tint = AccentYellow)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DateTimeChip(
                                modifier = Modifier.weight(1f),
                                icon     = Icons.Default.CalendarToday,
                                label    = viewModel.selectedDate,
                                onClick  = { showDatePicker = true }
                            )
                            DateTimeChip(
                                modifier = Modifier.weight(1f),
                                icon     = Icons.Default.AccessTime,
                                label    = viewModel.selectedTime,
                                onClick  = { showTimePicker = true }
                            )
                        }
                    }
                }

                // ── Search bar ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkNavy)
                        .background(BgLight, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    OutlinedTextField(
                        value         = viewModel.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder   = {
                            Text("Search name or roll number…", fontSize = 14.sp, color = TextGray)
                        },
                        leadingIcon  = {
                            Icon(Icons.Default.Search, null, tint = TextGray, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (viewModel.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, null, tint = TextGray, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        modifier    = Modifier.fillMaxWidth(),
                        singleLine  = true,
                        shape       = RoundedCornerShape(14.dp),
                        colors      = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = AccentYellow,
                            unfocusedBorderColor    = Color.Transparent,
                            focusedContainerColor   = CardWhite,
                            unfocusedContainerColor = CardWhite
                        )
                    )
                }
            }
        },

        // ── FAB — Smart Attendance via Face Scan ──────────────────────────
        floatingActionButton = {
            FloatingActionButton(
                // ✅ Pass classId so FaceScanScreen loads the right enrolled faces
                onClick        = { navController.navigate(Screen.FaceScan.createRoute(classId)) },
                containerColor = DarkNavy,
                contentColor   = AccentYellow,
                shape          = RoundedCornerShape(16.dp),
                modifier       = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.CameraAlt, "Smart Attendance", modifier = Modifier.size(26.dp))
            }
        },

        // ── Bottom bar ────────────────────────────────────────────────────
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                color           = CardWhite,
                shape           = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgLight)
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        StatItem(label = "Present", count = allStudents.count { it.isPresent },  color = GreenPresent)
                        VerticalDivider()
                        StatItem(label = "Absent",  count = allStudents.count { !it.isPresent }, color = RedAbsent)
                        VerticalDivider()
                        StatItem(label = "Total",   count = allStudents.size,                    color = DarkNavy)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.saveAttendanceRecord()
                            Toast.makeText(
                                context,
                                "Submitted! Present: ${viewModel.getPresentCount()} / ${viewModel.getTotalCount()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        },
                        enabled  = !viewModel.isLoadingStudents,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                        shape  = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SUBMIT ATTENDANCE",
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        when {
            viewModel.isLoadingStudents -> {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentYellow, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading students…", color = TextGray, fontSize = 14.sp)
                    }
                }
            }

            displayStudents.isEmpty() -> {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = TextGray.copy(alpha = 0.35f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (viewModel.searchQuery.isBlank()) "No students enrolled yet" else "No students found",
                            color = TextGray, fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            if (viewModel.searchQuery.isBlank()) "Ask the admin to enroll students first"
                            else "Try a different name or roll number",
                            color = TextGray.copy(0.6f), fontSize = 13.sp
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(18.dp)
                                    .background(AccentYellow, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Students  (${displayStudents.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp,
                                color      = TextDark
                            )
                        }
                    }

                    items(displayStudents, key = { it.id }) { student ->
                        StudentRow(
                            student  = student,
                            onToggle = { viewModel.toggleAttendance(student.id) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ─── Student Row Card ─────────────────────────────────────────────────────────
@Composable
fun StudentRow(student: StudentUiModel, onToggle: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue   = if (student.isPresent) GreenPresent.copy(alpha = 0.06f) else CardWhite,
        animationSpec = tween(300), label = "cardBg"
    )
    val iconScale by animateFloatAsState(
        targetValue   = if (student.isPresent) 1.15f else 1f,
        animationSpec = tween(200), label = "iconScale"
    )

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(
                        if (student.isPresent) GreenPresent else DividerColor,
                        RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )
            Spacer(modifier = Modifier.width(14.dp))
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (student.isPresent) GreenPresent.copy(alpha = 0.15f)
                        else DarkNavy.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = student.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 17.sp,
                    color      = if (student.isPresent) GreenPresent else DarkNavy
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                Spacer(modifier = Modifier.height(2.dp))
                Text(student.rollNo, fontSize = 12.sp, color = TextGray)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector        = if (student.isPresent) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint               = if (student.isPresent) GreenPresent else TextGray.copy(alpha = 0.4f),
                    modifier           = Modifier.size(28.dp).scale(iconScale)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text       = if (student.isPresent) "Present" else "Absent",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color      = if (student.isPresent) GreenPresent else TextGray,
                    textAlign  = TextAlign.Center
                )
            }
        }
    }
}

// ─── Shared Composables ───────────────────────────────────────────────────────
@Composable
fun DateTimeChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.12f), modifier = modifier) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AccentYellow, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
        Text(label, fontSize = 12.sp, color = TextGray)
    }
}

@Composable
fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(36.dp).background(DividerColor))
}