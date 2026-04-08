package com.example.edutrackapp.cms.feature.admin_module.notices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.edutrackapp.cms.feature.notices.Notice
import com.example.edutrackapp.cms.feature.notices.NoticeAudience
import com.example.edutrackapp.cms.feature.notices.NoticePriority
import com.example.edutrackapp.cms.feature.notices.openAttachment
import com.example.edutrackapp.cms.ui.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNoticesListScreen(
    navController: NavController,
    viewModel: AdminNoticeViewModel = hiltViewModel()
) {
    val notices by viewModel.notices.collectAsState()

    Scaffold(
        containerColor = Slate900,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Notices", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("${notices.size} notices posted", fontSize = 12.sp, color = TextMuted)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.PostNotice.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Post Notice", tint = Amber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate800)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { navController.navigate(Screen.PostNotice.route) },
                containerColor = Amber,
                contentColor   = Slate900,
                shape          = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post Notice")
            }
        }
    ) { paddingValues ->
        if (notices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Campaign, null, tint = TextMuted, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No notices yet", color = TextMuted, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Tap + to post the first notice", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notices, key = { it.id }) { notice ->
                    AdminNoticeCard(notice = notice, onDelete = { viewModel.deleteNotice(notice.id) })
                }
            }
        }
    }
}

@Composable
fun AdminNoticeCard(notice: Notice, onDelete: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Notice?", fontWeight = FontWeight.Bold) },
            text  = { Text("This will remove the notice for all users.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = RedHigh, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    val priorityColor = when (notice.priority) {
        NoticePriority.URGENT.value -> RedHigh
        NoticePriority.LOW.value    -> Emerald
        else                        -> Amber
    }
    val audienceColor = when (notice.audience) {
        NoticeAudience.TEACHERS.value -> Cyan
        NoticeAudience.STUDENTS.value -> Emerald
        else                          -> Violet
    }
    val audienceLabel = when (notice.audience) {
        NoticeAudience.TEACHERS.value -> "Teachers"
        NoticeAudience.STUDENTS.value -> "Students"
        else                          -> "Everyone"
    }
    val dateStr = remember(notice.createdAt) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(notice.createdAt.toDate())
    }

    Card(
        colors    = CardDefaults.cardColors(containerColor = Slate800),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(priorityColor))
                Spacer(Modifier.width(10.dp))
                Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextWhite, modifier = Modifier.weight(1f))
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null, tint = RedHigh.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(notice.message, fontSize = 13.sp, color = TextMuted, lineHeight = 19.sp)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(audienceColor.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(audienceLabel, fontSize = 10.sp, color = audienceColor, fontWeight = FontWeight.SemiBold)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(priorityColor.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(notice.priority.replaceFirstChar { it.uppercase() }, fontSize = 10.sp, color = priorityColor, fontWeight = FontWeight.SemiBold)
                }
                if (notice.attachmentUrl.isNotEmpty()) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Cyan.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachFile, null, tint = Cyan, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Attachment", fontSize = 10.sp, color = Cyan, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (notice.attachmentUrl.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            openAttachment(
                                context    = context,
                                base64Data = notice.attachmentUrl,
                                fileName   = notice.attachmentName.ifBlank { "attachment" },
                                mimeType   = notice.attachmentMime.ifBlank { "application/octet-stream" }
                            )
                        }
                    },
                    shape          = RoundedCornerShape(10.dp),
                    border         = androidx.compose.foundation.BorderStroke(1.dp, Cyan),
                    colors         = ButtonDefaults.outlinedButtonColors(contentColor = Cyan),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier       = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.FileOpen, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open Attachment", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("$dateStr  ·  By ${notice.postedBy}", fontSize = 11.sp, color = TextMuted)
        }
    }
}