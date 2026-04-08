package com.example.edutrackapp.cms.feature.admin_module.notices

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.edutrackapp.cms.feature.notices.NoticeAudience
import com.example.edutrackapp.cms.feature.notices.NoticePriority

// ─── Brand Palette ────────────────────────────────────────────────────────────
private val Slate900  = Color(0xFF0F172A)
private val Slate800  = Color(0xFF1E293B)
private val Slate700  = Color(0xFF334155)
private val Amber     = Color(0xFFF59E0B)
private val Emerald   = Color(0xFF10B981)
private val Violet    = Color(0xFF8B5CF6)
private val Cyan      = Color(0xFF06B6D4)
private val RedHigh   = Color(0xFFEF4444)
private val TextWhite = Color(0xFFE2E8F0)
private val TextMuted = Color(0xFF94A3B8)

private val audienceColors = mapOf(
    NoticeAudience.ALL      to Violet,
    NoticeAudience.TEACHERS to Cyan,
    NoticeAudience.STUDENTS to Emerald
)
private val priorityColors = mapOf(
    NoticePriority.LOW    to Emerald,
    NoticePriority.NORMAL to Amber,
    NoticePriority.URGENT to RedHigh
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostNoticeScreen(
    navController: NavController,
    viewModel: AdminNoticeViewModel = hiltViewModel()
) {
    val context   = LocalContext.current
    val uiState   by viewModel.uiState.collectAsState()
    val isLoading = uiState is NoticeUiState.Loading || uiState is NoticeUiState.Uploading

    // File picker launcher
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
        if (uiState is NoticeUiState.Error) {
            Toast.makeText(context, (uiState as NoticeUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = Slate900,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Post Notice", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Broadcast to students or faculty", fontSize = 12.sp, color = TextMuted)
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

            // ── Icon Banner ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Amber.copy(alpha = 0.15f))
                    .border(2.dp, Amber.copy(alpha = 0.4f), CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Campaign, null, tint = Amber, modifier = Modifier.size(34.dp))
            }

            // ── Notice Content ────────────────────────────────────────────────
            AdminSectionLabel("Notice Content")

            OutlinedTextField(
                value         = viewModel.noticeTitle.value,
                onValueChange = viewModel::onTitleChange,
                label         = { Text("Notice Title *", color = TextMuted) },
                leadingIcon   = { Icon(Icons.Default.Title, null, tint = Amber) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(14.dp),
                colors        = adminFieldColors()
            )

            OutlinedTextField(
                value         = viewModel.noticeMessage.value,
                onValueChange = viewModel::onMessageChange,
                label         = { Text("Message / Details *", color = TextMuted) },
                leadingIcon   = {
                    Icon(Icons.Default.Message, null, tint = Amber,
                        modifier = Modifier.padding(top = 14.dp))
                },
                minLines = 5, maxLines = 10,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                colors   = adminFieldColors()
            )

            // ── Priority ──────────────────────────────────────────────────────
            AdminSectionLabel("Priority Level")

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NoticePriority.values().forEach { p ->
                    val selected = viewModel.noticePriority.value == p
                    val color    = priorityColors[p] ?: Amber
                    Surface(
                        onClick         = { viewModel.onPriorityChange(p) },
                        modifier        = Modifier.weight(1f),
                        shape           = RoundedCornerShape(12.dp),
                        color           = if (selected) color.copy(alpha = 0.15f) else Slate800,
                        border          = BorderStroke(
                            if (selected) 1.5.dp else 1.dp,
                            if (selected) color else Slate700
                        )
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
                                color      = if (selected) color else TextMuted
                            )
                        }
                    }
                }
            }

            // ── Target Audience ───────────────────────────────────────────────
            AdminSectionLabel("Target Audience")

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NoticeAudience.values().forEach { audience ->
                    val selected = viewModel.noticeTarget.value == audience
                    val color    = audienceColors[audience] ?: Violet
                    val icon     = when (audience) {
                        NoticeAudience.ALL      -> Icons.Default.Group
                        NoticeAudience.TEACHERS -> Icons.Default.School
                        NoticeAudience.STUDENTS -> Icons.Default.People
                    }
                    Surface(
                        onClick  = { viewModel.onTargetChange(audience) },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        color    = if (selected) color.copy(alpha = 0.15f) else Slate800,
                        border   = BorderStroke(
                            if (selected) 1.5.dp else 1.dp,
                            if (selected) color else Slate700
                        )
                    ) {
                        Column(
                            modifier            = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, null, tint = if (selected) color else TextMuted,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                audience.label,
                                fontSize   = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color      = if (selected) color else TextMuted
                            )
                        }
                    }
                }
            }

            // Audience info note
            val noteColor = audienceColors[viewModel.noticeTarget.value] ?: Violet
            val noteText  = when (viewModel.noticeTarget.value) {
                NoticeAudience.ALL      -> "This notice will be visible to all users."
                NoticeAudience.TEACHERS -> "Only faculty members will see this notice."
                NoticeAudience.STUDENTS -> "Only enrolled students will see this notice."
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(noteColor.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, tint = noteColor, modifier = Modifier.size(14.dp))
                Text(noteText, fontSize = 12.sp, color = noteColor)
            }

            // ── Attachment ────────────────────────────────────────────────────
            AdminSectionLabel("Attachment (Optional)")

            Surface(
                onClick  = { filePicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                color    = Slate800,
                border   = BorderStroke(
                    1.dp,
                    if (viewModel.attachmentUri.value != null) Emerald else Slate700
                )
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
                        tint = if (viewModel.attachmentUri.value != null) Emerald else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (viewModel.attachmentName.value.isNotEmpty())
                                viewModel.attachmentName.value else "Tap to attach a file",
                            fontSize = 13.sp,
                            color    = if (viewModel.attachmentUri.value != null) TextWhite else TextMuted
                        )
                        if (viewModel.attachmentUri.value != null) {
                            Text("Tap to change", fontSize = 11.sp, color = TextMuted)
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

            // ── Upload indicator ──────────────────────────────────────────────
            if (uiState is NoticeUiState.Uploading) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(color = Emerald, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text("Uploading attachment...", fontSize = 12.sp, color = Emerald)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Submit Button ─────────────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.postNotice {
                        Toast.makeText(context, "Notice Published!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                enabled        = !isLoading,
                modifier       = Modifier.fillMaxWidth().height(56.dp),
                shape          = RoundedCornerShape(16.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (!isLoading)
                                Brush.horizontalGradient(listOf(Amber, RedHigh))
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
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Campaign, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("PUBLISH NOTICE", fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
@Composable
fun AdminSectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp).height(16.dp)
                .background(Color(0xFFF59E0B), RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE2E8F0))
    }
}

@Composable
fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Color(0xFFF59E0B),
    unfocusedBorderColor    = Color(0xFF334155),
    focusedContainerColor   = Color(0xFF1E293B),
    unfocusedContainerColor = Color(0xFF1E293B),
    focusedTextColor        = Color(0xFFE2E8F0),
    unfocusedTextColor      = Color(0xFFE2E8F0),
    cursorColor             = Color(0xFFF59E0B)
)