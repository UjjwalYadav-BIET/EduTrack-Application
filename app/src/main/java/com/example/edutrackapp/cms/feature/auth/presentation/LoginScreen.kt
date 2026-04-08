package com.example.edutrackapp.cms.feature.auth.presentation

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

// ─── Color Palette ────────────────────────────────────────────────────────────
private val PrimaryPurple    = Color(0xFF6200EE)
private val DeepPurple       = Color(0xFF3700B3)
private val SoftPurple       = Color(0xFFEDE7F6)
private val AccentViolet     = Color(0xFF7C3AED)
private val CardWhite        = Color(0xFFFFFFFF)
private val TextPrimary      = Color(0xFF1A1A2E)
private val TextSecondary    = Color(0xFF6B7280)
private val InputBorder      = Color(0xFFE5E7EB)

// ─── Replace with your actual Web Client ID from Firebase Console ─────────────
// Firebase Console → Project Settings → General → Web API Key / OAuth Client ID
private const val WEB_CLIENT_ID = "277411008137-30eai89ur3o85icjs366sn5n7ra9avjk.apps.googleusercontent.com"

// ─── Tab options ──────────────────────────────────────────────────────────────
private enum class LoginTab { EMAIL, PHONE }

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: (String) -> Unit
) {
    // ── State ─────────────────────────────────────────────────────────────────
    var selectedTab by remember { mutableStateOf(LoginTab.EMAIL) }

    // Email tab
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Phone tab
    var phone by remember { mutableStateOf("+91") }

    val state = viewModel.loginState.value
    val context = LocalContext.current
    val activity = context as Activity
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Floating blob animation
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // ── Handle auth state changes ─────────────────────────────────────────────
    LaunchedEffect(state) {
        Log.d("LOGIN_DEBUG", "Auth State: $state")
        when (state) {
            is AuthState.Success -> {
                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                onLoginSuccess(state.user.role)
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            is AuthState.OtpSent -> {
                // OTP sent — navigate to OTP screen
                onLoginSuccess("otp:$phone")
            }
            else -> {}
        }
    }

    // ── Google Sign-In helper ─────────────────────────────────────────────────
    fun launchGoogleSignIn() {
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                // Try with authorized accounts first
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .setNonce(null)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                when (val credential = result.credential) {
                    is GoogleIdTokenCredential -> {
                        viewModel.loginWithGoogle(credential.idToken)
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            "Unexpected credential: ${credential.type}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: GetCredentialException) {
                Log.e("GOOGLE_SIGNIN", "GetCredentialException: ${e.type} - ${e.message}")
                Toast.makeText(
                    context,
                    "Sign-in failed: ${e.type}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("GOOGLE_SIGNIN", "Exception: ${e.message}")
                Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF3F0FF), Color(0xFFF8F7FF), Color(0xFFFFFFFF))
                )
            )
    ) {
        // Decorative blobs
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-60).dp, y = (-40).dp + floatOffset.dp)
                .clip(CircleShape)
                .blur(80.dp)
                .background(PrimaryPurple.copy(alpha = 0.25f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 40.dp - floatOffset.dp)
                .clip(CircleShape)
                .blur(70.dp)
                .background(AccentViolet.copy(alpha = 0.18f))
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 30.dp)
                .clip(CircleShape)
                .blur(60.dp)
                .background(DeepPurple.copy(alpha = 0.12f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = CircleShape,
                                ambientColor = PrimaryPurple.copy(alpha = 0.4f),
                                spotColor = PrimaryPurple.copy(alpha = 0.4f)
                            )
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryPurple, DeepPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "E",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "EduTrack",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "College Management System",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Login Card ────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 200)) +
                        slideInVertically(tween(700, delayMillis = 200)) { 60 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = PrimaryPurple.copy(alpha = 0.15f),
                            spotColor = PrimaryPurple.copy(alpha = 0.15f)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Accent line
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(PrimaryPurple, AccentViolet)
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Welcome back",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sign in to continue",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Tab Row: Email | Phone ─────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F0FF)),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            LoginTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected)
                                                Brush.horizontalGradient(listOf(PrimaryPurple, AccentViolet))
                                            else
                                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                        )
                                        .clickable { selectedTab = tab }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (tab == LoginTab.EMAIL)
                                                Icons.Default.Email else Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (tab == LoginTab.EMAIL) "Email" else "Phone",
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Animated tab content ───────────────────────────────
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                            },
                            label = "tab_content"
                        ) { tab ->
                            when (tab) {

                                // ── Email & Password ───────────────────────────
                                LoginTab.EMAIL -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {

                                        // Email field
                                        Text(
                                            text = "Email Address",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = email,
                                            onValueChange = { email = it },
                                            placeholder = { Text("you@example.com", color = Color(0xFFBDBDBD)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Email,
                                                    contentDescription = null,
                                                    tint = PrimaryPurple,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Email,
                                                imeAction = ImeAction.Next
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = PrimaryPurple,
                                                unfocusedBorderColor = InputBorder,
                                                focusedContainerColor = SoftPurple.copy(alpha = 0.3f),
                                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                                cursorColor = PrimaryPurple
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Password field
                                        Text(
                                            text = "Password",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it },
                                            placeholder = { Text("Enter your password", color = Color(0xFFBDBDBD)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = PrimaryPurple,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                    Icon(
                                                        imageVector = if (passwordVisible)
                                                            Icons.Default.Visibility
                                                        else Icons.Default.VisibilityOff,
                                                        contentDescription = null,
                                                        tint = PrimaryPurple,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            },
                                            visualTransformation = if (passwordVisible)
                                                VisualTransformation.None
                                            else PasswordVisualTransformation(),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Password,
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(onDone = {
                                                focusManager.clearFocus()
                                                viewModel.login(email, password)
                                            }),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = PrimaryPurple,
                                                unfocusedBorderColor = InputBorder,
                                                focusedContainerColor = SoftPurple.copy(alpha = 0.3f),
                                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                                cursorColor = PrimaryPurple
                                            )
                                        )

                                        // Forgot password
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(
                                                onClick = { },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = "Forgot password?",
                                                    fontSize = 12.sp,
                                                    color = PrimaryPurple,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Sign In button
                                        GradientButton(
                                            text = "SIGN IN",
                                            isLoading = state is AuthState.Loading,
                                            onClick = {
                                                focusManager.clearFocus()
                                                viewModel.login(email, password)
                                            }
                                        )
                                    }
                                }

                                // ── Phone OTP ──────────────────────────────────
                                LoginTab.PHONE -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {

                                        Text(
                                            text = "Phone Number",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = phone,
                                            onValueChange = { phone = it },
                                            placeholder = { Text("+919876543210", color = Color(0xFFBDBDBD)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Phone,
                                                    contentDescription = null,
                                                    tint = PrimaryPurple,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Phone,
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(onDone = {
                                                focusManager.clearFocus()
                                                viewModel.sendOtp(phone, activity)
                                            }),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = PrimaryPurple,
                                                unfocusedBorderColor = InputBorder,
                                                focusedContainerColor = SoftPurple.copy(alpha = 0.3f),
                                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                                cursorColor = PrimaryPurple
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Include country code e.g. +919876543210",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Send OTP button
                                        GradientButton(
                                            text = "SEND OTP",
                                            isLoading = state is AuthState.Loading,
                                            onClick = {
                                                focusManager.clearFocus()
                                                viewModel.sendOtp(phone, activity)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Divider ────────────────────────────────────────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = InputBorder)
                            Text(
                                text = "  or  ",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = InputBorder)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Google Sign-In Button ──────────────────────────────
                        OutlinedButton(
                            onClick = { launchGoogleSignIn() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, InputBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White
                            ),
                            enabled = state !is AuthState.Loading
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Google "G" logo
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                listOf(Color(0xFF4285F4), Color(0xFF34A853))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Footer ────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 400))
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account?  ",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Contact Admin",
                        fontSize = 13.sp,
                        color = PrimaryPurple,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─── Reusable gradient button ─────────────────────────────────────────────────
@Composable
private fun GradientButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = PrimaryPurple.copy(alpha = 0.5f),
                spotColor = PrimaryPurple.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        enabled = !isLoading
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (!isLoading)
                            listOf(PrimaryPurple, AccentViolet, DeepPurple)
                        else
                            listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E))
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}