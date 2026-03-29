package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Book // <--- ADDED IMPORT
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.edutrackapp.Domain.Model.attendance.Subject
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.AttendanceViewModel
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.SubjectDropdown
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentScreen(
    navController: NavController,
    viewModel: AssignmentViewModel = hiltViewModel(),
    viewModelSubject:AttendanceViewModel = hiltViewModel()
) {
    val subjects by viewModelSubject.subjects.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModelSubject.loadSubjects()
    }
    val selectedSubjectId=viewModelSubject.selectedSubjectId

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            viewModel.onDateChange(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    val raw = viewModel.dueDate.value
    val isError = raw.length == 8 && !isValidDate(raw)

    // 1. FILE PICKER LAUNCHER
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onFileSelected(uri)
        if (uri != null) {
            Toast.makeText(context, "PDF Selected!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Assignment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(end = 10.dp, start = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TITLE INPUT
            OutlinedTextField(
                value = viewModel.title.value,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text("Assignment Title") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            SubjectDropdown(
                subjects = subjects,
                selectedId = selectedSubjectId,
                onSelected = { viewModelSubject.selectedSubjectId= it }
            )
            // --------------------------

            // 🔢 Semester
            OutlinedTextField(
                value = viewModel.semester.value,
                onValueChange = { viewModel.onSemesterChange(it) },
                label = { Text("Semester") },
                leadingIcon = {
                    Icon(Icons.Default.Title, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

// 🅰 Section
            OutlinedTextField(
                value = viewModel.section.value,
                onValueChange = { viewModel.onSectionChange(it) },
                label = { Text("Section (A/B/C)") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

// 🏫 Branch
            OutlinedTextField(
                value = viewModel.branch.value,
                onValueChange = { viewModel.onBranchChange(it) },
                label = { Text("Branch (CSE/ECE/ME)") },
                leadingIcon = {
                    Icon(Icons.Default.Book, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // DESCRIPTION INPUT
            OutlinedTextField(
                value = viewModel.description.value,
                onValueChange = { viewModel.onDescChange(it) },
                label = { Text("Description / Instructions") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            OutlinedTextField(
                value = viewModel.dueDate.value,
                onValueChange = {
                    viewModel.onDateChange(it.filter { ch -> ch.isDigit() }.take(8))
                },
                visualTransformation = DateVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Due Date (DD/MM/YYYY)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            datePickerDialog.show()
                        }
                    )
                },
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text("Invalid date")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val selectedUri = viewModel.selectedFileUri.value
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .clickable {
                        fileLauncher.launch("application/pdf")
                    }
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedUri != null) "File Attached" else "Tap to Attach PDF",
                        color = if (selectedUri != null) Color(0xFF4CAF50) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // SUBMIT BUTTON
            Button(
                onClick = {

                    if (
                        viewModel.title.value.isBlank() ||
                        viewModel.description.value.isBlank() ||
                        viewModel.semester.value.isBlank() ||
                        viewModel.section.value.isBlank() ||
                        viewModel.branch.value.isBlank() ||
                        viewModel.dueDate.value.length != 8 ||
                        isError
                    ) {
                        Toast.makeText(context, "Please fill all fields correctly ❌", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val dueDateMillis = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .parse(formatToDateString(viewModel.dueDate.value))?.time ?: 0L

                    val semesterInt = viewModel.semester.value.toIntOrNull() ?: 0

                    viewModel.createAssignment(
                        semester = semesterInt,
                        dueDate = dueDateMillis,
                        subjectId = selectedSubjectId
                    ) {
                        Toast.makeText(context, "Assignment Posted!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("POST ASSIGNMENT", fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun isValidDate(raw: String): Boolean {
    if (raw.length != 8) return false

    val date = formatToDateString(raw)

    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        sdf.parse(date)
        true
    } catch (e: Exception) {
        false
    }
}

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text.take(8)
        val builder = StringBuilder()

        for (i in input.indices) {
            builder.append(input[i])
            if ((i == 1 || i == 3) && i != input.lastIndex) {
                builder.append("/")
            }
        }

        val formatted = builder.toString()

        val offsetMapping = object : OffsetMapping {

            // 👉 FIXED: cursor forward push after '/'
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset in 3..4 -> offset + 1
                    offset in 5..8 -> offset + 2
                    else -> formatted.length
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset in 3..5 -> offset - 1
                    offset in 6..10 -> offset - 2
                    else -> input.length
                }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
fun formatToDateString(input: String): String {
    if (input.length < 8) return input

    return "${input.substring(0, 2)}/${input.substring(2, 4)}/${input.substring(4, 8)}"
}

@Preview
@Composable
fun CreateAssignmentPreview(){
    val navController= rememberNavController()
    CreateAssignmentScreen(navController)
}