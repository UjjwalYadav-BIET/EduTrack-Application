package com.example.edutrackapp.cms.feature.admin_module.manage_users

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

private val Slate900  = Color(0xFF0F172A)
private val Slate800  = Color(0xFF1E293B)
private val Slate700  = Color(0xFF334155)
private val Emerald   = Color(0xFF10B981)
private val EmeraldL  = Color(0xFF34D399)
private val TextWhite = Color(0xFFE2E8F0)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context   = LocalContext.current
    val uiState   by viewModel.uiState.collectAsState()
    val isLoading = uiState is AdminUiState.Loading

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUiState.Error -> {
                Toast.makeText(context, (uiState as AdminUiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = Slate900,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Enroll Student", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Register a new student", fontSize = 12.sp, color = TextMuted)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate800)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar Preview
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Emerald.copy(alpha = 0.15f))
                    .border(2.dp, Emerald.copy(alpha = 0.4f), CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.studentName.value.firstOrNull()?.uppercaseChar()?.toString() ?: "S",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Emerald
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            SectionLabel("Personal Info")

            AdminField(
                value = viewModel.studentName.value,
                onValueChange = viewModel::onStNameChange,
                label = "Full Name",
                icon = Icons.Default.Person
            )
            AdminField(
                value = viewModel.studentEmail.value,
                onValueChange = viewModel::onStEmailChange,
                label = "Email Address",
                icon = Icons.Default.Email
            )
            AdminField(
                value = viewModel.studentPhone.value,
                onValueChange = viewModel::onStPhoneChange,
                label = "Phone (with country code)",
                icon = Icons.Default.Phone
            )
            AdminField(
                value = viewModel.studentPassword.value,
                onValueChange = viewModel::onStPassChange,
                label = "Set Password",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(4.dp))
            SectionLabel("Academic Info")

            AdminField(
                value = viewModel.studentDepartment.value,
                onValueChange = viewModel::onStDeptChange,
                label = "Department / Branch",
                icon = Icons.Default.Business
            )
            AdminField(
                value = viewModel.studentEnrollmentId.value,
                onValueChange = viewModel::onStEnrollmentChange,
                label = "Enrollment ID",
                icon = Icons.Default.Badge
            )
            AdminField(
                value = viewModel.studentRollNo.value,
                onValueChange = viewModel::onStRollChange,
                label = "Roll Number (e.g. CS-105)",
                icon = Icons.Default.Numbers
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.createStudentAccount {
                        Toast.makeText(context, "Student Enrolled!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (!isLoading)
                                Brush.horizontalGradient(listOf(Emerald, Color(0xFF0EA5E9)))
                            else
                                Brush.horizontalGradient(listOf(Slate700, Slate700)),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.School, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("ENROLL STUDENT", fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}