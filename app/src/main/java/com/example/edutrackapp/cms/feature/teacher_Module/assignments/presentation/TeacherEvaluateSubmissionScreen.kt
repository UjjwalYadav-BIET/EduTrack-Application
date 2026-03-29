package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.isTraceInProgress
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentSubmissionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherEvaluateSubmissionScreen(
    navController: NavController,
    viewModel: TeacherEvaluateViewModel = hiltViewModel() // ✅ ONLY ONE VM
) {
    val context = LocalContext.current
    val submissionState = viewModel.submission.collectAsState().value

    // ⛔ Prevent crash
    if (submissionState == null) {
        Text("Loading...")
        return
    }

    val marks = remember { mutableStateOf(submissionState.marks?.toString() ?: "") }
    val feedback = remember { mutableStateOf(submissionState.feedback ?: "") }
    val marksTouched = remember { mutableStateOf(false) }
    val number = marks.value.toIntOrNull()
    val isError = marksTouched.value && (marks.value.isBlank() || number == null || number > 10)


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluate Submission") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Student Roll No: ${submissionState.studentRollNo}", fontWeight = FontWeight.Bold)
                    Text("Submitted: ${submissionState.submissionDate}", color = Color.Gray)
                    Text("Status: ${submissionState.status}", color = Color(0xFF6200EE))
                }
            }

            Button(
                onClick = {
                    try {
                        val uri = Uri.parse(submissionState.fileUri)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "*/*")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Submission File")
            }


            OutlinedTextField(
                value = marks.value,
                onValueChange = { if (it.all { char -> char.isDigit() }) marks.value = it },
                label = { Text("Enter Marks") },
                isError = isError,
                trailingIcon = { if (isError) Icon(Icons.Default.Warning, null, tint = Color.Red) },
                supportingText = { if (isError) Text("Marks should be between 0 and 10", color = Color.Red) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) marksTouched.value = true },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            val feedbackTouched = remember { mutableStateOf(false) }
            val wordCount = feedback.value.trim().split("\\s+".toRegex()).size
            val isFeedbackError = feedbackTouched.value && (feedback.value.isBlank() || wordCount > 20)

            val maxWords = 20

            OutlinedTextField(
                value = feedback.value,
                onValueChange = { text ->
                    val words = text.trim().split("\\s+".toRegex())
                    if (words.size <= maxWords) feedback.value = text
                },
                label = { Text("Feedback") },
                isError = isFeedbackError,
                trailingIcon = { if (isFeedbackError) Icon(Icons.Default.Warning, null, tint = Color.Red) },
                supportingText = {
                    when {
                        isFeedbackError && feedback.value.isBlank() -> Text("Feedback cannot be empty", color = Color.Red)
                        isFeedbackError && wordCount > maxWords -> Text("Maximum $maxWords words allowed", color = Color.Red)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .onFocusChanged { if (it.isFocused) feedbackTouched.value = true },
                maxLines = 4
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val marksInt = marks.value.toIntOrNull()
                    val marksInvalid = marksInt == null || marksInt > 10
                    val wordCount = feedback.value.trim().split("\\s+".toRegex()).size
                    val feedbackInvalid = feedback.value.isBlank() || wordCount > maxWords

                    if (marksInvalid || feedbackInvalid) {
                        val errorMessage = when {
                            marksInvalid && feedbackInvalid -> "Enter valid marks and feedback"
                            marksInvalid -> "Enter valid marks (0-10)"
                            else -> "Enter valid feedback"
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        return@Button  // ✅ Stop execution, stay on page
                    }

                    // Safe to submit
                    viewModel.evaluateSubmission(marksInt, feedback.value)
                    Toast.makeText(context, "Evaluation Saved", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Submit Evaluation")
            }
        }
    }
}