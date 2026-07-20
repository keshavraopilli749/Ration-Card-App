package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
}

@Dao
interface RationCardDao {
    @Query("SELECT * FROM ration_cards WHERE cardNo = :cardNo")
    fun getRationCard(cardNo: String): Flow<RationCardEntity?>

    @Query("SELECT * FROM ration_cards WHERE cardNo = :cardNo")
    suspend fun getRationCardSync(cardNo: String): RationCardEntity?

    @Query("SELECT * FROM ration_cards")
    fun getAllRationCards(): Flow<List<RationCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRationCard(card: RationCardEntity)

    @Update
    suspend fun updateRationCard(card: RationCardEntity)
}

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members WHERE cardNo = :cardNo")
    fun getFamilyMembers(cardNo: String): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members WHERE cardNo = :cardNo")
    suspend fun getFamilyMembersSync(cardNo: String): List<FamilyMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMemberEntity)

    @Query("DELETE FROM family_members WHERE cardNo = :cardNo")
    suspend fun deleteFamilyMembersForCard(cardNo: String)
}

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops WHERE id = :id")
    fun getShop(id: String): Flow<ShopEntity?>

    @Query("SELECT * FROM shops WHERE id = :id")
    suspend fun getShopSync(id: String): ShopEntity?

    @Query("SELECT * FROM shops")
    fun getAllShops(): Flow<List<ShopEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity)

    @Update
    suspend fun updateShop(shop: ShopEntity)
}

@Dao
interface SlotBookingDao {
    @Query("SELECT * FROM bookings WHERE citizenId = :citizenId ORDER BY timestamp DESC")
    fun getBookingsForCitizen(citizenId: String): Flow<List<SlotBookingEntity>>

    @Query("SELECT * FROM bookings WHERE shopId = :shopId ORDER BY timestamp DESC")
    fun getBookingsForShop(shopId: String): Flow<List<SlotBookingEntity>>

    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<SlotBookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun getBooking(id: String): Flow<SlotBookingEntity?>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingSync(id: String): SlotBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: SlotBookingEntity)

    @Update
    suspend fun updateBooking(booking: SlotBookingEntity)
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints WHERE citizenId = :citizenId ORDER BY dateRaised DESC")
    fun getComplaintsForCitizen(citizenId: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints ORDER BY dateRaised DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE id = :id")
    fun getComplaint(id: String): Flow<ComplaintEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)
}
