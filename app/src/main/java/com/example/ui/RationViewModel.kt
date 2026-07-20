package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RationViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = Repository(db)

    // Session State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentRationCard = MutableStateFlow<RationCardEntity?>(null)
    val currentRationCard: StateFlow<RationCardEntity?> = _currentRationCard.asStateFlow()

    private val _currentFamilyMembers = MutableStateFlow<List<FamilyMemberEntity>>(emptyList())
    val currentFamilyMembers: StateFlow<List<FamilyMemberEntity>> = _currentFamilyMembers.asStateFlow()

    // Dynamic Lists for Dashboard UI
    val allShops: StateFlow<List<ShopEntity>> = repository.allShops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<AnnouncementEntity>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Citizen Specific Flows
    private val _citizenBookings = MutableStateFlow<List<SlotBookingEntity>>(emptyList())
    val citizenBookings: StateFlow<List<SlotBookingEntity>> = _citizenBookings.asStateFlow()

    private val _citizenComplaints = MutableStateFlow<List<ComplaintEntity>>(emptyList())
    val citizenComplaints: StateFlow<List<ComplaintEntity>> = _citizenComplaints.asStateFlow()

    // Admin / Shopkeeper Flows
    val allBookings: StateFlow<List<SlotBookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allComplaints: StateFlow<List<ComplaintEntity>> = repository.allComplaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // QR Verification State (Shopkeeper)
    private val _verificationResult = MutableStateFlow<SlotVerificationResult?>(null)
    val verificationResult: StateFlow<SlotVerificationResult?> = _verificationResult.asStateFlow()

    // General status and notification messaging
    private val _uiNotification = MutableStateFlow<String?>(null)
    val uiNotification: StateFlow<String?> = _uiNotification.asStateFlow()

    init {
        // Initialize Database with pre-populated values and listen to session updates
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    fun clearNotification() {
        _uiNotification.value = null
    }

    // Auth Simulation
    fun sendOtp(userId: String, onSent: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserSync(userId)
            if (user != null) {
                // Generate simulated OTP
                val otp = (100000..999999).random().toString()
                _uiNotification.value = "SMS Sent to ${user.mobile}: Your e-Ration OTP is $otp"
                onSent(otp)
            } else {
                _uiNotification.value = "Error: Aadhaar or User ID not registered."
            }
        }
    }

    fun login(userId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserSync(userId)
            if (user != null) {
                _currentUser.value = user
                _uiNotification.value = "Logged in successfully as ${user.name} (${user.role})"

                // If citizen, load relevant details
                if (user.role == "Citizen" && user.rationCardNo != null) {
                    loadCitizenData(user.id, user.rationCardNo)
                }
                onComplete(true)
            } else {
                _uiNotification.value = "Authentication failed. User not found."
                onComplete(false)
            }
        }
    }

    private fun loadCitizenData(citizenId: String, cardNo: String) {
        viewModelScope.launch {
            // Load Ration Card
            repository.getRationCard(cardNo).collectLatest { card ->
                _currentRationCard.value = card
            }
        }
        viewModelScope.launch {
            // Load Family members
            repository.getFamilyMembers(cardNo).collectLatest { members ->
                _currentFamilyMembers.value = members
            }
        }
        viewModelScope.launch {
            // Load Booking History
            repository.getBookingsForCitizen(citizenId).collectLatest { bkgs ->
                _citizenBookings.value = bkgs
            }
        }
        viewModelScope.launch {
            // Load Complaints
            repository.getComplaintsForCitizen(citizenId).collectLatest { cmps ->
                _citizenComplaints.value = cmps
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentRationCard.value = null
        _currentFamilyMembers.value = emptyList()
        _citizenBookings.value = emptyList()
        _citizenComplaints.value = emptyList()
        _verificationResult.value = null
        _uiNotification.value = "Logged out successfully"
    }

    // Citizen Slot Booking Action
    fun bookSlot(shopId: String, date: String, slot: String, onResult: (Boolean, String?) -> Unit) {
        val user = _currentUser.value ?: return
        val card = _currentRationCard.value ?: return

        viewModelScope.launch {
            val result = repository.bookSlot(user.id, card.cardNo, shopId, date, slot)
            if (result.isSuccess) {
                val booking = result.getOrNull()!!
                _uiNotification.value = "Slot Booking Confirmed! Token ID: ${booking.id}"
                onResult(true, null)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Unknown error booking slot"
                _uiNotification.value = "Booking Failed: $err"
                onResult(false, err)
            }
        }
    }

    // Citizen Cancel Slot Booking Action
    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            repository.cancelBooking(bookingId)
            _uiNotification.value = "Booking $bookingId cancelled successfully"
        }
    }

    // Citizen Raise Complaint Action
    fun raiseComplaint(title: String, description: String, onResult: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.raiseComplaint(user.id, title, description)
            _uiNotification.value = "Complaint submitted successfully and assigned to Mandal Officer."
            onResult()
        }
    }

    // Admin Resolve Complaint Action
    fun resolveComplaint(complaintId: String, resolution: String) {
        viewModelScope.launch {
            repository.resolveComplaint(complaintId, resolution)
            _uiNotification.value = "Complaint $complaintId resolved successfully"
        }
    }

    // Admin Create FPS Shop
    fun createShop(id: String, name: String, shopkeeperName: String, district: String, mandal: String, village: String, workingHours: String, initialStock: Double) {
        viewModelScope.launch {
            val newShop = ShopEntity(
                id = id,
                name = name,
                shopkeeperName = shopkeeperName,
                district = district,
                mandal = mandal,
                village = village,
                workingHours = workingHours,
                riceStock = initialStock,
                wheatStock = initialStock * 0.6, // scale proportionately
                sugarStock = initialStock * 0.1,
                oilStock = initialStock * 0.15
            )
            repository.createShop(newShop)
            _uiNotification.value = "New Fair Price Shop $id created successfully"
        }
    }

    // Admin Replenish Stock
    fun replenishStock(shopId: String, rice: Double, wheat: Double, sugar: Double, oil: Double) {
        viewModelScope.launch {
            repository.updateShopStock(shopId, rice, wheat, sugar, oil)
            _uiNotification.value = "Supplies delivered to shop $shopId successfully"
        }
    }

    // Admin Publish Government Announcement
    fun publishAnnouncement(title: String, content: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.publishAnnouncement(title, content)
            _uiNotification.value = "Announcement published successfully"
            onComplete()
        }
    }

    // Shopkeeper Scan / Verify QR Token
    fun verifyQR(qrToken: String) {
        val user = _currentUser.value ?: return
        val shopId = user.shopId ?: return

        viewModelScope.launch {
            val result = repository.verifyQRToken(qrToken, shopId)
            _verificationResult.value = result
            when (result) {
                is SlotVerificationResult.Valid -> _uiNotification.value = "Valid Token! Loaded Citizen details"
                is SlotVerificationResult.WrongDate -> _uiNotification.value = "Warning: Booking date is not today!"
                is SlotVerificationResult.WrongShop -> _uiNotification.value = "Error: Token registered for a different FPS Shop!"
                is SlotVerificationResult.Invalid -> _uiNotification.value = "Verification Failed: ${result.message}"
            }
        }
    }

    fun clearVerificationState() {
        _verificationResult.value = null
    }

    // Shopkeeper Distribute Commodity
    fun approveRationDistribution(
        bookingId: String,
        riceDelivered: Double,
        wheatDelivered: Double,
        sugarDelivered: Double,
        oilDelivered: Double,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.completeDistribution(bookingId, riceDelivered, wheatDelivered, sugarDelivered, oilDelivered)
            if (result.isSuccess) {
                _uiNotification.value = "Ration distributed successfully! Receipt Generated."
                _verificationResult.value = null // Reset scanning details
                onComplete(true)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Unknown error during distribution"
                _uiNotification.value = "Distribution Failed: $err"
                onComplete(false)
            }
        }
    }
}
