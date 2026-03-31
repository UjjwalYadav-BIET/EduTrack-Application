package com.example.edutrackapp.cms.feature.teacher_Module.notices.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.core.data.local.entity.NoticeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherNoticeListScreen(
    navController: NavController,
    viewModel: TeacherNoticeViewModel = hiltViewModel()
) {

    val notices = viewModel.notices.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Notices") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (notices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No notices posted yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notices) { notice ->
                    TeacherNoticeCard(notice, viewModel)
                }
            }
        }
    }
}

@Composable
fun TeacherNoticeCard(
    notice: NoticeEntity,
    viewModel: TeacherNoticeViewModel
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notice.isActive) Color.White else Color.LightGray
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = notice.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = notice.description)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Target: ${notice.targetBranch}-${notice.targetSection}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔥 Status + Toggle Button
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = if (notice.isActive) "Active" else "Inactive",
                    color = if (notice.isActive) Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        viewModel.toggleNoticeStatus(notice)
                    }
                ) {
                    Text(
                        if (notice.isActive) "Deactivate" else "Activate"
                    )
                }
            }
        }
    }
}