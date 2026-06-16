package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Luxury Sedan", "Supercar", "SUV", "Electric Coupe"
    val photoUrl: String, // image placeholder code or custom drawable
    val additionalPhotos: String = "", // Comma-separated list of additional image URLs
    val pricePerHour: Double,
    val originalPricePerHour: Double,
    val status: String, // "Available", "Rented", "Maintenance"
    val gpsLat: Double,
    val gpsLng: Double,
    val rating: Float,
    val fuelType: String, // "Electric", "Hybrid", "Petrol"
    val seats: Int,
    val transmission: String, // "Automatic", "Manual"
    val description: String,
    val locationName: String
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val vehicleTitle: String,
    val vehicleCategory: String,
    val vehiclePhotoUrl: String,
    val pricePerHourAtBooking: Double,
    val durationHours: Int,
    val totalSpent: Double,
    val bookedAt: Long,
    val userEmail: String,
    val status: String, // "Awaiting Verification", "Active", "Completed", "Cancelled"
    val signature: String? = null,
    val isVerified: Boolean = false,
    val paymentStatus: String = "Unpaid", // "Paid", "Pending", "Refunded"
    val drivingLicenseNumber: String? = null,
    val pickupTime: Long = 0L,
    val returnTime: Long = 0L
)

@Entity(tableName = "reviews")
data class CarReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val userName: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "loyalty_profile")
data class LoyaltyProfile(
    @PrimaryKey val email: String,
    val pointsBalance: Int = 120, // 120 starter points!
    val tier: String = "Silver", // "Silver", "Gold", "Platinum"
    val totalBookings: Int = 0,
    val totalSpent: Double = 0.0
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String,
    val fullName: String = "Jeff J. Mwangi",
    val driverLicense: String = "",
    val preferredVehicleCategory: String = "Electric Sedan",
    val favoriteVehicleIds: String = "", // comma-separated vehicle IDs
    val phoneNumber: String = "+254 712 345678"
)

@Entity(tableName = "upcoming_events")
data class UpcomingEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dateText: String,
    val location: String = "Nairobi Corporate HQ",
    val imageUrl: String = ""
)

@Entity(tableName = "car_trackers")
data class CarTracker(
    @PrimaryKey val registrationNumber: String,
    val vehicleName: String,
    val driverName: String,
    val driverPhoneNumber: String,
    val status: String, // "En Route", "Stationary", "Completed"
    val lastKnownLocation: String,
    val gpsCoordinates: String, // e.g., "-1.2833, 36.8219"
    val speedKmh: Int = 0
)

