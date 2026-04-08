package com.example.edutrackapp.cms.feature.student_module.assignments.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import com.example.edutrackapp.cms.core.data.local.entity.SubmissionEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ---- Data Class ----
data class AssignmentUiState(
    val assignment: AssignmentEntity,
    val firestoreId: String,
    val isSubmitted: Boolean,
    val marks: String = "",
    val status: String = "submitted"
)

// ---- ViewModel ----
@HiltViewModel
class StudentAssignmentListViewModel @Inject constructor(
    private val database: EduTrackDatabase,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<AssignmentUiState>>(emptyList())
    val uiState: StateFlow<List<AssignmentUiState>> = _uiState.asStateFlow()

    private val _studentRollNo = MutableStateFlow("")
    private val _studentName   = MutableStateFlow("")
    private val _isLoading     = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { loadStudentThenListen() }

    private fun loadStudentThenListen() {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val userDoc = db.collection("users").document(uid).get().await()
                _studentRollNo.value = userDoc.getString("enrollmentId") ?: ""
                _studentName.value   = userDoc.getString("name") ?: ""
                listenToAssignments()
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun listenToAssignments() {
        db.collection("assignments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                viewModelScope.launch {
                    val rollNo = _studentRollNo.value
                    // fetch submissions for this student from Firestore
                    val submissionsSnap = db.collection("submissions")
                        .whereEqualTo("studentRollNo", rollNo)
                        .get().await()
                    val submittedIds = submissionsSnap.documents.associate {
                        it.getString("assignmentId")!! to
                                Pair(it.getString("marks") ?: "", it.getString("status") ?: "submitted")
                    }

                    _uiState.value = snapshot.documents.mapNotNull { doc ->
                        val stableId = doc.id.hashCode()
                        val entity = AssignmentEntity(
                            id            = stableId,
                            title         = doc.getString("title")         ?: return@mapNotNull null,
                            subject       = doc.getString("subject")       ?: "",
                            description   = doc.getString("description")   ?: "",
                            dueDate       = doc.getString("dueDate")       ?: "",
                            batch         = doc.getString("batch")         ?: "",
                            attachmentUri = doc.getString("attachmentUri")?.takeIf { it.isNotEmpty() }
                        )
                        val submissionInfo = submittedIds[doc.id]
                        AssignmentUiState(
                            assignment  = entity,
                            firestoreId = doc.id,
                            isSubmitted = submissionInfo != null,
                            marks       = submissionInfo?.first ?: "",
                            status      = submissionInfo?.second ?: "submitted"
                        )
                    }
                    _isLoading.value = false
                }
            }
    }

    fun submitAssignment(firestoreAssignmentId: String, fileUri: Uri) {
        viewModelScope.launch {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(fileUri, takeFlags)
            } catch (e: Exception) { e.printStackTrace() }

            val rollNo = _studentRollNo.value
            val name   = _studentName.value
            val uid    = auth.currentUser?.uid ?: ""
            val date   = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            // Save to Firestore submissions collection
            val submissionData = mapOf(
                "assignmentId"   to firestoreAssignmentId,
                "studentRollNo"  to rollNo,
                "studentName"    to name,
                "studentUid"     to uid,
                "fileUri"        to fileUri.toString(),
                "submissionDate" to date,
                "marks"          to "",
                "status"         to "submitted"
            )
            val ref = db.collection("submissions").add(submissionData).await()

            // Also cache locally in Room
            database.submissionDao.insertSubmission(
                SubmissionEntity(
                    firestoreId    = ref.id,
                    assignmentId   = firestoreAssignmentId,
                    studentRollNo  = rollNo,
                    studentName    = name,
                    studentUid     = uid,
                    fileUri        = fileUri.toString(),
                    submissionDate = date,
                    marks          = "",
                    status         = "submitted"
                )
            )
        }
    }
}

