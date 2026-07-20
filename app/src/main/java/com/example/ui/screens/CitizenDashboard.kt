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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDashboard(viewModel: RationViewModel, onLogout: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentUser by viewModel.currentUser.collectAsState()
    val currentCard by viewModel.currentRationCard.collectAsState()
    val familyMembers by viewModel.currentFamilyMembers.collectAsState()
    val bookings by viewModel.citizenBookings.collectAsState()
    val complaints by viewModel.citizenComplaints.collectAsState()
    val shops by viewModel.allShops.collectAsState()
    val announcements by viewModel.allAnnouncements.collectAsState()

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
                    .testTag("citizen_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("tab_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Book Slot") },
                    label = { Text("Book Slot") },
                    modifier = Modifier.testTag("tab_book")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = "My Tokens") },
                    label = { Text("My Tokens") },
                    modifier = Modifier.testTag("tab_tokens")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Feedback, contentDescription = "Complaints") },
                    label = { Text("Complaints") },
                    modifier = Modifier.testTag("tab_complaints")
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
            // Citizen Header
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
                        text = "GOVERNMENT OF INDIA",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Namaste, ${currentUser?.name ?: "Citizen"}",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier
                        .testTag("logout_button")
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
                    0 -> CitizenHomeTab(currentCard, familyMembers, announcements)
                    1 -> CitizenBookSlotTab(shops, viewModel) { selectedTab = 2 } // Switch to tokens upon successful booking
                    2 -> CitizenTokensTab(bookings, viewModel)
                    3 -> CitizenComplaintsTab(complaints, viewModel)
                }
            }
        }
    }
}

