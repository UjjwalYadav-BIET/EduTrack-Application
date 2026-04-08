package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

// ─── Design tokens ────────────────────────────────────────────────────────────
private val DarkNavy     = Color(0xFF1B2438)
private val BgLight      = Color(0xFFF5F6FA)
private val CardWhite    = Color(0xFFFFFFFF)
private val AccentYellow = Color(0xFFFFB800)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8A9A)
private val RedHigh      = Color(0xFFE74C3C)
private val OrangeMed    = Color(0xFFF39C12)
private val GreenLow     = Color(0xFF2ECC71)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentScreen(
    navController: NavController,
    viewModel: AssignmentViewModel = hiltViewModel()
) {
    val context     = LocalContext.current
    val scrollState = rememberScrollState()

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onFileSelected(uri)
        if (uri != null) Toast.makeText(context, "File attached!", Toast.LENGTH_SHORT).show()
    }

    // Date picker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDateChange(
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
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

    Scaffold(
        containerColor = BgLight,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Create Assignment",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = Color.White
                            )
                            Text(
                                "CS-A  •  Fill in all required fields",
                                fontSize = 11.sp,
                                color    = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { paddingValues ->

        // Read state values once at the composable level
        val titleValue       = viewModel.title.value
        val subjectValue     = viewModel.subject.value
        val descValue        = viewModel.description.value
        val dueDateValue     = viewModel.dueDate.value
        val totalMarksValue  = viewModel.totalMarks.value
        val priorityValue    = viewModel.priority.value
        val fileUriValue     = viewModel.selectedFileUri.value
        val titleErrValue    = viewModel.titleError.value
        val subjectErrValue  = viewModel.subjectError.value
        val descErrValue     = viewModel.descriptionError.value
        val dueDateErrValue  = viewModel.dueDateError.value

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Assignment Info ───────────────────────────────────────
            SectionHeader("Assignment Info")

            AssignmentField(
                value         = titleValue,
                onValueChange = viewModel::onTitleChange,
                label         = "Assignment Title *",
                icon          = Icons.Default.Title,
                isError       = titleErrValue,
                errorText     = "Title is required"
            )

            AssignmentField(
                value         = subjectValue,
                onValueChange = viewModel::onSubjectChange,
                label         = "Subject *",
                icon          = Icons.Default.Book,
                isError       = subjectErrValue,
                errorText     = "Subject is required"
            )

            OutlinedTextField(
                value         = descValue,
                onValueChange = viewModel::onDescChange,
                label         = { Text("Instructions / Description *") },
                leadingIcon   = { Icon(Icons.Default.Description, null, tint = AccentYellow) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape         = RoundedCornerShape(14.dp),
                isError       = descErrValue,
                supportingText = {
                    if (descErrValue)
                        Text("Description is required", color = RedHigh, fontSize = 11.sp)
                },
                maxLines      = 6,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = AccentYellow,
                    unfocusedBorderColor    = Color(0xFFDDDDDD),
                    focusedLabelColor       = AccentYellow,
                    cursorColor             = AccentYellow,
                    focusedContainerColor   = CardWhite,
                    unfocusedContainerColor = CardWhite
                )
            )

            // ── Schedule & Marks ──────────────────────────────────────
            SectionHeader("Schedule & Marks")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    onClick   = { showDatePicker = true },
                    modifier  = Modifier.weight(1f),
                    shape     = RoundedCornerShape(14.dp),
                    color     = CardWhite,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, null,
                            tint = AccentYellow, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Due Date", fontSize = 10.sp, color = TextGray)
                            Text(
                                text       = dueDateValue.ifBlank { "Pick date" },
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = when {
                                    dueDateErrValue          -> RedHigh
                                    dueDateValue.isBlank()   -> TextGray
                                    else                     -> TextDark
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value         = totalMarksValue,
                    onValueChange = viewModel::onMarksChange,
                    label         = { Text("Total Marks") },
                    leadingIcon   = { Icon(Icons.Default.Grade, null, tint = AccentYellow) },
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(14.dp),
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = AccentYellow,
                        unfocusedBorderColor    = Color(0xFFDDDDDD),
                        focusedLabelColor       = AccentYellow,
                        cursorColor             = AccentYellow,
                        focusedContainerColor   = CardWhite,
                        unfocusedContainerColor = CardWhite
                    )
                )
            }

            // ── Priority ─────────────────────────────────────────────
            SectionHeader("Priority Level")

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssignmentPriority.values().forEach { p ->
                    val selected = priorityValue == p
                    val color = when (p) {
                        AssignmentPriority.HIGH   -> RedHigh
                        AssignmentPriority.MEDIUM -> OrangeMed
                        AssignmentPriority.LOW    -> GreenLow
                    }
                    Surface(
                        onClick   = { viewModel.onPriorityChange(p) },
                        modifier  = Modifier.weight(1f),
                        shape     = RoundedCornerShape(12.dp),
                        color     = if (selected) color.copy(alpha = 0.15f) else CardWhite,
                        shadowElevation = if (selected) 0.dp else 2.dp,
                        border    = if (selected)
                            androidx.compose.foundation.BorderStroke(1.5.dp, color) else null
                    ) {
                        Column(
                            modifier            = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                p.label,
                                fontSize   = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color      = if (selected) color else TextGray
                            )
                        }
                    }
                }
            }

            // ── Attachment ───────────────────────────────────────────
            SectionHeader("Attachment  (Optional)")

            val hasFile = fileUriValue != null
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.5.dp,
                        color = if (hasFile) AccentYellow else Color(0xFFDDDDDD),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(if (hasFile) AccentYellow.copy(0.07f) else CardWhite)
                    .clickable { fileLauncher.launch("application/pdf") },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (hasFile) Icons.Default.CheckCircle else Icons.Default.AttachFile,
                        null,
                        tint     = if (hasFile) AccentYellow else TextGray,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            if (hasFile) "PDF Attached ✓" else "Tap to Attach PDF",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 13.sp,
                            color      = if (hasFile) AccentYellow else TextGray
                        )
                        Text(
                            if (hasFile) "Tap to replace" else "PDF files only",
                            fontSize = 11.sp,
                            color    = TextGray
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Submit ───────────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.createAssignment {
                        Toast.makeText(context, "Assignment Posted!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                shape  = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "POST ASSIGNMENT",
                    fontWeight    = FontWeight.Bold,
                    fontSize      = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Shared helpers ───────────────────────────────────────────────────────────
@Composable
fun SectionHeader(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .background(AccentYellow, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text          = text,
            fontWeight    = FontWeight.Bold,
            fontSize      = 13.sp,
            color         = TextDark,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
fun AssignmentField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isError: Boolean  = false,
    errorText: String = ""
) {
    OutlinedTextField(
        value          = value,
        onValueChange  = onValueChange,
        label          = { Text(label) },
        leadingIcon    = { Icon(icon, null, tint = AccentYellow) },
        modifier       = Modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(14.dp),
        singleLine     = true,
        isError        = isError,
        supportingText = {
            if (isError) Text(errorText, color = RedHigh, fontSize = 11.sp)
        },
        colors         = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = AccentYellow,
            unfocusedBorderColor    = Color(0xFFDDDDDD),
            focusedLabelColor       = AccentYellow,
            cursorColor             = AccentYellow,
            focusedContainerColor   = CardWhite,
            unfocusedContainerColor = CardWhite
        )
    )
}