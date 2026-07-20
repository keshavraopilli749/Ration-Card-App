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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(viewModel: RationViewModel, onLogout: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentUser by viewModel.currentUser.collectAsState()
    val shops by viewModel.allShops.collectAsState()
    val complaints by viewModel.allComplaints.collectAsState()
    val bookings by viewModel.allBookings.collectAsState()

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
                    .testTag("admin_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Overview") },
                    label = { Text("Overview") },
                    modifier = Modifier.testTag("admin_tab_overview")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Store, contentDescription = "Shops") },
                    label = { Text("Shops") },
                    modifier = Modifier.testTag("admin_tab_shops")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Feedback, contentDescription = "Grievances") },
                    label = { Text("Grievances") },
                    modifier = Modifier.testTag("admin_tab_grievances")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "Announce") },
                    label = { Text("Announce") },
                    modifier = Modifier.testTag("admin_tab_announce")
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
            // Admin Header
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
                        text = "DISTRICT ADMINISTRATIVE HEAD",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = currentUser?.name ?: "District Officer",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Mandal: Rangareddy Central Office",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.9f))
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier
                        .testTag("admin_logout_button")
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = Color.White)
                }
            }

            // Tabs Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> AdminOverviewTab(shops, complaints, bookings)
                    1 -> AdminShopsTab(shops, viewModel)
                    2 -> AdminGrievancesTab(complaints, viewModel)
                    3 -> AdminAnnounceTab(viewModel)
                }
            }
        }
    }
}
// ------------------- Admin Overview Tab -------------------
@Composable
fun AdminOverviewTab(
    shops: List<ShopEntity>,
    complaints: List<ComplaintEntity>,
    bookings: List<SlotBookingEntity>
) {
    val scrollState = rememberScrollState()

    val totalCitizens = 3 // Standard demo population
    val pendingComplaints = complaints.count { it.status == "Pending" }
    val totalDisbursements = bookings.count { it.status == "Completed" }

    // Chart analytics preparation
    val shopNames = remember(shops) { shops.map { it.name.take(12) } }
    val riceStocks = remember(shops) { shops.map { it.riceStock.toFloat() } }
    val wheatStocks = remember(shops) { shops.map { it.wheatStock.toFloat() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader("District Operational Dashboard")

        // Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = GovBluePrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total Shops", style = MaterialTheme.typography.labelSmall)
                    Text("${shops.size}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.People, contentDescription = null, tint = GovBlueSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Citizens", style = MaterialTheme.typography.labelSmall)
                    Text("$totalCitizens", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Disbursements", style = MaterialTheme.typography.labelSmall)
                    Text("$totalDisbursements", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)))
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Feedback, contentDescription = null, tint = GovOrangeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Complaints", style = MaterialTheme.typography.labelSmall)
                    Text("$pendingComplaints", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = if (pendingComplaints > 0) Color.Red else Color.Black))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Real-Time Analytics & Consumption Graphs")

        if (shops.isNotEmpty()) {
            CustomAnalyticsChart(
                title = "Fortified Rice Stock Levels (kg) by Shop",
                dataPoints = riceStocks,
                labels = shopNames
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomAnalyticsChart(
                title = "Sona Masuri Wheat Stock Levels (kg) by Shop",
                dataPoints = wheatStocks,
                labels = shopNames
            )
        } else {
            Text("No shop data available to plot analytics.")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ------------------- Admin Shops Tab -------------------
@Composable
fun AdminShopsTab(shops: List<ShopEntity>, viewModel: RationViewModel) {
    var expandCreate by remember { mutableStateOf(false) }

    var shopId by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var shopkeeper by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Rangareddy") }
    var mandal by remember { mutableStateOf("Shamshabad") }
    var village by remember { mutableStateOf("") }
    var workingHours by remember { mutableStateOf("08:00 AM - 05:00 PM") }
    var initialStock by remember { mutableStateOf("1000.0") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionHeader("Fair Price Shops Registry")
                Button(
                    onClick = { expandCreate = !expandCreate },
                    colors = ButtonDefaults.buttonColors(containerColor = GovBlueSecondary)
                ) {
                    Icon(if (expandCreate) Icons.Default.Close else Icons.Default.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (expandCreate) "Close" else "Add Shop")
                }
            }
        }

        if (expandCreate) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("admin_create_shop_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Register New Licensed FPS Depot",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GovBluePrimary)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = shopId,
                            onValueChange = { shopId = it },
                            label = { Text("Unique FPS License ID") },
                            placeholder = { Text("e.g. FPS-50078") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("Shop Centre Name") },
                            placeholder = { Text("e.g. Rajendranagar FPS Depot") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = shopkeeper,
                            onValueChange = { shopkeeper = it },
                            label = { Text("Licensed Dealer / Shopkeeper Name") },
                            placeholder = { Text("e.g. G. Narasimha Rao") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mandal,
                                onValueChange = { mandal = it },
                                label = { Text("Mandal") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = village,
                                onValueChange = { village = it },
                                label = { Text("Village / Ward") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = initialStock,
                            onValueChange = { initialStock = it },
                            label = { Text("Initial Stock Load (Rice kg)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val sId = shopId.trim()
                                val name = shopName.trim()
                                val dealer = shopkeeper.trim()
                                val vil = village.trim()
                                val stock = initialStock.toDoubleOrNull() ?: 1000.0

                                if (sId.isEmpty() || name.isEmpty() || dealer.isEmpty() || vil.isEmpty()) {
                                    Toast.makeText(context, "Please complete all registration fields", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.createShop(
                                        id = sId,
                                        name = name,
                                        shopkeeperName = dealer,
                                        district = district,
                                        mandal = mandal,
                                        village = vil,
                                        workingHours = workingHours,
                                        initialStock = stock
                                    )
                                    // reset inputs
                                    shopId = ""
                                    shopName = ""
                                    shopkeeper = ""
                                    village = ""
                                    expandCreate = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_shop_submit_btn")
                        ) {
                            Text("Provision License & Open Shop", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(shops) { shop ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(shop.id, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GovBluePrimary))
                            Text(shop.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Badge(containerColor = GovBlueSecondary) {
                            Text(shop.mandal.uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Licensed Dealer: ${shop.shopkeeperName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Location: Village ${shop.village}, Mandal ${shop.mandal}, ${shop.district} District", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Current stock levels on-hand:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text("Rice: ${shop.riceStock.toInt()}kg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Wheat: ${shop.wheatStock.toInt()}kg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Sugar: ${shop.sugarStock.toInt()}kg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Oil: ${shop.oilStock.toInt()}L", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ------------------- Admin Grievances Tab -------------------
@Composable
fun AdminGrievancesTab(complaints: List<ComplaintEntity>, viewModel: RationViewModel) {
    var expandedComplaintId by remember { mutableStateOf("") }
    var resolutionText by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            SectionHeader("Citizen Grievance Resolution Desk")
        }

        if (complaints.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "No public complaints filed. District systems running smoothly.",
                        modifier = Modifier.padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(complaints) { cmp ->
                val isResolved = cmp.status == "Resolved"
                val cardColor = if (isResolved) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            if (!isResolved) {
                                expandedComplaintId = if (expandedComplaintId == cmp.id) "" else cmp.id
                                resolutionText = ""
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cmp.id, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text(cmp.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Badge(containerColor = if (isResolved) Color(0xFF2E7D32) else GovOrangeAccent) {
                                Text(cmp.status.uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(cmp.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Raised by Citizen Aadhaar: ${cmp.citizenId} on ${cmp.dateRaised}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))

                        if (isResolved && cmp.resolution != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text("Resolution Logged: ${cmp.resolution}", color = Color(0xFF1B5E20), style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        AnimatedVisibility(visible = expandedComplaintId == cmp.id && !isResolved) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                OutlinedTextField(
                                    value = resolutionText,
                                    onValueChange = { resolutionText = it },
                                    label = { Text("Official Action taken / Resolution Text") },
                                    placeholder = { Text("e.g. Inspector dispatched to FPS depot, stock replenished...") },
                                    minLines = 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_resolution_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (resolutionText.trim().isEmpty()) {
                                            Toast.makeText(context, "Please enter a resolution description", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.resolveComplaint(cmp.id, resolutionText.trim())
                                            expandedComplaintId = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("resolve_complaint_btn")
                                ) {
                                    Text("Mark Grievance as RESOLVED", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ------------------- Admin Announce Tab -------------------
@Composable
fun AdminAnnounceTab(viewModel: RationViewModel) {
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader("Publish Official Government Announcements")

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Broadcast News Bulletin",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = GovBluePrimary
                )
                Text(
                    text = "This bulletin will immediately display on the home screen of all citizen profiles in the district:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Announcement Title") },
                    placeholder = { Text("e.g. Free Rice Schemes Augmented") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Bulletin Content Message") },
                    placeholder = { Text("Under central scheme, we enhanced allocations to...") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val t = title.trim()
                        val c = content.trim()

                        if (t.isEmpty() || c.isEmpty()) {
                            Toast.makeText(context, "Please fill both title and content", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.publishAnnouncement(t, c) {
                                title = ""
                                content = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("publish_announce_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GovBlueSecondary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Official Broadcast", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
