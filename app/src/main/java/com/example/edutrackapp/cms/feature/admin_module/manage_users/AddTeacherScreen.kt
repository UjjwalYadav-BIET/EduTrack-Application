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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

private val Slate900  = Color(0xFF0F172A)
private val Slate800  = Color(0xFF1E293B)
private val Slate700  = Color(0xFF334155)
private val Cyan500   = Color(0xFF06B6D4)
private val Cyan400   = Color(0xFF22D3EE)
private val TextWhite = Color(0xFFE2E8F0)
private val TextMuted = Color(0xFF94A3B8)
private val Rose      = Color(0xFFF43F5E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeacherScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context  = LocalContext.current
    val uiState  by viewModel.uiState.collectAsState()
    val isLoading = uiState is AdminUiState.Loading

    // Observe results
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
                        Text("Add Faculty", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Register new teacher", fontSize = 12.sp, color = TextMuted)
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
                    .background(Cyan500.copy(alpha = 0.15f))
                    .border(2.dp, Cyan500.copy(alpha = 0.4f), CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.name.value.firstOrNull()?.uppercaseChar()?.toString() ?: "T",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cyan500
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            SectionLabel("Personal Info")

            AdminField(
                value = viewModel.name.value,
                onValueChange = viewModel::onNameChange,
                label = "Full Name",
                icon = Icons.Default.Person
            )
            AdminField(
                value = viewModel.email.value,
                onValueChange = viewModel::onEmailChange,
                label = "Email Address",
                icon = Icons.Default.Email
            )
            AdminField(
                value = viewModel.phone.value,
                onValueChange = viewModel::onPhoneChange,
                label = "Phone (with country code)",
                icon = Icons.Default.Phone
            )
            AdminField(
                value = viewModel.password.value,
                onValueChange = viewModel::onPassChange,
                label = "Set Password",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(4.dp))
            SectionLabel("Academic Info")

            AdminField(
                value = viewModel.department.value,
                onValueChange = viewModel::onDeptChange,
                label = "Department",
                icon = Icons.Default.Business
            )
            AdminField(
                value = viewModel.employeeId.value,
                onValueChange = viewModel::onEmpIdChange,
                label = "Employee ID",
                icon = Icons.Default.Badge
            )
            AdminField(
                value = viewModel.subject.value,
                onValueChange = viewModel::onSubjectChange,
                label = "Subject / Specialization",
                icon = Icons.Default.Book
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.createTeacherAccount {
                        Toast.makeText(context, "Teacher Registered!", Toast.LENGTH_SHORT).show()
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
                                Brush.horizontalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF6366F1)))
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
                            Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("REGISTER TEACHER", fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionLabel(label: String) {
    Text(
        text = label.uppercase(),
        fontSize = 11.sp,
        color = Cyan400,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp
    )
}

@Composable
fun AdminField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted) },
        leadingIcon = { Icon(icon, null, tint = Cyan500) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor    = Cyan500,
            unfocusedBorderColor  = Slate700,
            focusedContainerColor = Slate800,
            unfocusedContainerColor = Slate800,
            focusedTextColor      = TextWhite,
            unfocusedTextColor    = TextWhite,
            cursorColor           = Cyan500
        )
    )
}