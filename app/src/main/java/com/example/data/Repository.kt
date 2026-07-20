package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class Repository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val rationCardDao = db.rationCardDao()
    private val familyMemberDao = db.familyMemberDao()
    private val shopDao = db.shopDao()
    private val bookingDao = db.bookingDao()
    private val complaintDao = db.complaintDao()
    private val announcementDao = db.announcementDao()

    // Exposed Flows for UI Consumption
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allShops: Flow<List<ShopEntity>> = shopDao.getAllShops()
    val allBookings: Flow<List<SlotBookingEntity>> = bookingDao.getAllBookings()
    val allComplaints: Flow<List<ComplaintEntity>> = complaintDao.getAllComplaints()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncements()

    fun getUser(id: String): Flow<UserEntity?> = userDao.getUserById(id)
    suspend fun getUserSync(id: String): UserEntity? = userDao.getUserByIdSync(id)

    fun getRationCard(cardNo: String): Flow<RationCardEntity?> = rationCardDao.getRationCard(cardNo)
    fun getFamilyMembers(cardNo: String): Flow<List<FamilyMemberEntity>> = familyMemberDao.getFamilyMembers(cardNo)
    fun getShop(id: String): Flow<ShopEntity?> = shopDao.getShop(id)
    fun getBookingsForCitizen(citizenId: String): Flow<List<SlotBookingEntity>> = bookingDao.getBookingsForCitizen(citizenId)
    fun getBookingsForShop(shopId: String): Flow<List<SlotBookingEntity>> = bookingDao.getBookingsForShop(shopId)
    fun getComplaintsForCitizen(citizenId: String): Flow<List<ComplaintEntity>> = complaintDao.getComplaintsForCitizen(citizenId)
    fun getBooking(id: String): Flow<SlotBookingEntity?> = bookingDao.getBooking(id)

    // User operations
    suspend fun insertUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    // Prepopulate database with realistic seed data if empty
    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        val existingUsers = userDao.getAllUsers().first()
        if (existingUsers.isEmpty()) {
            // 1. Create Users (Citizens, Shopkeeper, Admin)
            val citizens = listOf(
                UserEntity("111122223333", "Citizen", "Rajesh Kumar", "9876543210", "RC-BPL-98041"),
                UserEntity("444455556666", "Citizen", "Anjali Sharma", "8765432109", "RC-AAY-34012"),
                UserEntity("777788889999", "Citizen", "Vijay Reddy", "7654321098", "RC-APL-12053")
            )
            val shopkeepers = listOf(
                UserEntity("shopkeeper1", "Shopkeeper", "Mahesh Gowd", "9988776655", shopId = "FPS-50012"),
                UserEntity("shopkeeper2", "Shopkeeper", "Koteshwar Rao", "8877665544", shopId = "FPS-50034")
            )
            val admins = listOf(
                UserEntity("admin", "Admin", "Officer Sandeep", "9000100020")
            )

            (citizens + shopkeepers + admins).forEach { userDao.insertUser(it) }

            // 2. Create Ration Cards (BPL, AAY, APL)
            val rationCards = listOf(
                // Rajesh Kumar: BPL card with family
                RationCardEntity(
                    cardNo = "RC-BPL-98041",
                    category = "BPL",
                    address = "Block A, Sector 4, Shamshabad, Hyderabad",
                    riceEntitled = 25.0, riceCollected = 0.0,
                    wheatEntitled = 10.0, wheatCollected = 0.0,
                    sugarEntitled = 2.0, sugarCollected = 0.0,
                    oilEntitled = 3.0, oilCollected = 0.0
                ),
                // Anjali Sharma: AAY card (maximum entitlement, already collected current month)
                RationCardEntity(
                    cardNo = "RC-AAY-34012",
                    category = "AAY",
                    address = "Village Rampur, Mandal Shamshabad, Rangareddy",
                    riceEntitled = 35.0, riceCollected = 35.0,
                    wheatEntitled = 15.0, wheatCollected = 15.0,
                    sugarEntitled = 3.0, sugarCollected = 3.0,
                    oilEntitled = 4.0, oilCollected = 4.0
                ),
                // Vijay Reddy: APL card
                RationCardEntity(
                    cardNo = "RC-APL-12053",
                    category = "APL",
                    address = "Flat 302, Green Meadows, Gachibowli, Hyderabad",
                    riceEntitled = 12.0, riceCollected = 0.0,
                    wheatEntitled = 8.0, wheatCollected = 0.0,
                    sugarEntitled = 1.0, sugarCollected = 0.0,
                    oilEntitled = 1.0, oilCollected = 0.0
                )
            )

            rationCards.forEach { rationCardDao.insertRationCard(it) }

            // 3. Create Family Members
            val familyMembers = listOf(
                // Rajesh's family
                FamilyMemberEntity(cardNo = "RC-BPL-98041", name = "Sunita Kumar", relation = "Wife", age = 38),
                FamilyMemberEntity(cardNo = "RC-BPL-98041", name = "Amit Kumar", relation = "Son", age = 14),
                FamilyMemberEntity(cardNo = "RC-BPL-98041", name = "Priya Kumar", relation = "Daughter", age = 12),

                // Anjali's family
                FamilyMemberEntity(cardNo = "RC-AAY-34012", name = "Ramesh Sharma", relation = "Husband", age = 45),
                FamilyMemberEntity(cardNo = "RC-AAY-34012", name = "Rahul Sharma", relation = "Son", age = 18),

                // Vijay's family
                FamilyMemberEntity(cardNo = "RC-APL-12053", name = "Kavitha Reddy", relation = "Wife", age = 32)
            )

            familyMembers.forEach { familyMemberDao.insertFamilyMember(it) }

            // 4. Create Shops (with inventory)
            val shops = listOf(
                ShopEntity(
                    id = "FPS-50012",
                    name = "Shamshabad FPS Centre",
                    shopkeeperName = "Mahesh Gowd",
                    district = "Rangareddy",
                    mandal = "Shamshabad",
                    village = "Shamshabad",
                    workingHours = "08:00 AM - 05:00 PM",
                    riceStock = 1250.0,
                    wheatStock = 800.0,
                    sugarStock = 150.0,
                    oilStock = 220.0
                ),
                ShopEntity(
                    id = "FPS-50034",
                    name = "Gachibowli Government Depot",
                    shopkeeperName = "Koteshwar Rao",
                    district = "Hyderabad",
                    mandal = "Serilingampally",
                    village = "Gachibowli",
                    workingHours = "09:00 AM - 06:00 PM",
                    riceStock = 920.0,
                    wheatStock = 540.0,
                    sugarStock = 95.0,
                    oilStock = 110.0
                )
            )

            shops.forEach { shopDao.insertShop(it) }

            // 5. Create Announcements
            val announcements = listOf(
                AnnouncementEntity(
                    title = "Rice Allocations Enhanced for July 2026",
                    content = "Under the PMGKAY scheme, BPL and AAY category cardholders will receive an additional 5kg of Rice free of charge. Please verify your slips upon booking a time slot.",
                    date = "2026-07-18"
                ),
                AnnouncementEntity(
                    title = "FPS Biometric Verification Mandatory Update",
                    content = "All citizens are advised to complete their annual e-KYC verification at nearest FPS depots. Mobile OTP backup is available for senior citizens.",
                    date = "2026-07-15"
                ),
                AnnouncementEntity(
                    title = "Sugar Distribution Resumed in Rangareddy",
                    content = "Fresh sugar stocks have arrived at all fair price shops in Shamshabad mandal. Book your slot online to avoid long queues.",
                    date = "2026-07-12"
                )
            )

            announcements.forEach { announcementDao.insertAnnouncement(it) }

            // 6. Create Initial Complaints (Some pending, some resolved for admin and citizen lists)
            val complaints = listOf(
                ComplaintEntity(
                    id = "CMP-8731",
                    citizenId = "111122223333",
                    title = "Delay in Sugar Stock Arrival",
                    description = "The local shop FPS-50012 said sugar is unavailable, even though online stock says 150kg available.",
                    status = "Resolved",
                    dateRaised = "2026-07-10",
                    resolution = "Sugar stock updated and distributed successfully to complainant on July 13."
                ),
                ComplaintEntity(
                    id = "CMP-1024",
                    citizenId = "111122223333",
                    title = "Biometric Machine Malfunction",
                    description = "The fingerprint biometric scanner at Shamshabad FPS Centre often times out, leading to 20-minute delays per citizen verification.",
                    status = "Pending",
                    dateRaised = "2026-07-19"
                )
            )

            complaints.forEach { complaintDao.insertComplaint(it) }

            // 7. Create a Sample Historical Completed Booking
            val historicalBooking = SlotBookingEntity(
                id = "BKG-1052",
                citizenId = "444455556666",
                cardNo = "RC-AAY-34012",
                shopId = "FPS-50012",
                date = "2026-07-10",
                timeSlot = "09:00 AM - 10:00 AM",
                status = "Completed",
                qrToken = "TOKEN-444455556666-FPS-50012-Completed",
                estimatedWaitingTimeMinutes = 10,
                timestamp = System.currentTimeMillis() - 864000000 // 10 days ago
            )
            bookingDao.insertBooking(historicalBooking)
        }
    }

    // Business Action: Book slot
    suspend fun bookSlot(
        citizenId: String,
        cardNo: String,
        shopId: String,
        date: String,
        timeSlot: String
    ): Result<SlotBookingEntity> = withContext(Dispatchers.IO) {
        try {
            // Check stock availability
            val shop = shopDao.getShopSync(shopId) ?: return@withContext Result.failure(Exception("Shop not found"))
            if (shop.riceStock <= 0 && shop.wheatStock <= 0) {
                return@withContext Result.failure(Exception("No stock available at this Fair Price Shop"))
            }

            // Check if user has active slot booking already
            val userBookings = bookingDao.getBookingsForCitizen(citizenId).first()
            val hasActive = userBookings.any { it.status == "Pending" && it.date == date }
            if (hasActive) {
                return@withContext Result.failure(Exception("You already have an active booking on this date"))
            }

            // Calculate estimated waiting time (stochastic queue prediction)
            val shopBookings = bookingDao.getBookingsForShop(shopId).first()
            val parallelSlots = shopBookings.count { it.date == date && it.timeSlot == timeSlot && it.status == "Pending" }
            val estimatedTime = (parallelSlots * 8) + 10 // 8 minutes per appointment, baseline 10 minutes

            val bkgId = "BKG-${Random.nextInt(10000, 99999)}"
            val token = "TOKEN-$citizenId-$shopId-${Random.nextInt(1000, 9999)}"

            val newBooking = SlotBookingEntity(
                id = bkgId,
                citizenId = citizenId,
                cardNo = cardNo,
                shopId = shopId,
                date = date,
                timeSlot = timeSlot,
                status = "Pending",
                qrToken = token,
                estimatedWaitingTimeMinutes = estimatedTime
            )

            bookingDao.insertBooking(newBooking)
            Result.success(newBooking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Business Action: Cancel Booking
    suspend fun cancelBooking(bookingId: String) = withContext(Dispatchers.IO) {
        val booking = bookingDao.getBookingSync(bookingId)
        if (booking != null && booking.status == "Pending") {
            val cancelled = booking.copy(status = "Cancelled")
            bookingDao.insertBooking(cancelled)
        }
    }

    // Business Action: QR scan & validation (Returns details of booking if eligible or error)
    suspend fun verifyQRToken(qrToken: String, shopId: String): SlotVerificationResult = withContext(Dispatchers.IO) {
        val allB = bookingDao.getAllBookings().first()
        val booking = allB.find { it.qrToken == qrToken }
            ?: return@withContext SlotVerificationResult.Invalid("Token not found / Invalid QR Format")

        if (booking.status == "Cancelled") {
            return@withContext SlotVerificationResult.Invalid("This booking token has been Cancelled")
        }
        if (booking.status == "Completed") {
            return@withContext SlotVerificationResult.Invalid("This token was already USED and distribution is complete")
        }
        if (booking.shopId != shopId) {
            return@withContext SlotVerificationResult.WrongShop("This token is registered for Shop: ${booking.shopId}. You are logged into Shop: $shopId")
        }

        // Check if current date is booking date (soft warning or strict)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (booking.date != today) {
            return@withContext SlotVerificationResult.WrongDate("Token is registered for Date: ${booking.date}. Today is: $today", booking)
        }

        // Check card status
        val card = rationCardDao.getRationCardSync(booking.cardNo)
            ?: return@withContext SlotVerificationResult.Invalid("Associated Ration Card (${booking.cardNo}) not found")

        // If card already collected all entitled ration
        val hasRationRemaining = (card.riceEntitled > card.riceCollected) ||
                (card.wheatEntitled > card.wheatCollected) ||
                (card.sugarEntitled > card.sugarCollected) ||
                (card.oilEntitled > card.oilCollected)

        if (!hasRationRemaining) {
            return@withContext SlotVerificationResult.Invalid("Citizen has already collected 100% of monthly ration for all commodities.")
        }

        val citizen = userDao.getUserByIdSync(booking.citizenId)
            ?: return@withContext SlotVerificationResult.Invalid("Citizen profile not found")

        val family = familyMemberDao.getFamilyMembersSync(booking.cardNo)

        SlotVerificationResult.Valid(booking, card, citizen, family)
    }

    // Business Action: Complete distribution (Approved by Shopkeeper)
    suspend fun completeDistribution(
        bookingId: String,
        riceDelivered: Double,
        wheatDelivered: Double,
        sugarDelivered: Double,
        oilDelivered: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val booking = bookingDao.getBookingSync(bookingId) ?: return@withContext Result.failure(Exception("Booking not found"))
            val card = rationCardDao.getRationCardSync(booking.cardNo) ?: return@withContext Result.failure(Exception("Ration Card not found"))
            val shop = shopDao.getShopSync(booking.shopId) ?: return@withContext Result.failure(Exception("Shop not found"))

            // Verify stock levels at FPS shop
            if (shop.riceStock < riceDelivered || shop.wheatStock < wheatDelivered || shop.sugarStock < sugarDelivered || shop.oilStock < oilDelivered) {
                return@withContext Result.failure(Exception("Insufficient inventory in shop to deliver requested amounts"))
            }

            // Update Card
            val updatedCard = card.copy(
                riceCollected = card.riceCollected + riceDelivered,
                wheatCollected = card.wheatCollected + wheatDelivered,
                sugarCollected = card.sugarCollected + sugarDelivered,
                oilCollected = card.oilCollected + oilDelivered
            )
            rationCardDao.updateRationCard(updatedCard)

            // Update Shop Inventory
            val updatedShop = shop.copy(
                riceStock = shop.riceStock - riceDelivered,
                wheatStock = shop.wheatStock - wheatDelivered,
                sugarStock = shop.sugarStock - sugarDelivered,
                oilStock = shop.oilStock - oilDelivered
            )
            shopDao.updateShop(updatedShop)

            // Mark Booking completed
            val updatedBooking = booking.copy(status = "Completed")
            bookingDao.insertBooking(updatedBooking)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Business Action: Raise Complaint
    suspend fun raiseComplaint(citizenId: String, title: String, description: String) = withContext(Dispatchers.IO) {
        val cmpId = "CMP-${Random.nextInt(1000, 9999)}"
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val complaint = ComplaintEntity(
            id = cmpId,
            citizenId = citizenId,
            title = title,
            description = description,
            status = "Pending",
            dateRaised = today
        )
        complaintDao.insertComplaint(complaint)
    }

    // Business Action: Resolve Complaint (Admin)
    suspend fun resolveComplaint(complaintId: String, resolution: String) = withContext(Dispatchers.IO) {
        val allC = complaintDao.getAllComplaints().first()
        val complaint = allC.find { it.id == complaintId }
        if (complaint != null) {
            val resolved = complaint.copy(status = "Resolved", resolution = resolution)
            complaintDao.insertComplaint(resolved)
        }
    }

    // Business Action: Add new Fair Price Shop (Admin)
    suspend fun createShop(shop: ShopEntity) = withContext(Dispatchers.IO) {
        shopDao.insertShop(shop)
    }

    // Business Action: Update Shop Stock (Shopkeeper or Admin)
    suspend fun updateShopStock(shopId: String, riceAdd: Double, wheatAdd: Double, sugarAdd: Double, oilAdd: Double) = withContext(Dispatchers.IO) {
        val shop = shopDao.getShopSync(shopId)
        if (shop != null) {
            val updated = shop.copy(
                riceStock = shop.riceStock + riceAdd,
                wheatStock = shop.wheatStock + wheatAdd,
                sugarStock = shop.sugarStock + sugarAdd,
                oilStock = shop.oilStock + oilAdd
            )
            shopDao.updateShop(updated)
        }
    }

    // Business Action: Add Announcement
    suspend fun publishAnnouncement(title: String, content: String) = withContext(Dispatchers.IO) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ann = AnnouncementEntity(title = title, content = content, date = today)
        announcementDao.insertAnnouncement(ann)
    }
}

// Verification States for Shopkeeper App QR scan
sealed class SlotVerificationResult {
    data class Valid(
        val booking: SlotBookingEntity,
        val card: RationCardEntity,
        val citizen: UserEntity,
        val family: List<FamilyMemberEntity>
    ) : SlotVerificationResult()

    data class WrongDate(val message: String, val booking: SlotBookingEntity) : SlotVerificationResult()
    data class WrongShop(val message: String) : SlotVerificationResult()
    data class Invalid(val message: String) : SlotVerificationResult()
}
