package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.RationViewModel
import com.example.ui.theme.GovBluePrimary
import com.example.ui.theme.GovBlueSecondary
import com.example.ui.theme.GovOrangeAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopkeeperDashboard(viewModel: RationViewModel, onLogout: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentUser by viewModel.currentUser.collectAsState()
    val shops by viewModel.allShops.collectAsState()
    val bookings by viewModel.allBookings.collectAsState()

    // Determine current shop belonging to the logged-in shopkeeper
    val currentShop = remember(shops, currentUser) {
        shops.find { it.id == currentUser?.shopId }
    }

    // Filter bookings belonging to current shopkeeper's shop
    val shopBookings = remember(bookings, currentShop) {
        bookings.filter { it.shopId == currentShop?.id }
    }

    val uiNotification by viewModel.uiNotification.collectAsState()
    LaunchedEffect(uiNotification) {
        uiNotification?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearNotification()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("shop_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    modifier = Modifier.testTag("shop_tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Verify QR") },
                    label = { Text("Verify QR") },
                    modifier = Modifier.testTag("shop_tab_verify")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    modifier = Modifier.testTag("shop_tab_inventory")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Shopkeeper Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color(0xFF2563EB), Color(0xFF4338CA))
                        )
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "FPS LICENSED SHOPKEEPER",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = currentUser?.name ?: "Shopkeeper",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Shop: ${currentShop?.name ?: "Unknown FPS"} (${currentShop?.id})",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.9f))
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier
                        .testTag("shop_logout_button")
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = Color.White)
                }
            }

            // Low Stock Warning Banner
            currentShop?.let { shop ->
                val lowStockItems = mutableListOf<String>()
                if (shop.riceStock < 300.0) lowStockItems.add("Rice")
                if (shop.wheatStock < 200.0) lowStockItems.add("Wheat")
                if (shop.sugarStock < 50.0) lowStockItems.add("Sugar")
                if (shop.oilStock < 50.0) lowStockItems.add("Oil")

                if (lowStockItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0))
                            .border(1.dp, Color(0xFFFFB74D))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "CRITICAL LOW STOCK: ${lowStockItems.joinToString(", ")}. File replenishment request.",
                                color = Color(0xFFE65100),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Tabs Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> ShopkeeperDashboardTab(currentShop, shopBookings)
                    1 -> ShopkeeperVerifyTab(currentShop, shopBookings, viewModel)
                    2 -> ShopkeeperInventoryTab(currentShop, viewModel)
                }
            }
        }
    }
}

