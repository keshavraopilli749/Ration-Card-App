package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // Aadhaar number for Citizens, ID/username for others
    val role: String, // "Citizen", "Shopkeeper", "Admin"
    val name: String,
    val mobile: String,
    val rationCardNo: String? = null,
    val shopId: String? = null
)

@Entity(tableName = "ration_cards")
data class RationCardEntity(
    @PrimaryKey val cardNo: String,
    val category: String, // "APL" (Above Poverty Line), "BPL" (Below Poverty Line), "AAY" (Antyodaya Anna Yojana)
    val address: String,
    val riceEntitled: Double,
    val riceCollected: Double,
    val wheatEntitled: Double,
    val wheatCollected: Double,
    val sugarEntitled: Double,
    val sugarCollected: Double,
    val oilEntitled: Double,
    val oilCollected: Double
)

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardNo: String,
    val name: String,
    val relation: String,
    val age: Int
)

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey val id: String, // FPS shop code
    val name: String,
    val shopkeeperName: String,
    val district: String,
    val mandal: String,
    val village: String,
    val workingHours: String,
    val riceStock: Double,
    val wheatStock: Double,
    val sugarStock: Double,
    val oilStock: Double
)

@Entity(tableName = "bookings")
data class SlotBookingEntity(
    @PrimaryKey val id: String, // BKG-XXXX
    val citizenId: String,
    val cardNo: String,
    val shopId: String,
    val date: String,
    val timeSlot: String,
    val status: String, // "Pending", "Completed", "Cancelled"
    val qrToken: String,
    val estimatedWaitingTimeMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String, // CMP-XXXX
    val citizenId: String,
    val title: String,
    val description: String,
    val status: String, // "Pending", "Assigned", "Resolved"
    val dateRaised: String,
    val imageUri: String? = null,
    val resolution: String? = null
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: String
)
