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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.edutrackapp.cms.core.util.FaceAnalyzer
import java.util.concurrent.Executors

// ─── Design tokens (matches Teacher Dashboard) ────────────────────────────────
private val DarkNavy     = Color(0xFF1B2438)
private val AccentYellow = Color(0xFFFFB800)
private val GreenPresent = Color(0xFF2ECC71)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceScanScreen(navController: NavController) {

    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var faceCount     by remember { mutableIntStateOf(0) }
    var hasPermission by remember { mutableStateOf(false) }

    // ── Camera permission ─────────────────────────────────────────────────
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted }
    )

    LaunchedEffect(Unit) {
        val check = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (check == PackageManager.PERMISSION_GRANTED) hasPermission = true
        else launcher.launch(Manifest.permission.CAMERA)
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
                                "Point camera at the class",
                                fontSize = 11.sp,
                                color    = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { paddingValues ->

        if (hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ── Camera preview ────────────────────────────────────────
                AndroidView(
                    factory = { ctx ->
                        val previewView        = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build()
                            preview.setSurfaceProvider(previewView.surfaceProvider)

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(
                                Executors.newSingleThreadExecutor(),
                                FaceAnalyzer { count -> faceCount = count }
                            )

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("Camera", "Binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // ── Gradient scrim at bottom ──────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, DarkNavy.copy(alpha = 0.92f))
                            )
                        )
                )

                // ── Overlay card ──────────────────────────────────────────
                Column(
                    modifier            = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Face count badge
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier              = Modifier
                            .background(
                                Color.White.copy(alpha = 0.10f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(36.dp)
                                .background(AccentYellow.copy(0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FaceRetouchingNatural,
                                contentDescription = null,
                                tint     = AccentYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text       = "Detected: $faceCount  ${if (faceCount == 1) "student" else "students"}",
                            color      = Color.White,
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Confirm button
                    Button(
                        onClick = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("face_count", faceCount)
                            Toast.makeText(
                                context,
                                "Marking $faceCount students present…",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        },
                        enabled  = faceCount > 0,
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = AccentYellow,
                            contentColor   = DarkNavy,
                            disabledContainerColor = Color.White.copy(0.15f),
                            disabledContentColor   = Color.White.copy(0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "CONFIRM ATTENDANCE",
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

        } else {
            // ── No permission state ───────────────────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F6FA)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier         = Modifier
                            .size(80.dp)
                            .background(DarkNavy.copy(0.08f), CircleShape),
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
                        color     = Color(0xFF8A8A9A),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}