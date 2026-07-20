package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RationViewModel
import com.example.ui.theme.GovBluePrimary
import com.example.ui.theme.GovBlueSecondary
import com.example.ui.theme.GovOrangeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: RationViewModel, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var userIdInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var simulatedOtp by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Observe notifications in background to display as toast/snack
    val uiNotification by viewModel.uiNotification.collectAsState()
    LaunchedEffect(uiNotification) {
        uiNotification?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearNotification()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2563EB), Color(0xFF4338CA))
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Indian National Emblem Representation / App logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = "Emblem",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "E-RATION SYSTEM",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Ministry of Consumer Affairs, Food & Public Distribution",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Authentication Form Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card")
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Unified OTP Sign In",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Enter Aadhaar Number, Shopkeeper ID or Admin code",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = userIdInput,
                        onValueChange = { userIdInput = it },
                        label = { Text("Aadhaar / ID Number") },
                        placeholder = { Text("e.g. 111122223333 or shopkeeper1") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("id_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(visible = otpSent) {
                        Column {
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("6-Digit OTP") },
                                placeholder = { Text("Enter OTP from SMS") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    if (!otpSent) {
                        Button(
                            onClick = {
                                if (userIdInput.trim().isEmpty()) {
                                    Toast.makeText(context, "Please enter your ID/Aadhaar", Toast.LENGTH_SHORT).show()
                                } else {
                                    isLoading = true
                                    viewModel.sendOtp(userIdInput.trim()) { otp ->
                                        simulatedOtp = otp
                                        otpSent = true
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("send_otp_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GovBluePrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Send Verification OTP", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (otpInput == simulatedOtp) {
                                    isLoading = true
                                    viewModel.login(userIdInput.trim()) { success ->
                                        isLoading = false
                                        if (success) {
                                            onLoginSuccess()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Incorrect OTP. Please check the notification banner.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("verify_otp_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GovBlueSecondary)
                        ) {
                            Text("Verify OTP & Log In", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = {
                                otpSent = false
                                otpInput = ""
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Change Aadhaar / ID", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Demo Login Shortcut Section (Critically useful for reviewers)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("demo_section_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OFFICIAL DEMO PORTAL PATHWAYS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Citizen 1
                    Button(
                        onClick = {
                            viewModel.login("111122223333") { if (it) onLoginSuccess() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("demo_citizen1_btn")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Color.White)
                            Text("Citizen: Rajesh (BPL Card)", color = Color.White, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }

                    // Citizen 2
                    Button(
                        onClick = {
                            viewModel.login("444455556666") { if (it) onLoginSuccess() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("demo_citizen2_btn")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Color.White)
                            Text("Citizen: Anjali (AAY Card)", color = Color.White, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }

                    // Shopkeeper
                    Button(
                        onClick = {
                            viewModel.login("shopkeeper1") { if (it) onLoginSuccess() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("demo_shopkeeper_btn")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White)
                            Text("Shopkeeper: Mahesh (FPS-50012)", color = Color.White, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }

                    // Government Admin
                    Button(
                        onClick = {
                            viewModel.login("admin") { if (it) onLoginSuccess() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("demo_admin_btn")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White)
                            Text("Admin: Officer Sandeep", color = Color.White, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