// ---- UI Screen ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssignmentScreen(
    navController: NavController,
    viewModel: StudentAssignmentListViewModel = hiltViewModel()
) {
    val assignmentList = viewModel.uiState.collectAsState().value
    val isLoading      = viewModel.isLoading.collectAsState().value
    val context        = LocalContext.current

    var showDialog        by remember { mutableStateOf(false) }
    var selectedUiState   by remember { mutableStateOf<AssignmentUiState?>(null) }
    var firestoreIdToSubmit by remember { mutableStateOf<String?>(null) }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && firestoreIdToSubmit != null) {
            viewModel.submitAssignment(firestoreIdToSubmit!!, uri)
            Toast.makeText(context, "Assignment submitted!", Toast.LENGTH_SHORT).show()
        }
    }

    // Detail dialog
    if (showDialog && selectedUiState != null) {
        val item = selectedUiState!!
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(item.assignment.title, fontWeight = FontWeight.Bold, color = Color(0xFF009688))
            },
            text = {
                Column {
                    Text("Subject: ${item.assignment.subject}",
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Divider()
                    Spacer(Modifier.height(8.dp))
                    Text(item.assignment.description, fontSize = 16.sp, lineHeight = 24.sp)
                    Spacer(Modifier.height(16.dp))

                    if (!item.assignment.attachmentUri.isNullOrEmpty()) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data  = Uri.parse(item.assignment.attachmentUri)
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context,
                                        "Cannot open file.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0F2F1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AttachFile, null, tint = Color(0xFF009688))
                            Spacer(Modifier.width(8.dp))
                            Text("Open Attached PDF", color = Color(0xFF009688))
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Show marks if graded
                    if (item.status == "graded" && item.marks.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)),
                            shape  = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Grade, null,
                                    tint = Color(0xFF2E7D32))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Marks: ${item.marks}",
                                    fontWeight = FontWeight.Bold,
                                    color      = Color(0xFF2E7D32)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null,
                            tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Due: ${item.assignment.dueDate}",
                            color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("Close") }
            },
            icon = { Icon(Icons.Default.Assignment, null, tint = Color(0xFF009688)) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Assignments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor        = Color(0xFF009688),
                    titleContentColor     = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF009688))
                }
            }
            assignmentList.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center) {
                    Text("No assignments found.", color = Color.Gray)
                }
            }
            else -> {
                LazyColumn(
                    modifier       = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFFEDE7F6)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(assignmentList) { uiItem ->
                        StudentAssignmentCard(
                            item        = uiItem,
                            onCardClick = {
                                selectedUiState = uiItem
                                showDialog      = true
                            },
                            onSubmitClick = {
                                firestoreIdToSubmit = uiItem.firestoreId
                                fileLauncher.launch("application/pdf")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAssignmentCard(
    item: AssignmentUiState,
    onCardClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    val assignment  = item.assignment
    val isSubmitted = item.isSubmitted
    val isGraded    = item.status == "graded"

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onCardClick() },
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape     = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment, null, tint = Color(0xFF009688))
                    Spacer(Modifier.width(8.dp))
                    Text(assignment.subject, fontWeight = FontWeight.Bold,
                        fontSize = 12.sp, color = Color.Gray)
                }
                when {
                    isGraded    -> Icon(Icons.Default.Grade, null, tint = Color(0xFF2E7D32))
                    isSubmitted -> Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(assignment.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(assignment.description, color = Color.DarkGray, fontSize = 14.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)

            // Show marks badge if graded
            if (isGraded && item.marks.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape  = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Marks: ${item.marks}",
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize   = 12.sp,
                        color      = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null,
                        tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Due: ${assignment.dueDate}", fontSize = 12.sp, color = Color.Red)
                }
                when {
                    isGraded    -> Text("Graded ✓", color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    isSubmitted -> Text("Submitted ✓", color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    else        -> Button(
                        onClick        = { onSubmitClick() },
                        colors         = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF009688)),
                        modifier       = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Submit", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}