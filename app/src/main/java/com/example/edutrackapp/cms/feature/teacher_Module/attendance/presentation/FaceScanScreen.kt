package com.example.edutrackapp.cms.feature.teacher_Module.attendance.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

// ── Design tokens ─────────────────────────────────────────────────────────────
private val DarkNavy     = Color(0xFF1B2438)
private val AccentYellow = Color(0xFFFFB800)
private val GreenPresent = Color(0xFF2ECC71)
private val TextGray     = Color(0xFF8A8A9A)
private val CardWhite    = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceScanScreen(
    navController: NavController,
    classId: String = "CS-A",
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(false) }

    // Generate a session ID once per scan session
    val sessionId = remember {
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }

    val uiState     by viewModel.uiState.collectAsState()
    val allStudents by viewModel.students.collectAsState()

    // Students recognised so far this session
    val recognisedNames = uiState.markedPresent

    // Camera permission
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) permLauncher.launch(Manifest.permission.CAMERA)
    }

    // Load students + enrolled faces when screen opens
    LaunchedEffect(classId) {
        viewModel.loadStudents(classId)
        viewModel.loadEnrolledFaces(classId)
    }

    Scaffold(
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
                                "Smart Attendance",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = Color.White
                            )
                            Text(
                                "Face recognition  •  ${recognisedNames.size} recognised",
                                fontSize = 11.sp,
                                color    = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->

        if (!hasPermission) {
            NoCameraPermissionState(padding)
            return@Scaffold
        }

        // Loading enrolled faces
        if (uiState.isLoading) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentYellow)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Loading enrolled faces…",
                        color    = TextGray,
                        fontSize = 14.sp
                    )
                }
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Camera preview ────────────────────────────────────────────
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val future      = ProcessCameraProvider.getInstance(ctx)

                    future.addListener({
                        val provider = future.get()
                        val preview  = Preview.Builder().build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)

                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(
                                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                            ).build()

                        // ✅ Real recognition: every frame goes to ViewModel
                        // which runs FaceNet + cosine similarity against enrolled faces
                        analysis.setAnalyzer(
                            Executors.newSingleThreadExecutor(),
                            FaceAnalyser(ctx) { faceBitmap ->
                                viewModel.processFrameEmbedding(
                                    classId   = classId,
                                    sessionId = sessionId,
                                    faceBitmap = faceBitmap
                                )
                            }
                        )

                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )
                        } catch (e: Exception) {
                            Log.e("FaceScan", "Camera bind failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // ── Last recognised name banner ───────────────────────────────
            uiState.lastRecognisedName?.let { name ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    shape  = RoundedCornerShape(20.dp),
                    color  = GreenPresent.copy(alpha = 0.92f)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint     = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "✅ $name recognised!",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp
                        )
                    }
                }
            }

            // ── Bottom gradient scrim ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, DarkNavy.copy(alpha = 0.97f))
                        )
                    )
            )

            // ── Bottom panel ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Recognised students list (scrollable, max 3 visible)
                if (recognisedNames.isNotEmpty()) {
                    Text(
                        "Marked Present (${recognisedNames.size})",
                        color      = Color.White.copy(0.75f),
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier  = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 130.dp),
                        shape     = RoundedCornerShape(14.dp),
                        colors    = CardDefaults.cardColors(
                            containerColor = CardWhite.copy(alpha = 0.10f)
                        )
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(recognisedNames.toList()) { name ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier          = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier         = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(GreenPresent.copy(0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            name.first().uppercaseChar().toString(),
                                            color      = GreenPresent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize   = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        name,
                                        color      = Color.White,
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint     = GreenPresent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    // Hint when no one recognised yet
                    Row(
                        modifier = Modifier
                            .background(
                                Color.White.copy(0.10f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FaceRetouchingNatural,
                            null,
                            tint     = AccentYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Point camera at students to recognise them",
                            color    = Color.White,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Confirm & go back button
                Button(
                    onClick = {
                        // Pass recognised count back to MarkAttendanceScreen
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("face_count", recognisedNames.size)

                        viewModel.finaliseSession(classId, sessionId)

                        Toast.makeText(
                            context,
                            "Marked ${recognisedNames.size} student(s) present via face scan",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                    },
                    enabled  = recognisedNames.isNotEmpty(),
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
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (recognisedNames.isEmpty()) "WAITING FOR FACES…"
                        else "CONFIRM  ${recognisedNames.size} PRESENT",
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NoCameraPermissionState(padding: PaddingValues) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color(0xFFF5F6FA)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DarkNavy.copy(0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    null,
                    modifier = Modifier.size(36.dp),
                    tint     = DarkNavy.copy(0.4f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Camera Permission Required",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 16.sp,
                color      = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Please allow camera access to use\nSmart Attendance",
                fontSize  = 13.sp,
                color     = TextGray,
                textAlign = TextAlign.Center
            )
        }
    }
}