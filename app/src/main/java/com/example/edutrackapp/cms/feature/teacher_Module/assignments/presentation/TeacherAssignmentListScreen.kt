package com.example.edutrackapp.cms.feature.teacher_Module.assignments.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.edutrackapp.cms.core.data.local.entity.AssignmentEntity
import com.example.edutrackapp.cms.ui.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ─── Data class to hold Firestore ID alongside entity ────────────────────────
data class AssignmentWithId(
    val entity: AssignmentEntity,
    val firestoreId: String
)

// ─── ViewModel ────────────────────────────────────────────────────────────────
@HiltViewModel
class TeacherAssignmentListViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _assignments = MutableStateFlow<List<AssignmentWithId>>(emptyList())
    val assignments: StateFlow<List<AssignmentWithId>> = _assignments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { listenToAssignments() }

    private fun listenToAssignments() {
        db.collection("assignments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                viewModelScope.launch {
                    _assignments.value = snapshot.documents.mapNotNull { doc ->
                        val entity = AssignmentEntity(
                            id            = doc.id.hashCode(),
                            title         = doc.getString("title")       ?: return@mapNotNull null,
                            subject       = doc.getString("subject")     ?: "",
                            description   = doc.getString("description") ?: "",
                            dueDate       = doc.getString("dueDate")     ?: "",
                            batch         = doc.getString("batch")       ?: "",
                            attachmentUri = doc.getString("attachmentUri")
                                ?.takeIf { it.isNotEmpty() }
                        )
                        AssignmentWithId(entity, doc.id)
                    }
                    _isLoading.value = false
                }
            }
    }
}

