package com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.titleContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoticeScreen(
    navController: NavController,
    viewModel: NoticeViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val years = listOf(1, 2, 3, 4)
    val branches = listOf("CSE", "IT", "ME")
    val sections = listOf("A", "B", "C")

    // 📎 File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val type = context.contentResolver.getType(it)

            if (type == "application/pdf" || type?.startsWith("image/") == true) {
                viewModel.attachmentUri.value = it.toString()
            } else {
                Toast.makeText(context, "Only PDF/Image allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Notice") },

                // 🔙 Back Button (Left Side)
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                // 📋 Right Side Icon (View All Notices)
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.TeacherNoticeList.route) {
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "All Notices"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 🔹 Title
            OutlinedTextField(
                value = viewModel.title.value,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // 🔹 Description
            OutlinedTextField(
                value = viewModel.description.value,
                onValueChange = viewModel::onDescChange,
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            // 🔽 Year Dropdown
            DropdownField(
                label = "Select Year",
                options = years.map { it.toString() },
                selected = viewModel.targetYear.value.toString()
            ) {
                viewModel.targetYear.value = it.toInt()
            }

            // 🔽 Branch Dropdown
            DropdownField(
                label = "Select Branch",
                options = branches,
                selected = viewModel.targetBranch.value
            ) {
                viewModel.targetBranch.value = it
            }

            // 🔽 Section Dropdown
            DropdownField(
                label = "Select Section",
                options = sections,
                selected = viewModel.targetSection.value
            ) {
                viewModel.targetSection.value = it
            }

            // 📎 Attach File Button
            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (viewModel.attachmentUri.value == null)
                        "Attach File"
                    else
                        "Change Attachment"
                )
            }

            // 📄 Show file selected
            viewModel.attachmentUri.value?.let {
                Text(
                    text = "File Selected",
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 🚀 Submit Button
            Button(
                onClick = {
                    if (viewModel.title.value.isBlank()) {
                        Toast.makeText(context, "Enter title", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (viewModel.description.value.isBlank()) {
                        Toast.makeText(context, "Enter description", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.postNotice {
                        Toast.makeText(context, "Notice Posted", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("POST NOTICE")
            }
        }
    }
}