// ------------------- Citizen Home Tab -------------------
@Composable
fun CitizenHomeTab(
    card: RationCardEntity?,
    family: List<FamilyMemberEntity>,
    announcements: List<AnnouncementEntity>
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        if (card != null) {
            SectionHeader("Your Digital Ration Card")
            RationCardBadge(card)

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader("This Month's Ration Quota (July 2026)")
            CommodityProgressMeter("Rice (Fortified)", card.riceCollected, card.riceEntitled, "kg")
            CommodityProgressMeter("Wheat", card.wheatCollected, card.wheatEntitled, "kg")
            CommodityProgressMeter("Refined Sugar", card.sugarCollected, card.sugarEntitled, "kg")
            CommodityProgressMeter("Karanj/Palm Oil", card.oilCollected, card.oilEntitled, "L")
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = "No associated Ration Card found for this Aadhaar profile. Please visit Nearest District Food Office.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Family Details")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)), // Slate 200 border
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (family.isEmpty()) {
                    Text("No registered family members listed under this card.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    family.forEachIndexed { idx, member ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(member.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(member.relation, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                }
                            }
                            Text("${member.age} Yrs", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        if (idx < family.size - 1) {
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Government Bulletins & Announcements")
        if (announcements.isEmpty()) {
            Text("No announcements at this time.", style = MaterialTheme.typography.bodyMedium)
        } else {
            announcements.forEach { ann ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)), // Slate 200 border
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = ann.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GovBluePrimary)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = ann.date,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF2563EB),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ann.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ------------------- Citizen Slot Booking Tab -------------------
@Composable
fun CitizenBookSlotTab(
    shops: List<ShopEntity>,
    viewModel: RationViewModel,
    onBookingSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var selectedShop by remember { mutableStateOf<ShopEntity?>(null) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf("") }

    val dates = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in 0..2) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val timeSlots = listOf(
        "08:00 AM - 09:00 AM",
        "09:00 AM - 10:00 AM",
        "10:00 AM - 11:00 AM",
        "11:00 AM - 12:00 PM",
        "02:00 PM - 03:00 PM",
        "03:00 PM - 04:00 PM",
        "04:00 PM - 05:00 PM"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader("1. Select Nearest Fair Price Shop (FPS)")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (shops.isEmpty()) {
                    Text("No Fair Price Shops registered in your district.")
                } else {
                    shops.forEach { shop ->
                        val isSelected = selectedShop?.id == shop.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GovBluePrimary.copy(alpha = 0.1f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) GovBluePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedShop = shop }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedShop = shop }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(shop.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Village: ${shop.village}, Mandal: ${shop.mandal}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text("Working Hours: ${shop.workingHours}", style = MaterialTheme.typography.labelSmall.copy(color = GovBlueSecondary, fontWeight = FontWeight.SemiBold))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("2. Select Available Date")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dates.forEach { date ->
                val isSelected = selectedDate == date
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GovBlueSecondary else MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedDate = date }
                        .padding(vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (date == dates[0]) "TODAY" else if (date == dates[1]) "TOMORROW" else "DAY AFTER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = date.substring(5), // Show MM-DD
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("3. Select Available Time Slot")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                timeSlots.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { slot ->
                            val isSelected = selectedSlot == slot
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GovOrangeAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedSlot = slot }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = slot,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Queue and waiting predictor
        AnimatedVisibility(visible = selectedShop != null && selectedDate.isNotEmpty() && selectedSlot.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, contentDescription = "Queue prediction", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Queue Predictor: HIGH EFFICIENCY SLOT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        )
                        Text(
                            text = "Estimated Waiting Time at Shop: ~12 mins",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        )
                        Text(
                            text = "By booking this digital token, you skip the physical queue. Visit with QR code.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val shop = selectedShop
                if (shop == null || selectedDate.isEmpty() || selectedSlot.isEmpty()) {
                    Toast.makeText(context, "Please complete all three selections.", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.bookSlot(shop.id, selectedDate, selectedSlot) { success, _ ->
                        if (success) {
                            onBookingSuccess()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_booking_button"),
            colors = ButtonDefaults.buttonColors(containerColor = GovBluePrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.BookOnline, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Confirm Appointment", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ------------------- Citizen Tokens Tab -------------------
@Composable
fun CitizenTokensTab(bookings: List<com.example.data.SlotBookingEntity>, viewModel: RationViewModel) {
    val pending = bookings.filter { it.status == "Pending" }
    val historical = bookings.filter { it.status != "Pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            SectionHeader("Active Digital Token")
        }

        if (pending.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = GovBluePrimary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Active Slot Booked",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Book an online time slot in the 'Book Slot' tab to generate a queue-free QR Token receipt.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        } else {
            items(pending) { bkg ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("active_token_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "BOOKING TOKEN",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GovBluePrimary, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = bkg.id,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                                )
                            }
                            Badge(containerColor = GovOrangeAccent) {
                                Text("PENDING SCAN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        // QR canvas drawing
                        CanvasQRCode(token = bkg.qrToken, modifier = Modifier.padding(vertical = 12.dp))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DATE", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(bkg.date, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("TIME SLOT", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(bkg.timeSlot, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("FPS SHOP CODE", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(bkg.shopId, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("EST. WAIT TIME", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text("~${bkg.estimatedWaitingTimeMinutes} mins", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, color = GovBlueSecondary))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedButton(
                            onClick = { viewModel.cancelBooking(bkg.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cancel_booking_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel Appointment", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("Booking & Distribution History")
        }

        if (historical.isEmpty()) {
            item {
                Text(
                    text = "No past bookings recorded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(historical) { bkg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bkg.id, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("${bkg.date} • ${bkg.timeSlot}", style = MaterialTheme.typography.bodySmall)
                            Text("Shop ID: ${bkg.shopId}", style = MaterialTheme.typography.labelSmall)
                        }

                        val color = if (bkg.status == "Completed") Color(0xFF2E7D32) else Color.Red
                        Badge(containerColor = color) {
                            Text(bkg.status.uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
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

// ------------------- Citizen Complaints Tab -------------------
@Composable
fun CitizenComplaintsTab(complaints: List<com.example.data.ComplaintEntity>, viewModel: RationViewModel) {
    var expandedForm by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }

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
                SectionHeader("Grievance & Redressal Desk")
                Button(
                    onClick = { expandedForm = !expandedForm },
                    colors = ButtonDefaults.buttonColors(containerColor = GovBlueSecondary)
                ) {
                    Icon(if (expandedForm) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (expandedForm) "Close" else "File Dispute")
                }
            }
        }

        if (expandedForm) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("complaint_form_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Register Formal Grevience",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GovBluePrimary)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Subject / Issue Title") },
                            placeholder = { Text("e.g., Shop closed during working hours") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("complaint_title_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text("Detailed Description") },
                            placeholder = { Text("Specify FPS shop ID, date, time and commodities denied...") },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("complaint_desc_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (titleInput.isEmpty() || descInput.isEmpty()) {
                                    Toast.makeText(context, "Please write both subject and description", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.raiseComplaint(titleInput.trim(), descInput.trim()) {
                                        titleInput = ""
                                        descInput = ""
                                        expandedForm = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_complaint_btn")
                        ) {
                            Text("Submit Formal Grievance", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("Registered Complaints & Status")
        }

        if (complaints.isEmpty()) {
            item {
                Text(
                    text = "No complaints submitted. The Grievance desk is empty.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(complaints) { cmp ->
                val isResolved = cmp.status == "Resolved"
                val statusColor = if (isResolved) Color(0xFF2E7D32) else Color(0xFFEF6C00)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cmp.id,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = cmp.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Badge(containerColor = statusColor) {
                                Text(cmp.status.uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = cmp.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isResolved && cmp.resolution != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "RESOLUTION BY OFFICER:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cmp.resolution,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1B5E20)
                                    )
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
