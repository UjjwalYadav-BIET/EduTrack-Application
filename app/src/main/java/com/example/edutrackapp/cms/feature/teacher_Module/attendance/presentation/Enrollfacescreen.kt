package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.edutrackapp.cms.feature.teacher_Module.attendance.ml.FaceAnalyser
import java.util.concurrent.Executors

private val DarkNavy     = Color(0xFF1B2438)
private val AccentYellow = Color(0xFFFFB800)
private val GreenPresent = Color(0xFF2ECC71)
private val BgLight      = Color(0xFFF5F6FA)
private val TextGray     = Color(0xFF8A8A9A)

private enum class EnrollStep { SELECT_STUDENT, CAPTURE_FACE, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollFaceScreen(
    navController: NavController,
    classId: String,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var step             by remember { mutableStateOf(EnrollStep.SELECT_STUDENT) }
    var hasPermission    by remember { mutableStateOf(false) }
    var capturedFace     by remember { mutableStateOf<Bitmap?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val uiState     by viewModel.uiState.collectAsState()
    val allStudents by viewModel.students.collectAsState()

    var selectedStudent by remember { mutableStateOf<StudentUiModel?>(null) }

    LaunchedEffect(classId) {
        viewModel.loadStudents(classId)
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) permLauncher.launch(Manifest.permission.CAMERA)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
            if (!uiState.isError && step == EnrollStep.CAPTURE_FACE) {
                step = EnrollStep.DONE
            }
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = BgLight,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy)
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Enroll Student Face",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = Color.White
                            )
                            Text(
                                when (step) {
                                    EnrollStep.SELECT_STUDENT -> "Step 1 of 2 — Select student"
                                    EnrollStep.CAPTURE_FACE  -> "Step 2 of 2 — Capture face"
                                    EnrollStep.DONE          -> "Enrollment complete"
                                },
                                fontSize = 11.sp,
                                color    = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (step == EnrollStep.CAPTURE_FACE) step = EnrollStep.SELECT_STUDENT
                            else navController.popBackStack()
                        }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->

        when (step) {

            // ── Step 1: Select student ────────────────────────────────────
            EnrollStep.SELECT_STUDENT -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    StepProgressBar(currentStep = 1, totalSteps = 2)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Select the student to enroll",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = DarkNavy
                    )
                    Text(
                        "The student will stand in front of the camera in the next step. " +
                                "Their face will be saved and used for automatic attendance.",
                        fontSize   = 13.sp,
                        color      = TextGray,
                        lineHeight = 20.sp
                    )

                    ExposedDropdownMenuBox(
                        expanded         = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStudent?.let {
                                "${it.name}  ·  ${it.rollNo}"
                            } ?: "Tap to select student…",
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Student") },
                            trailingIcon  = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, null, tint = DarkNavy)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape  = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = AccentYellow,
                                unfocusedBorderColor = Color(0xFFDDE2EC)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded         = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            if (allStudents.isEmpty()) {
                                DropdownMenuItem(
                                    text    = { Text("No students found", color = TextGray) },
                                    onClick = { dropdownExpanded = false }
                                )
                            } else {
                                allStudents.forEach { student ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    student.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize   = 14.sp
                                                )
                                                Text(
                                                    student.rollNo,
                                                    fontSize = 12.sp,
                                                    color    = TextGray
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedStudent  = student
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = selectedStudent != null) {
                        selectedStudent?.let { student ->
                            Card(
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(16.dp),
                                colors    = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(3.dp)
                            ) {
                                Row(
                                    modifier          = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier         = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(AccentYellow.copy(0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            student.name.first().uppercaseChar().toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize   = 22.sp,
                                            color      = AccentYellow
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            student.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize   = 15.sp,
                                            color      = DarkNavy
                                        )
                                        Text(
                                            "Roll: ${student.rollNo}",
                                            fontSize = 12.sp,
                                            color    = TextGray
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        null,
                                        tint     = GreenPresent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick  = { step = EnrollStep.CAPTURE_FACE },
                        enabled  = selectedStudent != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NEXT — CAPTURE FACE", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }

            // ── Step 2: Capture face ──────────────────────────────────────
            EnrollStep.CAPTURE_FACE -> {
                val student = selectedStudent ?: return@Scaffold

                if (!hasPermission) {
                    NoCameraPermission(padding)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val future      = ProcessCameraProvider.getInstance(ctx)

                                future.addListener({
                                    val provider = future.get()
                                    val preview  = Preview.Builder().build()
                                    preview.setSurfaceProvider(previewView.surfaceProvider)

                                    val analysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    analysis.setAnalyzer(
                                        Executors.newSingleThreadExecutor(),
                                        FaceAnalyser(ctx) { faceBitmap ->
                                            capturedFace = faceBitmap
                                        }
                                    )

                                    try {
                                        provider.unbindAll()
                                        provider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_FRONT_CAMERA,
                                            preview,
                                            analysis
                                        )
                                    } catch (e: Exception) {
                                        Log.e("EnrollFace", "Camera bind failed", e)
                                    }
                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        FaceGuideOverlay()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, DarkNavy.copy(0.95f))
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(Color.White.copy(0.12f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person, null,
                                    tint     = AccentYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Enrolling: ${student.name}",
                                    color      = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text  = if (capturedFace != null) "✅ Face detected — tap Enroll to save"
                                else "👤 Position face inside the oval",
                                color = if (capturedFace != null) GreenPresent else Color.White.copy(0.7f),
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    capturedFace?.let { face ->
                                        viewModel.enrollStudent(
                                            classId     = classId,
                                            studentName = student.name,
                                            faceBitmap  = face
                                        )
                                    }
                                },
                                enabled  = capturedFace != null && !uiState.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape  = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor         = AccentYellow,
                                    contentColor           = DarkNavy,
                                    disabledContainerColor = Color.White.copy(0.15f),
                                    disabledContentColor   = Color.White.copy(0.4f)
                                )
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        color       = DarkNavy,
                                        modifier    = Modifier.size(20.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    // ✅ FIX: use Icons.Default.Fingerprint directly — no extension function
                                    Icon(
                                        Icons.Default.Fingerprint,
                                        null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "ENROLL FACE",
                                        fontWeight    = FontWeight.Bold,
                                        fontSize      = 15.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Done ──────────────────────────────────────────────────────
            EnrollStep.DONE -> {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(GreenPresent.copy(0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle, null,
                                tint     = GreenPresent,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Face Enrolled!", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = DarkNavy)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${selectedStudent?.name}'s face has been saved.\n" +
                                    "They will be recognized automatically during attendance.",
                            fontSize   = 14.sp,
                            color      = TextGray,
                            textAlign  = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                selectedStudent = null
                                capturedFace    = null
                                step            = EnrollStep.SELECT_STUDENT
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                        ) {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ENROLL ANOTHER STUDENT", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick  = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy)
                        ) {
                            Text("DONE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun StepProgressBar(currentStep: Int, totalSteps: Int) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index < currentStep) AccentYellow else Color(0xFFDDE2EC))
            )
        }
    }
}

@Composable
private fun FaceGuideOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.Center)
                .border(3.dp, AccentYellow.copy(0.8f), CircleShape)
        )
        Text(
            "Align face here",
            color    = Color.White.copy(0.7f),
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 125.dp)
        )
    }
}

@Composable
private fun NoCameraPermission(padding: PaddingValues) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(BgLight),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(48.dp), tint = TextGray)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Camera permission required", fontWeight = FontWeight.SemiBold, color = DarkNavy)
        }
    }
}