package com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.feature.notices.NoticePriority

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
private val CyanBlue     = Color(0xFF06B6D4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoticeScreen(
    navController: NavController,
    viewModel: TeacherNoticeViewModel = hiltViewModel()
) {
    val context      = LocalContext.current
    val uiState      by viewModel.uiState.collectAsState()
    val teacherName  by viewModel.teacherName.collectAsState()
    val scrollState  = rememberScrollState()
    val isLoading    = uiState is TeacherNoticeUiState.Loading || uiState is TeacherNoticeUiState.Uploading

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val name = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(idx)
            } ?: "attachment"
            viewModel.onAttachmentPicked(it, name)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is TeacherNoticeUiState.Error) {
            Toast.makeText(context, (uiState as TeacherNoticeUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
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
                            Text("Post Notice", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                            Text("For Students Only  •  Fill in all fields", fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Audience info banner (fixed — teachers post to students only) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenLow.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.People, null, tint = GreenLow, modifier = Modifier.size(20.dp))
                Column {
                    Text("Posting to: Students Only", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GreenLow)
                    Text("As a teacher, your notices go to students only.", fontSize = 11.sp, color = TextGray)
                }
            }

            // ── Notice Info ───────────────────────────────────────────────────
            NoticeSectionHeader("Notice Info")

            OutlinedTextField(
                value         = viewModel.title.value,
                onValueChange = viewModel::onTitleChange,
                label         = { Text("Notice Title *") },
                leadingIcon   = { Icon(Icons.Default.Campaign, null, tint = AccentYellow) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(14.dp),
                singleLine    = true,
                isError       = viewModel.titleError.value,
                supportingText = {
                    if (viewModel.titleError.value) Text("Title is required", color = RedHigh, fontSize = 11.sp)
                },
                colors = noticeFieldColors()
            )

            OutlinedTextField(
                value         = viewModel.description.value,
                onValueChange = viewModel::onDescChange,
                label         = { Text("Details / Description *") },
                leadingIcon   = { Icon(Icons.Default.Description, null, tint = AccentYellow) },
                modifier      = Modifier.fillMaxWidth().height(140.dp),
                shape         = RoundedCornerShape(14.dp),
                isError       = viewModel.descError.value,
                supportingText = {
                    if (viewModel.descError.value) Text("Description is required", color = RedHigh, fontSize = 11.sp)
                },
                maxLines = 8,
                colors   = noticeFieldColors()
            )

            // ── Priority ──────────────────────────────────────────────────────
            NoticeSectionHeader("Priority Level")

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NoticePriority.values().forEach { p ->
                    val selected = viewModel.priority.value == p
                    val color = when (p) {
                        NoticePriority.URGENT -> RedHigh
                        NoticePriority.NORMAL -> OrangeMed
                        NoticePriority.LOW    -> GreenLow
                    }
                    Surface(
                        onClick         = { viewModel.onPriorityChange(p) },
                        modifier        = Modifier.weight(1f),
                        shape           = RoundedCornerShape(12.dp),
                        color           = if (selected) color.copy(alpha = 0.15f) else CardWhite,
                        shadowElevation = if (selected) 0.dp else 2.dp,
                        border          = if (selected) BorderStroke(1.5.dp, color) else null
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

            // ── Attachment ────────────────────────────────────────────────────
            NoticeSectionHeader("Attachment (Optional)")

            Surface(
                onClick  = { filePicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                color    = CardWhite,
                border   = BorderStroke(
                    1.dp,
                    if (viewModel.attachmentUri.value != null) GreenLow else Color(0xFFDDDDDD)
                ),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.attachmentUri.value != null)
                            Icons.Default.AttachFile else Icons.Default.Upload,
                        contentDescription = null,
                        tint     = if (viewModel.attachmentUri.value != null) GreenLow else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text     = if (viewModel.attachmentName.value.isNotEmpty())
                                viewModel.attachmentName.value else "Tap to attach a file",
                            fontSize = 13.sp,
                            color    = if (viewModel.attachmentUri.value != null) TextDark else TextGray
                        )
                        if (viewModel.attachmentUri.value != null) {
                            Text("Tap to change", fontSize = 11.sp, color = TextGray)
                        }
                    }
                    if (viewModel.attachmentUri.value != null) {
                        IconButton(
                            onClick  = { viewModel.onAttachmentPicked(null, "") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = RedHigh, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Upload progress
            if (uiState is TeacherNoticeUiState.Uploading) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(color = GreenLow, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text("Uploading attachment...", fontSize = 12.sp, color = GreenLow)
                }
            }

            // ── Posted By ─────────────────────────────────────────────────────
            NoticeSectionHeader("Posted By")

            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(14.dp),
                color           = CardWhite,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            // Real initial from Firestore
                            text = if (teacherName.isNotEmpty()) teacherName.first().uppercaseChar().toString() else "T",
                            color = AccentYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        // Real name from Firestore
                        Text(teacherName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                        Text("Teacher  •  Posting to Students", fontSize = 11.sp, color = TextGray)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.VerifiedUser, null, tint = GreenLow, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Broadcast Button ──────────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.postNotice {
                        Toast.makeText(context, "Notice Posted!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                enabled   = !isLoading,
                modifier  = Modifier.fillMaxWidth().height(54.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                shape     = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("BROADCAST NOTICE", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
@Composable
fun NoticeSectionHeader(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp).height(16.dp)
                .background(Color(0xFFFFB800), RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1A1A2E), letterSpacing = 0.3.sp)
    }
}

@Composable
fun noticeFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Color(0xFFFFB800),
    unfocusedBorderColor    = Color(0xFFDDDDDD),
    focusedLabelColor       = Color(0xFFFFB800),
    cursorColor             = Color(0xFFFFB800),
    focusedContainerColor   = Color.White,
    unfocusedContainerColor = Color.White
)