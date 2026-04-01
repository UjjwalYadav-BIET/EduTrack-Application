package com.example.edutrackapp.cms.feature.teacher_Module.results.presentation

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.core.data.local.entity.TestEntity
import com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation.DateVisualTransformation
import com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation.formatToDateString
import com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation.isValidDate
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.AttendanceViewModel
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation.SubjectDropdown
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTestScreen(
    navController: NavController,
    viewModel: ResultViewModel = hiltViewModel(),
    viewModelSubject: AttendanceViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModelSubject.loadSubjects()
    }
    var title by remember { mutableStateOf("") }
    val subjects by viewModelSubject.subjects.collectAsState()
    val selectedSubjectId = viewModelSubject.selectedSubjectId

    val context = LocalContext.current

    var maxMarks by remember { mutableStateOf("") }


    // Dropdown states
    val yearOptions = listOf("1", "2", "3", "4")
    val semesterOptions=listOf(1,2,3,4,5,6,7,8)
    var selectedYear by remember { mutableStateOf(yearOptions.first()) }
    var yearExpanded by remember { mutableStateOf(false) }

    var selectedSem by remember { mutableStateOf(semesterOptions.first()) }
    var semExpanded by remember { mutableStateOf(false) }

    val branchOptions = listOf("CSE", "ECE", "ME", "CE")
    var selectedBranch by remember { mutableStateOf(branchOptions.first()) }
    var branchExpanded by remember { mutableStateOf(false) }

    val sectionOptions = listOf("A", "B", "C", "D")
    var selectedSection by remember { mutableStateOf(sectionOptions.first()) }
    var sectionExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Test Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Subject
            SubjectDropdown(
                subjects = subjects,
                selectedId = selectedSubjectId,
                onSelected = { viewModelSubject.selectedSubjectId = it }
            )

            OutlinedTextField(
                value = maxMarks,
                onValueChange = { maxMarks = it },
                label = { Text("Max Marks") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Year Dropdown
            ExposedDropdownMenuBox(
                expanded = yearExpanded,
                onExpandedChange = { yearExpanded = !yearExpanded }
            ) {
                OutlinedTextField(
                    value = selectedYear,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Year") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(yearExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = yearExpanded,
                    onDismissRequest = { yearExpanded = false }
                ) {
                    yearOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                selectedYear = it
                                yearExpanded = false
                            }
                        )
                    }
                }
            }

            // Branch Dropdown
            ExposedDropdownMenuBox(
                expanded = branchExpanded,
                onExpandedChange = { branchExpanded = !branchExpanded }
            ) {
                OutlinedTextField(
                    value = selectedBranch,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Branch") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(branchExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = branchExpanded,
                    onDismissRequest = { branchExpanded = false }
                ) {
                    branchOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                selectedBranch = it
                                branchExpanded = false
                            }
                        )
                    }
                }
            }

            // Branch Dropdown
            ExposedDropdownMenuBox(
                expanded = semExpanded,
                onExpandedChange = { semExpanded = !semExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSem.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Semester") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(semExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = semExpanded,
                    onDismissRequest = { semExpanded = false }
                ) {
                    semesterOptions.forEach {
                        DropdownMenuItem(
                            text = { Text("$it") },
                            onClick = {
                                selectedSem = it
                                semExpanded = false
                            }
                        )
                    }
                }
            }

            // Section Dropdown
            ExposedDropdownMenuBox(
                expanded = sectionExpanded,
                onExpandedChange = { sectionExpanded = !sectionExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSection,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Section") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(sectionExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = sectionExpanded,
                    onDismissRequest = { sectionExpanded = false }
                ) {
                    sectionOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                selectedSection = it
                                sectionExpanded = false
                            }
                        )
                    }
                }
            }


            // Button
            Button(
                onClick = {
                    if (
                        title.isBlank() ||
                        selectedSubjectId == null ||
                        maxMarks.isBlank()
                    ) {
                        Toast.makeText(context, "Fill all fields ❌", Toast.LENGTH_SHORT).show()
                        return@Button
                    }


                    val test = TestEntity(
                        testName = title,
                        subject = selectedSubjectId!!,
                        teacherId = 1,
                        maxMarks = maxMarks.toInt(),
                        date = SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(Date()),
                        year = selectedYear.toInt(),
                        branch = selectedBranch,
                        section = selectedSection,
                        semester = selectedSem
                    )

                    viewModel.createTest(test) {
                        Toast.makeText(context, "Test Created ✅", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CREATE TEST")
            }
        }
    }
}
