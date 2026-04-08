package com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

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
private val Violet       = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherNoticesListScreen(
    navController: NavController,
    viewModel: TeacherNoticeViewModel = hiltViewModel()
) {
    val notices by viewModel.notices.collectAsState()

    Scaffold(
        containerColor = BgLight,
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().background(DarkNavy)) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Notices", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                            Text("${notices.size} notice${if (notices.size == 1) "" else "s"} available", fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("create_notice") }) {
                            Icon(Icons.Default.Add, contentDescription = "Post Notice", tint = AccentYellow)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("create_notice") }, containerColor = DarkNavy, contentColor = AccentYellow, shape = CircleShape) {
                Icon(Icons.Default.Add, contentDescription = "Post Notice")
            }
        }
    ) { paddingValues ->
        if (notices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(DarkNavy.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Campaign, null, tint = TextGray, modifier = Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("No notices yet", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Tap + to post a notice to students", color = TextGray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notices, key = { it.id }) { notice ->
                    TeacherNoticeCard(notice = notice)
                }
            }
        }
    }
}

@Composable
fun TeacherNoticeCard(notice: Notice) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val priorityColor = when (notice.priority) {
        NoticePriority.URGENT.value -> RedHigh
        NoticePriority.LOW.value    -> GreenLow
        else                        -> OrangeMed
    }
    val audienceColor = when (notice.audience) {
        NoticeAudience.TEACHERS.value -> CyanBlue
        NoticeAudience.STUDENTS.value -> GreenLow
        else                          -> Violet
    }
    val audienceLabel = when (notice.audience) {
        NoticeAudience.TEACHERS.value -> "Teachers"
        NoticeAudience.STUDENTS.value -> "Students"
        else                          -> "Everyone"
    }
    val dateStr = remember(notice.createdAt) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(notice.createdAt.toDate())
    }

    Card(colors = CardDefaults.cardColors(containerColor = CardWhite), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(priorityColor))
                Spacer(Modifier.width(10.dp))
                Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
            Text(notice.message, fontSize = 13.sp, color = TextGray, lineHeight = 19.sp)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(audienceColor.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(audienceLabel, fontSize = 10.sp, color = audienceColor, fontWeight = FontWeight.SemiBold)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(priorityColor.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(notice.priority.replaceFirstChar { it.uppercase() }, fontSize = 10.sp, color = priorityColor, fontWeight = FontWeight.SemiBold)
                }
                if (notice.attachmentUrl.isNotEmpty()) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(CyanBlue.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachFile, null, tint = CyanBlue, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Attachment", fontSize = 10.sp, color = CyanBlue, fontWeight = FontWeight.SemiBold)
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
                    border         = androidx.compose.foundation.BorderStroke(1.dp, CyanBlue),
                    colors         = ButtonDefaults.outlinedButtonColors(contentColor = CyanBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier       = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.FileOpen, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open Attachment", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, tint = TextGray, modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(4.dp))
                Text("$dateStr  ·  ${notice.postedBy}", fontSize = 11.sp, color = TextGray)
            }
        }
    }
}