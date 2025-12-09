package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.SubmissionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 1. ViewModel to fetch submissions
@HiltViewModel
class TeacherSubmissionViewModel @Inject constructor(
    private val database: EduTrackDatabase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // We get the assignmentId from the Navigation arguments
    private val assignmentId: Int = checkNotNull(savedStateHandle["assignmentId"])

    val submissions = database.submissionDao.getSubmissionsForAssignment(assignmentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

// 2. The Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherSubmissionScreen(
    navController: NavController,
    viewModel: TeacherSubmissionViewModel = hiltViewModel()
) {
    val submissionList = viewModel.submissions.collectAsState().value
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Submissions") },
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
        if (submissionList.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No submissions yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(submissionList) { submission ->
                    SubmissionCard(submission) {
                        // --- UPDATED UNIVERSAL OPEN LOGIC ---
                        try {
                            val uri = Uri.parse(submission.fileUri)

                            // 1. Auto-detect the file type (PDF, Image, etc.)
                            // This ensures we don't force "PDF" if it's an Image
                            val mimeType = context.contentResolver.getType(uri) ?: "*/*"

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, mimeType) // Use detected type
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_ACTIVITY_NO_HISTORY
                            }

                            // 2. Open the Chooser Menu (Force "Open With...")
                            val chooser = Intent.createChooser(intent, "Open File With...")
                            context.startActivity(chooser)

                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open this file format.", Toast.LENGTH_SHORT).show()
                        }
                        // ------------------------------------
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionCard(submission: SubmissionEntity, onOpenClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Roll No: ${submission.studentRollNo}",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Submitted: ${submission.submissionDate}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onOpenClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open", color = Color.Black)
            }
        }
    }
}