// ------------------- Shopkeeper Dashboard Tab -------------------
@Composable
fun ShopkeeperDashboardTab(shop: ShopEntity?, bookings: List<SlotBookingEntity>) {
    val scrollState = rememberScrollState()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayBookings = remember(bookings, todayStr) { bookings.filter { it.date == todayStr } }
    val pendingCount = todayBookings.count { it.status == "Pending" }
    val completedCount = todayBookings.count { it.status == "Completed" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader("Today's Distribution Summary (${todayStr})")

        // Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = GovOrangeAccent)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Today's Slots", style = MaterialTheme.typography.labelSmall)
                    Text("${todayBookings.size}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Active Bookings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Completed", style = MaterialTheme.typography.labelSmall)
                    Text("$completedCount", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)))
                    Text("Delivered Slips", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Current Depot Stock Levels")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (shop != null) {
                    CommodityStockRow("Fortified Rice", shop.riceStock, 1500.0, "kg")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    CommodityStockRow("Sona Masuri Wheat", shop.wheatStock, 1000.0, "kg")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    CommodityStockRow("Refined Sugar", shop.sugarStock, 300.0, "kg")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    CommodityStockRow("Fortified Palm Oil", shop.oilStock, 400.0, "L")
                } else {
                    Text("No depot associated.")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Today's Appointment Queue List")
        if (todayBookings.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "No slot bookings scheduled for today.",
                    modifier = Modifier.padding(24.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            todayBookings.forEach { bkg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (bkg.status == "Pending") MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bkg.id, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(bkg.timeSlot, style = MaterialTheme.typography.bodySmall)
                            Text("Card: ${bkg.cardNo}", style = MaterialTheme.typography.labelSmall)
                        }

                        val color = when (bkg.status) {
                            "Completed" -> Color(0xFF2E7D32)
                            "Pending" -> GovOrangeAccent
                            else -> Color.Red
                        }
                        Badge(containerColor = color) {
                            Text(bkg.status.uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CommodityStockRow(name: String, current: Double, max: Double, unit: String) {
    val ratio = (current / max).toFloat().coerceIn(0f, 1f)
    val color = if (current < max * 0.2) Color(0xFFC62828) else if (current < max * 0.4) Color(0xFFEF6C00) else Color(0xFF2E7D32)

    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text("${current.toInt()} / ${max.toInt()} $unit", style = MaterialTheme.typography.bodyMedium.copy(color = color, fontWeight = FontWeight.ExtraBold))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { ratio },
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

// ------------------- Shopkeeper QR Verify Tab -------------------
@Composable
fun ShopkeeperVerifyTab(
    shop: ShopEntity?,
    bookings: List<SlotBookingEntity>,
    viewModel: RationViewModel
) {
    val scrollState = rememberScrollState()
    val verificationResult by viewModel.verificationResult.collectAsState()

    var manualTokenInput by remember { mutableStateOf("") }

    // Collect pending bookings for simple simulation clicks
    val pendingBookings = remember(bookings) {
        bookings.filter { it.status == "Pending" }
    }

    // Input fields for delivery quantities
    var riceDelivered by remember { mutableStateOf("0.0") }
    var wheatDelivered by remember { mutableStateOf("0.0") }
    var sugarDelivered by remember { mutableStateOf("0.0") }
    var oilDelivered by remember { mutableStateOf("0.0") }

    // Synchronize delivery inputs when verification details load successfully
    LaunchedEffect(verificationResult) {
        val result = verificationResult
        if (result is SlotVerificationResult.Valid) {
            // Pre-calculate remainders of entitlement and set as default deliverable
            val card = result.card
            riceDelivered = (card.riceEntitled - card.riceCollected).coerceAtLeast(0.0).toString()
            wheatDelivered = (card.wheatEntitled - card.wheatCollected).coerceAtLeast(0.0).toString()
            sugarDelivered = (card.sugarEntitled - card.sugarCollected).coerceAtLeast(0.0).toString()
            oilDelivered = (card.oilEntitled - card.oilCollected).coerceAtLeast(0.0).toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader("QR Code Verification Panel")

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Awaiting Token Scan",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Scan client's printed/digital QR token receipt or type Token Code manually below:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = manualTokenInput,
                    onValueChange = { manualTokenInput = it },
                    label = { Text("Manual QR Token String") },
                    placeholder = { Text("e.g. TOKEN-111122223333-FPS-50012-1405") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_token_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (manualTokenInput.trim().isEmpty()) {
                            Toast.makeText(viewModel.getApplication(), "Please enter a token", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.verifyQR(manualTokenInput.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_token_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GovBlueSecondary)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify & Load Details", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Token Simulator (To easily bypass manual typing!)
        if (pendingBookings.isNotEmpty() && verificationResult == null) {
            SectionHeader("DEPOT QUEUE (Tap a Citizen to Simulated-Scan QR)")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    pendingBookings.forEach { bkg ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    manualTokenInput = bkg.qrToken
                                    viewModel.verifyQR(bkg.qrToken)
                                }
                                .padding(12.dp)
                                .border(1.dp, GovBlueSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Token: ${bkg.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Card: ${bkg.cardNo} • Slot: ${bkg.timeSlot}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = {
                                manualTokenInput = bkg.qrToken
                                viewModel.verifyQR(bkg.qrToken)
                            }) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = GovBlueSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display scan verification results
        verificationResult?.let { result ->
            when (result) {
                is SlotVerificationResult.Invalid -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("❌ VERIFICATION INVALID", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(result.message, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.clearVerificationState() }) {
                                Text("Clear and Retry", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                is SlotVerificationResult.WrongShop -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("❌ WRONG SHOP DEPOT", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(result.message, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.clearVerificationState() }) {
                                Text("Dismiss", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                is SlotVerificationResult.WrongDate -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("⚠️ WRONG BOOKING DATE", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(result.message, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row {
                                TextButton(onClick = { viewModel.clearVerificationState() }) {
                                    Text("Dismiss", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                is SlotVerificationResult.Valid -> {
                    val card = result.card
                    val citizen = result.citizen

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verification_success_card")
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("✅ TOKEN APPROVED", fontWeight = FontWeight.Black, color = Color(0xFF2E7D32), fontSize = 16.sp)
                                Badge(containerColor = GovBlueSecondary) {
                                    Text(card.category, style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            Text("Citizen: ${citizen.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Aadhaar: ${citizen.id}", style = MaterialTheme.typography.bodySmall)
                            Text("Ration Card: ${card.cardNo}", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Verify Family Members present:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            result.family.forEach { member ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Text("- ${member.name} (${member.relation})", style = MaterialTheme.typography.bodySmall)
                                    Text("Age: ${member.age}", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            Text("Eligible Deliverable Remainder:", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall)

                            DeliverableCommodityRow("Rice (Fortified)", card.riceCollected, card.riceEntitled, "kg") { riceDelivered = it }
                            DeliverableCommodityRow("Wheat", card.wheatCollected, card.wheatEntitled, "kg") { wheatDelivered = it }
                            DeliverableCommodityRow("Refined Sugar", card.sugarCollected, card.sugarEntitled, "kg") { sugarDelivered = it }
                            DeliverableCommodityRow("Refined Oil", card.oilCollected, card.oilEntitled, "L") { oilDelivered = it }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val r = riceDelivered.toDoubleOrNull() ?: 0.0
                                    val w = wheatDelivered.toDoubleOrNull() ?: 0.0
                                    val s = sugarDelivered.toDoubleOrNull() ?: 0.0
                                    val o = oilDelivered.toDoubleOrNull() ?: 0.0

                                    viewModel.approveRationDistribution(
                                        result.booking.id,
                                        riceDelivered = r,
                                        wheatDelivered = w,
                                        sugarDelivered = s,
                                        oilDelivered = o
                                    ) { success ->
                                        if (success) {
                                            viewModel.clearVerificationState()
                                            manualTokenInput = ""
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("disburse_approve_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Complete Ration Distribution", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { viewModel.clearVerificationState() },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Cancel Verification", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DeliverableCommodityRow(name: String, collected: Double, entitled: Double, unit: String, onValueChange: (String) -> Unit) {
    val maxAvailable = (entitled - collected).coerceAtLeast(0.0)
    var currentInput by remember { mutableStateOf(maxAvailable.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text("Entitled: ${entitled.toInt()} | Collected: ${collected.toInt()} $unit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Distribute: ", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = currentInput,
                onValueChange = {
                    currentInput = it
                    onValueChange(it)
                },
                singleLine = true,
                modifier = Modifier
                    .width(72.dp)
                    .height(48.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Text(" $unit", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

// ------------------- Shopkeeper Inventory Tab -------------------
@Composable
fun ShopkeeperInventoryTab(shop: ShopEntity?, viewModel: RationViewModel) {
    val scrollState = rememberScrollState()

    var riceAdd by remember { mutableStateOf("") }
    var wheatAdd by remember { mutableStateOf("") }
    var sugarAdd by remember { mutableStateOf("") }
    var oilAdd by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader("Depot Stock Control Panel")

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Request Stock Replenishment / Log Shipment",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = GovBluePrimary
                )
                Text(
                    text = "Incoming supply delivery can be added directly here to sync depot inventory levels:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = riceAdd,
                    onValueChange = { riceAdd = it },
                    label = { Text("Add Rice (kg)") },
                    placeholder = { Text("e.g. 500") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = wheatAdd,
                    onValueChange = { wheatAdd = it },
                    label = { Text("Add Wheat (kg)") },
                    placeholder = { Text("e.g. 300") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = sugarAdd,
                    onValueChange = { sugarAdd = it },
                    label = { Text("Add Sugar (kg)") },
                    placeholder = { Text("e.g. 100") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = oilAdd,
                    onValueChange = { oilAdd = it },
                    label = { Text("Add Palm Oil (L)") },
                    placeholder = { Text("e.g. 150") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val r = riceAdd.toDoubleOrNull() ?: 0.0
                        val w = wheatAdd.toDoubleOrNull() ?: 0.0
                        val s = sugarAdd.toDoubleOrNull() ?: 0.0
                        val o = oilAdd.toDoubleOrNull() ?: 0.0

                        if (shop != null) {
                            viewModel.replenishStock(shop.id, r, w, s, o)
                            riceAdd = ""
                            wheatAdd = ""
                            sugarAdd = ""
                            oilAdd = ""
                            Toast.makeText(context, "Stock levels updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_stock_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GovBlueSecondary)
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register Shipment Delivery", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
