package com.example.edutrackapp.cms.feature.student_module.assignments.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import com.example.edutrackapp.cms.core.data.local.entity.SubmissionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext // <--- NEEDED FOR CONTEXT IN VIEWMODEL
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// 1. Updated ViewModel
@HiltViewModel
class StudentAssignmentListViewModel @Inject constructor(
    private val database: EduTrackDatabase,
    @ApplicationContext private val context: Context // <--- INJECT CONTEXT HERE
) : ViewModel() {

    private val studentRollNo = "CS-101"

    val uiState = combine(
        database.assignmentDao.getAllAssignments(),
        database.submissionDao.getStudentSubmissions(studentRollNo)
    ) { assignments, submissions ->
        assignments.map { assignment ->
            val isSubmitted = submissions.any { it.assignmentId == assignment.id }
            AssignmentUiState(assignment, isSubmitted)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitAssignment(assignmentId: Int, fileUri: Uri) {
        viewModelScope.launch {
            // Persist permission so the Teacher can open it later
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(fileUri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val submission = SubmissionEntity(
                assignmentId = assignmentId,
                studentRollNo = studentRollNo,
                submissionDate = date,
                fileUri = fileUri.toString()
            )
            database.submissionDao.insertSubmission(submission)
        }
    }
}

// Helper Data Class
data class AssignmentUiState(
    val assignment: AssignmentEntity,
    val isSubmitted: Boolean
)

// 2. Updated UI Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssignmentScreen(
    navController: NavController,
    viewModel: StudentAssignmentListViewModel = hiltViewModel()
) {
    val assignmentList = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    // --- STATES FOR VIEWING DETAILS ---
    var showDialog by remember { mutableStateOf(false) }
    var selectedAssignment by remember { mutableStateOf<AssignmentEntity?>(null) }
    // ----------------------------------

    // State for Submission logic
    var assignmentIdToSubmit by remember { mutableStateOf<Int?>(null) }

    // File Picker Launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && assignmentIdToSubmit != null) {
            viewModel.submitAssignment(assignmentIdToSubmit!!, uri)
            Toast.makeText(context, "File Uploaded Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- THE DETAILS DIALOG ---
    if (showDialog && selectedAssignment != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = selectedAssignment!!.title,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF009688)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Subject: ${selectedAssignment!!.subject}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedAssignment!!.description,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- OPEN ATTACHMENT BUTTON ---
                    if (!selectedAssignment!!.attachmentUri.isNullOrEmpty()) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(selectedAssignment!!.attachmentUri)
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open file. Need PDF Viewer.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2F1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AttachFile, null, tint = Color(0xFF009688))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Attached PDF", color = Color(0xFF009688))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    // -----------------------------------

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Due Date: ${selectedAssignment!!.dueDate}", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            },
            icon = { Icon(Icons.Default.Assignment, null, tint = Color(0xFF009688)) }
        )
    }
    // --------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Assignments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF009688),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (assignmentList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No assignments found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFEDE7F6)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(assignmentList) { uiItem ->
                    StudentAssignmentCard(
                        item = uiItem,
                        onCardClick = {
                            selectedAssignment = uiItem.assignment
                            showDialog = true
                        },
                        onSubmitClick = {
                            assignmentIdToSubmit = uiItem.assignment.id
                            fileLauncher.launch("application/pdf")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentAssignmentCard(
    item: AssignmentUiState,
    onCardClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    val assignment = item.assignment
    val isSubmitted = item.isSubmitted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment, null, tint = Color(0xFF009688))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = assignment.subject,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                if (isSubmitted) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = assignment.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = assignment.description,
                color = Color.DarkGray,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Due: ${assignment.dueDate}", fontSize = 12.sp, color = Color.Red)
                }

                if (!isSubmitted) {
                    Button(
                        onClick = { onSubmitClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Submit", fontSize = 12.sp)
                    }
                } else {
                    Text("Completed", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}