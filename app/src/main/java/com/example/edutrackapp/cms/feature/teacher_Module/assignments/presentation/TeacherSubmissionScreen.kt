package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ─── Design tokens ────────────────────────────────────────────────────────────
private val DarkNavy     = Color(0xFF1B2438)
private val BgLight      = Color(0xFFF5F6FA)
private val CardWhite    = Color(0xFFFFFFFF)
private val AccentYellow = Color(0xFFFFB800)
private val GreenPresent = Color(0xFF2ECC71)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8A9A)

// ─── Submission UI Model ──────────────────────────────────────────────────────
data class SubmissionUiModel(
    val firestoreDocId: String,
    val studentRollNo: String,
    val studentName: String,
    val fileUri: String,
    val submissionDate: String,
    val marks: String,
    val status: String
)

// ─── ViewModel ────────────────────────────────────────────────────────────────
@HiltViewModel
class TeacherSubmissionViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assignmentIdHash: Int = checkNotNull(savedStateHandle["assignmentId"])

    private val _submissions = MutableStateFlow<List<SubmissionUiModel>>(emptyList())
    val submissions: StateFlow<List<SubmissionUiModel>> = _submissions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var firestoreAssignmentId = ""

    init { findAssignmentThenListen() }

    private fun findAssignmentThenListen() {
        viewModelScope.launch {
            try {
                val snap = db.collection("assignments").get().await()
                firestoreAssignmentId = snap.documents
                    .firstOrNull { it.id.hashCode() == assignmentIdHash }?.id ?: ""
                if (firestoreAssignmentId.isNotEmpty()) {
                    listenToSubmissions()
                } else {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun listenToSubmissions() {
        db.collection("submissions")
            .whereEqualTo("assignmentId", firestoreAssignmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                _submissions.value = snapshot.documents.map { doc ->
                    SubmissionUiModel(
                        firestoreDocId = doc.id,
                        studentRollNo  = doc.getString("studentRollNo")  ?: "",
                        studentName    = doc.getString("studentName")    ?: "",
                        fileUri        = doc.getString("fileUri")        ?: "",
                        submissionDate = doc.getString("submissionDate") ?: "",
                        marks          = doc.getString("marks")          ?: "",
                        status         = doc.getString("status")         ?: "submitted"
                    )
                }
                _isLoading.value = false
            }
    }

    fun saveGrade(firestoreDocId: String, marks: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("submissions").document(firestoreDocId)
                    .update(mapOf("marks" to marks, "status" to "graded"))
                    .await()
                onDone()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherSubmissionScreen(
    navController: NavController,
    viewModel: TeacherSubmissionViewModel = hiltViewModel()
) {
    val list      = viewModel.submissions.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val context   = LocalContext.current

    var gradingTarget by remember { mutableStateOf<SubmissionUiModel?>(null) }
    var marksInput    by remember { mutableStateOf("") }

    gradingTarget?.let { sub ->
        AlertDialog(
            onDismissRequest = { gradingTarget = null },
            title = {
                Text("Grade Submission", fontWeight = FontWeight.Bold, color = TextDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Student: ${sub.studentName} (${sub.studentRollNo})",
                        fontSize = 13.sp, color = TextGray)
                    OutlinedTextField(
                        value         = marksInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) marksInput = it },
                        label         = { Text("Marks Awarded") },
                        leadingIcon   = { Icon(Icons.Default.Grade, null, tint = AccentYellow) },
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = AccentYellow,
                            unfocusedBorderColor = Color(0xFFDDDDDD),
                            focusedLabelColor    = AccentYellow,
                            cursorColor          = AccentYellow
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveGrade(sub.firestoreDocId, marksInput) {
                        Toast.makeText(context,
                            "Marks saved for ${sub.studentName}!",
                            Toast.LENGTH_SHORT).show()
                    }
                    gradingTarget = null
                    marksInput    = ""
                }) { Text("Save", color = DarkNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { gradingTarget = null }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    Scaffold(
        containerColor = BgLight,
        topBar = {
            Column {
                Box(modifier = Modifier.fillMaxWidth().background(DarkNavy)) {
                    Column(modifier = Modifier.padding(
                        start = 4.dp, end = 8.dp, top = 8.dp, bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Student Submissions", fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp, color = Color.White)
                                Text("${list.size} submitted",
                                    fontSize = 11.sp,
                                    color    = Color.White.copy(alpha = 0.55f))
                            }
                        }
                        if (list.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val gradedCount = list.count { it.status == "graded" }
                                HeaderStatChip(Modifier.weight(1f), "Submitted",
                                    list.size.toString(), GreenPresent)
                                HeaderStatChip(Modifier.weight(1f), "Graded",
                                    gradedCount.toString(), AccentYellow)
                                HeaderStatChip(Modifier.weight(1f), "Pending",
                                    (list.size - gradedCount).toString(), Color.White)
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth().background(DarkNavy)
                        .background(BgLight,
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(4.dp).height(18.dp)
                            .background(AccentYellow, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(8.dp))
                        Text("All Submissions", fontWeight = FontWeight.Bold,
                            fontSize = 15.sp, color = TextDark)
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentYellow)
                }
            }
            list.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                                .background(DarkNavy.copy(alpha = 0.07f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Inbox, null,
                                tint = DarkNavy.copy(0.3f), modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("No submissions yet", fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp, color = TextDark)
                        Spacer(Modifier.height(6.dp))
                        Text("Students haven't submitted anything yet",
                            fontSize = 13.sp, color = TextGray)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(list, key = { it.firestoreDocId }) { submission ->
                        SubmissionCard(
                            submission   = submission,
                            onOpenClick  = {
                                try {
                                    val uri    = Uri.parse(submission.fileUri)
                                    val mime   = context.contentResolver
                                        .getType(uri) ?: "*/*"
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, mime)
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                                Intent.FLAG_ACTIVITY_NO_HISTORY
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, "Open With…"))
                                } catch (e: Exception) {
                                    Toast.makeText(context,
                                        "Cannot open file on this device.",
                                        Toast.LENGTH_SHORT).show()
                                }
                            },
                            onGradeClick = {
                                gradingTarget = submission
                                marksInput    = submission.marks
                            }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ─── Submission Card ──────────────────────────────────────────────────────────
@Composable
fun SubmissionCard(
    submission: SubmissionUiModel,
    onOpenClick:  () -> Unit,
    onGradeClick: () -> Unit
) {
    val isGraded = submission.status == "graded"

    Card(
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(14.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.width(4.dp).height(100.dp)
                    .background(
                        if (isGraded) AccentYellow else GreenPresent,
                        RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )
            Spacer(Modifier.width(14.dp))

            // Avatar with first letter of name
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(DarkNavy.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = submission.studentName.firstOrNull()
                        ?.uppercaseChar()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    color      = DarkNavy
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f).padding(vertical = 14.dp)) {
                Text(submission.studentName, fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, color = TextDark)
                Spacer(Modifier.height(2.dp))
                Text("Roll: ${submission.studentRollNo}",
                    fontSize = 12.sp, color = TextGray)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape)
                        .background(GreenPresent))
                    Spacer(Modifier.width(5.dp))
                    Text("Submitted: ${submission.submissionDate}",
                        fontSize = 12.sp, color = TextGray)
                }
                if (isGraded && submission.marks.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Marks: ${submission.marks}",
                        fontSize   = 12.sp,
                        color      = AccentYellow,
                        fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier            = Modifier.padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End
            ) {
                FilledTonalButton(
                    onClick        = onOpenClick,
                    colors         = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkNavy.copy(alpha = 0.08f),
                        contentColor   = DarkNavy),
                    shape          = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Open", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick        = onGradeClick,
                    colors         = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isGraded)
                            AccentYellow.copy(alpha = 0.25f)
                        else AccentYellow.copy(alpha = 0.15f),
                        contentColor   = Color(0xFF7A5800)),
                    shape          = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Grade, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isGraded) "Re-grade" else "Grade",
                        fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}