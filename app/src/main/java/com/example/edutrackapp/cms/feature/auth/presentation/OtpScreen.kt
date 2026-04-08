package com.example.edutrackapp.cms.feature.auth.presentation

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val PrimaryPurple = Color(0xFF6200EE)
private val DeepPurple    = Color(0xFF3700B3)
private val AccentViolet  = Color(0xFF7C3AED)
private val SoftPurple    = Color(0xFFEDE7F6)
private val TextPrimary   = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6B7280)

@Composable
fun OtpScreen(
    phoneNumber: String,
    viewModel: AuthViewModel = hiltViewModel(),
    onVerified: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val state         = viewModel.loginState.value
    val resendAllowed = viewModel.resendAllowed.value
    val secondsLeft   = viewModel.secondsLeft.value   // ← driven by ViewModel, always in sync

    // 6 individual digit boxes
    val digits          = remember { Array(6) { mutableStateOf("") } }
    val focusRequesters = remember { Array(6) { FocusRequester() } }

    // Navigate away on success
    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onVerified(state.user.role)
        }
    }

    // Auto-focus first box on entry.
    // DO NOT call sendOtp here — LoginScreen already sent it before navigating.
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF3F0FF), Color(0xFFF8F7FF), Color(0xFFFFFFFF))
                )
            )
    ) {
        // Back button
        IconButton(
            onClick = {
                viewModel.resetState()
                onBack()
            },
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .shadow(
                                16.dp, CircleShape,
                                ambientColor = PrimaryPurple.copy(alpha = 0.3f),
                                spotColor = PrimaryPurple.copy(alpha = 0.3f)
                            )
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(PrimaryPurple, AccentViolet)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📱", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Verify your phone",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We sent a 6-digit code to",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = phoneNumber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── OTP Card ─────────────────────────────────────────────────────
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
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

                        Spacer(modifier = Modifier.height(24.dp))

                        // ── 6 Digit Boxes ────────────────────────────────────
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            digits.forEachIndexed { index, digit ->
                                OutlinedTextField(
                                    value = digit.value,
                                    onValueChange = { newVal ->
                                        val filtered = newVal
                                            .filter { it.isDigit() }
                                            .takeLast(1)   // takeLast so pasting replaces correctly

                                        digit.value = filtered

                                        when {
                                            // Digit entered → move focus forward
                                            filtered.isNotEmpty() && index < 5 -> {
                                                focusRequesters[index + 1].requestFocus()
                                            }
                                            // All boxes filled → auto-submit
                                            index == 5 && filtered.isNotEmpty() -> {
                                                val otp = digits.joinToString("") { it.value }
                                                if (otp.length == 6) viewModel.verifyOtp(otp)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .focusRequester(focusRequesters[index])
                                        // Handle backspace: clear current box and move back
                                        .onKeyEvent { keyEvent ->
                                            if (keyEvent.key == Key.Backspace) {
                                                if (digit.value.isEmpty() && index > 0) {
                                                    digits[index - 1].value = ""
                                                    focusRequesters[index - 1].requestFocus()
                                                } else {
                                                    digit.value = ""
                                                }
                                                true
                                            } else false
                                        },
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryPurple,
                                        unfocusedBorderColor = Color(0xFFE5E7EB),
                                        focusedContainerColor = SoftPurple.copy(alpha = 0.3f),
                                        unfocusedContainerColor = Color(0xFFFAFAFA),
                                        cursorColor = PrimaryPurple
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Verify Button ─────────────────────────────────────
                        Button(
                            onClick = {
                                val otp = digits.joinToString("") { it.value }
                                viewModel.verifyOtp(otp)
                            },
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
                            enabled = state !is AuthState.Loading
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = if (state !is AuthState.Loading)
                                                listOf(PrimaryPurple, AccentViolet, DeepPurple)
                                            else
                                                listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E))
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state is AuthState.Loading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Text(
                                        text = "VERIFY OTP",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Resend Row ────────────────────────────────────────
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Didn't receive it? ",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            if (resendAllowed) {
                                TextButton(
                                    onClick = {
                                        digits.forEach { it.value = "" }
                                        focusRequesters[0].requestFocus()
                                        // sendOtp resets the countdown internally via startCountdown()
                                        viewModel.sendOtp(phoneNumber, activity)
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "Resend OTP",
                                        fontSize = 13.sp,
                                        color = PrimaryPurple,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    // secondsLeft comes from ViewModel — always in sync with resendAllowed
                                    text = "Resend in ${secondsLeft}s",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Error message
                        if (state is AuthState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                color = Color(0xFFE53935),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}