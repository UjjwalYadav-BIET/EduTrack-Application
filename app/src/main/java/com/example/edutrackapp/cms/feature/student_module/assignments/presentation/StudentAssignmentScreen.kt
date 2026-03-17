package com.example.edutrackapp.cms.feature.student_module.assignments.presentation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity


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
                                    val uri = Uri.parse(selectedAssignment!!.attachmentUri)

                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }

                                    context.startActivity(Intent.createChooser(intent, "Open PDF"))

                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Cannot open file. Please install a PDF viewer.",
                                        Toast.LENGTH_SHORT
                                    ).show()
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