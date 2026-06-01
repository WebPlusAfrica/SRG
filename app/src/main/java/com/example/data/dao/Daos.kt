package com.example.data.dao

import androidx.room.*
import com.example.data.model.Vehicle
import com.example.data.model.Booking
import com.example.data.model.CarReview
import com.example.data.model.LoyaltyProfile
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY title ASC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    fun getVehicleById(id: Int): Flow<Vehicle?>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleByIdSuspend(id: Int): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<Vehicle>)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Query("UPDATE vehicles SET pricePerHour = :price WHERE id = :vehicleId")
    suspend fun updateVehiclePrice(vehicleId: Int, price: Double)

    @Query("UPDATE vehicles SET status = :status WHERE id = :vehicleId")
    suspend fun updateVehicleStatus(vehicleId: Int, status: String)

    @Query("UPDATE vehicles SET gpsLat = :lat, gpsLng = :lng WHERE id = :vehicleId")
    suspend fun updateVehicleGps(vehicleId: Int, lat: Double, lng: Double)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY bookedAt DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE userEmail = :email ORDER BY bookedAt DESC")
    fun getBookingsByEmail(email: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    fun getBookingById(id: Int): Flow<Booking?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Int, status: String)

    @Query("UPDATE bookings SET isVerified = :isVerified, drivingLicenseNumber = :license, signature = :sig WHERE id = :id")
    suspend fun verifyBookingAgreement(id: Int, isVerified: Boolean, license: String, sig: String)

    @Query("UPDATE bookings SET paymentStatus = :paymentStatus WHERE id = :id")
    suspend fun updateBookingPayment(id: Int, paymentStatus: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE vehicleId = :vehicleId ORDER BY createdAt DESC")
    fun getReviewsForVehicle(vehicleId: Int): Flow<List<CarReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: CarReview)
}

@Dao
interface LoyaltyDao {
    @Query("SELECT * FROM loyalty_profile WHERE email = :email LIMIT 1")
    fun getProfileFlow(email: String): Flow<LoyaltyProfile?>

    @Query("SELECT * FROM loyalty_profile WHERE email = :email LIMIT 1")
    suspend fun getProfileSuspend(email: String): LoyaltyProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: LoyaltyProfile)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    fun getProfileFlow(email: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileSuspend(email: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)
}