// ─── Design tokens ────────────────────────────────────────────────────────────
private val DarkNavy     = Color(0xFF1B2438)
private val BgLight      = Color(0xFFF5F6FA)
private val CardWhite    = Color(0xFFFFFFFF)
private val AccentYellow = Color(0xFFFFB800)
private val GreenPresent = Color(0xFF2ECC71)
private val RedAbsent    = Color(0xFFE74C3C)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8A9A)

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAssignmentListScreen(
    navController: NavController,
    viewModel: TeacherAssignmentListViewModel = hiltViewModel()
) {
    val allAssignments = viewModel.assignments.collectAsState().value
    val isLoading      = viewModel.isLoading.collectAsState().value

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Active", "Overdue")

    val today = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    fun isOverdue(dueDate: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.parse(dueDate)?.before(sdf.parse(today)) == true
        } catch (e: Exception) { false }
    }

    val displayList = allAssignments.filter { a ->
        val matchesSearch = searchQuery.isBlank() ||
                a.entity.title.contains(searchQuery, ignoreCase = true) ||
                a.entity.subject.contains(searchQuery, ignoreCase = true)
        val matchesTab = when (selectedTab) {
            1    -> !isOverdue(a.entity.dueDate)
            2    -> isOverdue(a.entity.dueDate)
            else -> true
        }
        matchesSearch && matchesTab
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
                                Text("Assignments", fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp, color = Color.White)
                                Text("${allAssignments.size} total",
                                    fontSize = 11.sp,
                                    color    = Color.White.copy(alpha = 0.55f))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val overdueCount = allAssignments.count {
                                isOverdue(it.entity.dueDate)
                            }
                            HeaderStatChip(Modifier.weight(1f), "Total",
                                allAssignments.size.toString(), Color.White)
                            HeaderStatChip(Modifier.weight(1f), "Active",
                                (allAssignments.size - overdueCount).toString(), GreenPresent)
                            HeaderStatChip(Modifier.weight(1f), "Overdue",
                                overdueCount.toString(), RedAbsent)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkNavy)
                        .background(BgLight,
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value         = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder   = { Text("Search title or subject…",
                                fontSize = 14.sp, color = TextGray) },
                            leadingIcon   = { Icon(Icons.Default.Search, null,
                                tint = TextGray, modifier = Modifier.size(20.dp)) },
                            trailingIcon  = {
                                if (searchQuery.isNotEmpty())
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, null,
                                            tint     = TextGray,
                                            modifier = Modifier.size(18.dp))
                                    }
                            },
                            modifier   = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape      = RoundedCornerShape(14.dp),
                            colors     = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = AccentYellow,
                                unfocusedBorderColor    = Color.Transparent,
                                focusedContainerColor   = CardWhite,
                                unfocusedContainerColor = CardWhite
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            tabs.forEachIndexed { index, label ->
                                val selected = selectedTab == index
                                Surface(
                                    onClick         = { selectedTab = index },
                                    shape           = RoundedCornerShape(20.dp),
                                    color           = if (selected) DarkNavy else CardWhite,
                                    shadowElevation = if (selected) 0.dp else 1.dp
                                ) {
                                    Text(label,
                                        modifier   = Modifier.padding(
                                            horizontal = 16.dp, vertical = 8.dp),
                                        fontSize   = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold
                                        else FontWeight.Normal,
                                        color      = if (selected) AccentYellow else TextGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { navController.navigate(Screen.CreateAssignment.route) },
                containerColor = DarkNavy,
                contentColor   = AccentYellow,
                shape          = RoundedCornerShape(16.dp),
                modifier       = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(26.dp))
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
            displayList.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                                .background(DarkNavy.copy(alpha = 0.07f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assignment, null,
                                tint     = DarkNavy.copy(0.3f),
                                modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (searchQuery.isBlank()) "No assignments yet"
                            else "No results for \"$searchQuery\"",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 16.sp,
                            color      = TextDark
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Tap + to create an assignment",
                            color = TextGray, fontSize = 13.sp)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier          = Modifier.fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(4.dp).height(18.dp)
                                .background(AccentYellow, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(8.dp))
                            Text("${tabs[selectedTab]} (${displayList.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp,
                                color      = TextDark)
                        }
                    }
                    items(displayList, key = { it.firestoreId }) { item ->
                        AssignmentCard(
                            assignment = item.entity,
                            isOverdue  = isOverdue(item.entity.dueDate),
                            onClick    = {
                                navController.navigate(
                                    Screen.ViewSubmissions.createRoute(item.entity.id)
                                )
                            }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ─── Assignment Card ──────────────────────────────────────────────────────────
@Composable
fun AssignmentCard(
    assignment: AssignmentEntity,
    isOverdue: Boolean,
    onClick: () -> Unit
) {
    val statusColor = if (isOverdue) RedAbsent else GreenPresent

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.width(4.dp).height(96.dp)
                    .background(statusColor,
                        RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f).padding(vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(assignment.title, fontWeight = FontWeight.Bold,
                        fontSize = 14.sp, color = TextDark, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isOverdue) RedAbsent.copy(0.12f)
                                else GreenPresent.copy(0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (isOverdue) "Overdue" else "Active",
                            fontSize   = 10.sp,
                            color      = if (isOverdue) RedAbsent else GreenPresent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniChip(assignment.subject, AccentYellow)
                    MiniChip(assignment.batch, DarkNavy)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null,
                            tint     = if (isOverdue) RedAbsent else AccentYellow,
                            modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Due: ${assignment.dueDate}", fontSize = 11.sp,
                            color = if (isOverdue) RedAbsent else TextGray)
                    }
                    if (!assignment.attachmentUri.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachFile, null,
                                tint = TextGray, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PDF", fontSize = 11.sp, color = TextGray)
                        }
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null,
                tint     = TextGray,
                modifier = Modifier.padding(end = 12.dp).size(20.dp))
        }
    }
}

// ─── Header Stat Chip ─────────────────────────────────────────────────────────
@Composable
fun HeaderStatChip(modifier: Modifier, label: String, value: String, color: Color) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.10f)) {
        Column(modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(label, fontSize = 11.sp, color = Color.White.copy(0.6f))
        }
    }
}

// ─── Mini Chip ────────────────────────────────────────────────────────────────
@Composable
fun MiniChip(text: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}