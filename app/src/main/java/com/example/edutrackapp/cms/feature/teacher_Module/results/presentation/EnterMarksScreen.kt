package com.example.edutrackapp.cms.feature.teacher_Module.results.presentation

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

// ─── Design Tokens (matches Teacher Dashboard) ────────────────────────────────
private val DarkNavy     = Color(0xFF1B2438)
private val NavyMid      = Color(0xFF243047)
private val BgLight      = Color(0xFFF5F6FA)
private val CardWhite    = Color(0xFFFFFFFF)
private val AccentYellow = Color(0xFFFFB800)
private val GreenPass    = Color(0xFF2ECC71)
private val RedFail      = Color(0xFFE74C3C)
private val BlueInfo     = Color(0xFF3498DB)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8A9A)
private val DividerColor = Color(0xFFEEEEEE)

// Grade thresholds
private fun getGrade(marks: Int, max: Int): Pair<String, Color> {
    val pct = marks * 100f / max
    return when {
        pct >= 90 -> "A+" to Color(0xFF1ABC9C)
        pct >= 80 -> "A"  to GreenPass
        pct >= 70 -> "B"  to BlueInfo
        pct >= 60 -> "C"  to AccentYellow
        pct >= 50 -> "D"  to Color(0xFFE67E22)
        else       -> "F"  to RedFail
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterMarksScreen(
    navController: NavController,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val context  = LocalContext.current
    val students = viewModel.students

    // ── Exam detail state ─────────────────────────────────────────────────
    val examTypes   = listOf("Mid-Term", "End-Term", "Unit Test", "Practical", "Assignment")
    val subjectList = listOf("Data Structures", "Algorithms", "DBMS", "Computer Networks", "OS")

    var selectedExamType by remember { mutableStateOf(examTypes[0]) }
    var selectedSubject  by remember { mutableStateOf(subjectList[0]) }
    var maxMarks         by remember { mutableStateOf("100") }
    var examTypeExpanded by remember { mutableStateOf(false) }
    var subjectExpanded  by remember { mutableStateOf(false) }
    var showSaveDialog   by remember { mutableStateOf(false) }

    // ── Derived stats ─────────────────────────────────────────────────────
    val filledCount  = students.count { it.marks.isNotEmpty() }
    val absentCount  = students.count { it.marks.equals("AB", ignoreCase = true) }
    val maxVal       = maxMarks.toIntOrNull() ?: 100
    val avgMarks     = students
        .mapNotNull { it.marks.toIntOrNull() }
        .let { list -> if (list.isEmpty()) 0f else list.average().toFloat() }
    val passCount    = students.count {
        val m = it.marks.toIntOrNull() ?: return@count false
        m * 100f / maxVal >= 50f
    }

    // ── Save confirmation dialog ───────────────────────────────────────────
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            icon = {
                Icon(
                    Icons.Default.Save,
                    null,
                    tint     = AccentYellow,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Save Results?", fontWeight = FontWeight.Bold, color = TextDark)
            },
            text = {
                Text(
                    "You are about to save marks for $filledCount students in $selectedSubject ($selectedExamType).",
                    color     = TextGray,
                    textAlign = TextAlign.Center,
                    fontSize  = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        viewModel.saveResults(selectedExamType, selectedSubject) {
                            Toast.makeText(context, "Results saved successfully!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                    shape  = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSaveDialog = false },
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
                                "Enter Marks",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = Color.White
                            )
                            Text(
                                "$selectedSubject  •  $selectedExamType  •  ${students.size} Students",
                                fontSize = 11.sp,
                                color    = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        // Mark all absent
                        IconButton(onClick = { viewModel.markAllAbsent() }) {
                            Icon(
                                Icons.Default.PersonOff,
                                contentDescription = "Mark All Absent",
                                tint = Color.White.copy(0.85f)
                            )
                        }
                        // Clear all
                        IconButton(onClick = { viewModel.clearAllMarks() }) {
                            Icon(
                                Icons.Default.ClearAll,
                                contentDescription = "Clear All",
                                tint = AccentYellow
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                color           = CardWhite,
                shape           = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                    // ── Live stats strip ──────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgLight)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        MarksStat("$filledCount/${students.size}", "Filled",  BlueInfo)
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(DividerColor))
                        MarksStat("$passCount",                    "Passing", GreenPass)
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(DividerColor))
                        MarksStat("$absentCount",                  "Absent",  RedFail)
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(DividerColor))
                        MarksStat(
                            if (avgMarks > 0f) String.format("%.1f", avgMarks) else "-",
                            "Avg Marks",
                            AccentYellow
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick   = { showSaveDialog = true },
                        modifier  = Modifier.fillMaxWidth().height(52.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                        shape     = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SAVE RESULTS",
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Exam config card ──────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape     = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Subject dropdown
                        Text("Subject", fontSize = 11.sp, color = TextGray,
                            fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded         = subjectExpanded,
                            onExpandedChange = { subjectExpanded = it }
                        ) {
                            OutlinedTextField(
                                value           = selectedSubject,
                                onValueChange   = {},
                                readOnly        = true,
                                trailingIcon    = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded)
                                },
                                modifier        = Modifier.fillMaxWidth().menuAnchor(),
                                shape           = RoundedCornerShape(10.dp),
                                singleLine      = true,
                                colors          = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = AccentYellow,
                                    unfocusedBorderColor = DividerColor
                                )
                            )
                            ExposedDropdownMenu(
                                expanded         = subjectExpanded,
                                onDismissRequest = { subjectExpanded = false }
                            ) {
                                subjectList.forEach { sub ->
                                    DropdownMenuItem(
                                        text    = { Text(sub) },
                                        onClick = {
                                            selectedSubject  = sub
                                            subjectExpanded  = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Exam type + Max marks row
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1.6f)) {
                                Text("Exam Type", fontSize = 11.sp, color = TextGray,
                                    fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                ExposedDropdownMenuBox(
                                    expanded         = examTypeExpanded,
                                    onExpandedChange = { examTypeExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value         = selectedExamType,
                                        onValueChange = {},
                                        readOnly      = true,
                                        trailingIcon  = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = examTypeExpanded)
                                        },
                                        modifier      = Modifier.fillMaxWidth().menuAnchor(),
                                        shape         = RoundedCornerShape(10.dp),
                                        singleLine    = true,
                                        colors        = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor   = AccentYellow,
                                            unfocusedBorderColor = DividerColor
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded         = examTypeExpanded,
                                        onDismissRequest = { examTypeExpanded = false }
                                    ) {
                                        examTypes.forEach { type ->
                                            DropdownMenuItem(
                                                text    = { Text(type) },
                                                onClick = {
                                                    selectedExamType  = type
                                                    examTypeExpanded  = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Max Marks", fontSize = 11.sp, color = TextGray,
                                    fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value         = maxMarks,
                                    onValueChange = { if (it.length <= 3) maxMarks = it },
                                    singleLine    = true,
                                    shape         = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier      = Modifier.fillMaxWidth(),
                                    colors        = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor   = AccentYellow,
                                        unfocusedBorderColor = DividerColor
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section header
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 4.dp),
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
                        "Students  (${students.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        color      = TextDark
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Tap row to mark Absent",
                        fontSize = 11.sp,
                        color    = TextGray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Student rows ──────────────────────────────────────────────
            itemsIndexed(students) { index, student ->
                EnhancedResultRow(
                    student       = student,
                    maxMarks      = maxVal,
                    onMarksChange = { viewModel.onMarksChange(index, it) },
                    onToggleAbsent = { viewModel.toggleAbsent(index) }
                )
                if (index < students.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

// ─── Enhanced Result Row ──────────────────────────────────────────────────────
@Composable
fun EnhancedResultRow(
    student:        StudentResultUi,
    maxMarks:       Int,
    onMarksChange:  (String) -> Unit,
    onToggleAbsent: () -> Unit
) {
    val isAbsent = student.marks.equals("AB", ignoreCase = true)
    val marksInt = student.marks.toIntOrNull()
    val (grade, gradeColor) = if (!isAbsent && marksInt != null)
        getGrade(marksInt, maxMarks)
    else
        ("–" to TextGray)

    val cardBg by animateColorAsState(
        targetValue   = when {
            isAbsent   -> RedFail.copy(alpha = 0.05f)
            marksInt != null && marksInt * 100f / maxMarks < 50f -> RedFail.copy(0.04f)
            marksInt != null -> GreenPass.copy(alpha = 0.04f)
            else       -> CardWhite
        },
        animationSpec = tween(300),
        label         = "cardBg"
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(
                        if (isAbsent) RedFail
                        else if (marksInt != null) gradeColor
                        else DividerColor,
                        RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAbsent) RedFail.copy(0.12f) else DarkNavy.copy(0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    student.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    color      = if (isAbsent) RedFail else DarkNavy
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name + Roll
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    student.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    color      = TextDark
                )
                Text(
                    student.rollNo,
                    fontSize = 12.sp,
                    color    = TextGray
                )
            }

            // Grade badge
            if (!isAbsent && marksInt != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(gradeColor.copy(0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        grade,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 12.sp,
                        color      = gradeColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Absent toggle button
            IconButton(
                onClick  = onToggleAbsent,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    if (isAbsent) Icons.Default.PersonOff else Icons.Default.Person,
                    contentDescription = "Toggle Absent",
                    tint     = if (isAbsent) RedFail else TextGray.copy(0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Marks input
            OutlinedTextField(
                value         = if (isAbsent) "AB" else student.marks,
                onValueChange = { if (!isAbsent && it.length <= 3) onMarksChange(it) },
                enabled       = !isAbsent,
                singleLine    = true,
                modifier      = Modifier
                    .width(80.dp)
                    .padding(end = 12.dp),
                shape         = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle     = androidx.compose.ui.text.TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    textAlign  = TextAlign.Center,
                    color      = if (isAbsent) RedFail else TextDark
                ),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor     = AccentYellow,
                    unfocusedBorderColor   = if (isAbsent) RedFail.copy(0.4f) else DividerColor,
                    disabledBorderColor    = RedFail.copy(0.3f),
                    disabledTextColor      = RedFail,
                    focusedContainerColor  = CardWhite,
                    unfocusedContainerColor= CardWhite,
                    disabledContainerColor = RedFail.copy(0.05f)
                )
            )
        }
    }
}

// ─── Marks Stat Item ──────────────────────────────────────────────────────────
@Composable
fun MarksStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(label, fontSize = 11.sp, color = TextGray)
    }
}