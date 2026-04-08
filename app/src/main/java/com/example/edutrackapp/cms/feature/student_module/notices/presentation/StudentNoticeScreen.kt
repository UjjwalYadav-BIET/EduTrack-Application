package com.example.edutrackapp.cms.feature.student_module.notices.presentation

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
import com.example.edutrackapp.cms.feature.notices.NoticePriority
import com.example.edutrackapp.cms.feature.notices.openAttachment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private val StudentTeal = Color(0xFF009688)
private val BgPage      = Color(0xFFF0F4F4)
private val CardWhite   = Color(0xFFFFFFFF)
private val TextDark    = Color(0xFF1A1A2E)
private val TextGray    = Color(0xFF8A8A9A)
private val RedHigh     = Color(0xFFE74C3C)
private val OrangeMed   = Color(0xFFF39C12)
private val GreenLow    = Color(0xFF2ECC71)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentNoticeScreen(
    navController: NavController,
    viewModel: StudentNoticeViewModel = hiltViewModel()
) {
    val notices by viewModel.notices.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Campus Notices", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${notices.size} active notices", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudentTeal)
            )
        }
    ) { paddingValues ->
        if (notices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(BgPage), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, tint = TextGray, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No notices yet", color = TextGray, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Check back later", color = TextGray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(paddingValues).background(BgPage),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notices, key = { it.id }) { notice ->
                    StudentNoticeCard(notice = notice)
                }
            }
        }
    }
}

@Composable
fun StudentNoticeCard(notice: Notice) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val priorityColor = when (notice.priority) {
        NoticePriority.URGENT.value -> RedHigh
        NoticePriority.LOW.value    -> GreenLow
        else                        -> OrangeMed
    }
    val priorityLabel = when (notice.priority) {
        NoticePriority.URGENT.value -> "Urgent"
        NoticePriority.LOW.value    -> "Low"
        else                        -> "Normal"
    }
    val roleIcon = if (notice.postedByRole == "admin") Icons.Default.AdminPanelSettings else Icons.Default.School

    val dateStr = remember(notice.createdAt) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(notice.createdAt.toDate())
    }

    Card(colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(priorityColor))
                Spacer(Modifier.width(10.dp))
                Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(roleIcon, null, tint = TextGray, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text("${notice.postedBy}  ·  $dateStr", fontSize = 11.sp, color = TextGray)
            }

            Spacer(Modifier.height(10.dp))
            Text(notice.message, fontSize = 14.sp, lineHeight = 20.sp, color = TextDark)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(priorityColor.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(priorityLabel, fontSize = 10.sp, color = priorityColor, fontWeight = FontWeight.SemiBold)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(StudentTeal.copy(alpha = 0.10f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(if (notice.postedByRole == "admin") "Admin" else "Teacher", fontSize = 10.sp, color = StudentTeal, fontWeight = FontWeight.SemiBold)
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
                    border         = androidx.compose.foundation.BorderStroke(1.dp, StudentTeal),
                    colors         = ButtonDefaults.outlinedButtonColors(contentColor = StudentTeal),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier       = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.FileOpen, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View Attachment", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}