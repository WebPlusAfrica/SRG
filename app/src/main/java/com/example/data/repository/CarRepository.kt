package com.example.data.repository

import com.example.data.dao.VehicleDao
import com.example.data.dao.BookingDao
import com.example.data.dao.ReviewDao
import com.example.data.dao.LoyaltyDao
import com.example.data.dao.UserProfileDao
import com.example.data.model.Vehicle
import com.example.data.model.Booking
import com.example.data.model.CarReview
import com.example.data.model.LoyaltyProfile
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CarRepository(
    private val vehicleDao: VehicleDao,
    private val bookingDao: BookingDao,
    private val reviewDao: ReviewDao,
    private val loyaltyDao: LoyaltyDao,
    private val userProfileDao: UserProfileDao
) {
    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    fun getUserProfile(email: String): Flow<UserProfile?> = userProfileDao.getProfileFlow(email)
    
    suspend fun getUserProfileSuspend(email: String): UserProfile? = userProfileDao.getProfileSuspend(email)
    
    suspend fun insertUserProfile(profile: UserProfile) = userProfileDao.insertProfile(profile)


    fun getVehicleById(id: Int): Flow<Vehicle?> = vehicleDao.getVehicleById(id)

    suspend fun getVehicleByIdSuspend(id: Int): Vehicle? = vehicleDao.getVehicleByIdSuspend(id)

    suspend fun insertVehicle(vehicle: Vehicle) = vehicleDao.insertVehicle(vehicle)

    suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.updateVehicle(vehicle)

    suspend fun updateVehiclePrice(vehicleId: Int, price: Double) = vehicleDao.updateVehiclePrice(vehicleId, price)

    suspend fun updateVehicleStatus(vehicleId: Int, status: String) = vehicleDao.updateVehicleStatus(vehicleId, status)

    suspend fun updateVehicleGps(vehicleId: Int, lat: Double, lng: Double) = vehicleDao.updateVehicleGps(vehicleId, lat, lng)

    suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.deleteVehicle(vehicle)

    // Bookings
    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()

    fun getBookingsByEmail(email: String): Flow<List<Booking>> = bookingDao.getBookingsByEmail(email)

    fun getBookingById(id: Int): Flow<Booking?> = bookingDao.getBookingById(id)

    suspend fun insertBooking(booking: Booking): Long = bookingDao.insertBooking(booking)

    suspend fun updateBooking(booking: Booking) = bookingDao.updateBooking(booking)

    suspend fun updateBookingStatus(id: Int, status: String) = bookingDao.updateBookingStatus(id, status)

    suspend fun verifyBookingAgreement(id: Int, isVerified: Boolean, license: String, signature: String) {
        bookingDao.verifyBookingAgreement(id, isVerified, license, signature)
    }

    suspend fun updateBookingPayment(id: Int, paymentStatus: String) = bookingDao.updateBookingPayment(id, paymentStatus)

    // Reviews
    fun getReviewsForVehicle(vehicleId: Int): Flow<List<CarReview>> = reviewDao.getReviewsForVehicle(vehicleId)

    suspend fun insertReview(review: CarReview) = reviewDao.insertReview(review)

    // Loyalty Profile
    fun getLoyaltyProfile(email: String): Flow<LoyaltyProfile?> = loyaltyDao.getProfileFlow(email)

    suspend fun getLoyaltyProfileSuspend(email: String): LoyaltyProfile? = loyaltyDao.getProfileSuspend(email)

    suspend fun insertLoyaltyProfile(profile: LoyaltyProfile) = loyaltyDao.insertProfile(profile)

    suspend fun incrementLoyaltyPoints(email: String, pointsToAdd: Int, spentAmount: Double) {
        val currentProfile = loyaltyDao.getProfileSuspend(email) ?: LoyaltyProfile(email = email)
        val newPoints = currentProfile.pointsBalance + pointsToAdd
        val newSpent = currentProfile.totalSpent + spentAmount
        val newBookingsCount = currentProfile.totalBookings + 1
        
        // Dynamic Tier Progression
        val newTier = when {
            newPoints >= 1000 -> "Platinum"
            newPoints >= 400 -> "Gold"
            else -> "Silver"
        }
        
        loyaltyDao.insertProfile(
            currentProfile.copy(
                pointsBalance = newPoints,
                tier = newTier,
                totalBookings = newBookingsCount,
                totalSpent = newSpent
            )
        )
    }

    // Database seeding
    suspend fun seedInitialDataIfNecessary() {
        // We will pull the list first to see if it is empty
        val list = vehicleDao.getAllVehicles().firstOrNull() ?: emptyList()
        if (list.isEmpty()) {
            val initialVehicles = listOf(
                Vehicle(
                    title = "Tesla Model S Plaid",
                    category = "Electric Sedan",
                    photoUrl = "tesla_model_s",
                    pricePerHour = 9500.0,
                    originalPricePerHour = 9500.0,
                    status = "Available",
                    gpsLat = -1.2833,
                    gpsLng = 36.8219,
                    rating = 4.9f,
                    fuelType = "Electric",
                    seats = 5,
                    transmission = "Automatic",
                    description = "Launch from 0 to 60 mph in 1.99 seconds. Features tri-motor electric all-wheel drive, futuristic yoke steering, epic gaming rig screen, and ultimate carbon-ceramic active styling.",
                    locationName = "Nairobi Central CBD Hub"
                ),
                Vehicle(
                    title = "Porsche 911 GT3 RS",
                    category = "Supercar Coupe",
                    photoUrl = "porsche_911",
                    pricePerHour = 14500.0,
                    originalPricePerHour = 14500.0,
                    status = "Available",
                    gpsLat = -1.2680,
                    gpsLng = 36.8044,
                    rating = 5.0f,
                    fuelType = "Petrol",
                    seats = 2,
                    transmission = "PDK Automatic",
                    description = "Pure motorsport engineering for the street. Aggressive carbon aerodynamics, a high-revving naturally aspirated flat-six engine pulling 9,000 rpm, and race-grade active suspension.",
                    locationName = "Westlands Premium Suites"
                ),
                Vehicle(
                    title = "Audi R8 V10 Spyder",
                    category = "Convertible Supercar",
                    photoUrl = "audi_r8",
                    pricePerHour = 12000.0,
                    originalPricePerHour = 12500.0, // marked as discounted!
                    status = "Available",
                    gpsLat = -1.3200,
                    gpsLng = 36.7020,
                    rating = 4.8f,
                    fuelType = "Petrol",
                    seats = 2,
                    transmission = "Automatic",
                    description = "Exquisite acoustic excellence meets screaming high-revving mechanical power. Unleash the pure, roaring 5.2-litre naturally aspirated central V10 engine to its fullest open-air potential.",
                    locationName = "Karen Luxury Hills"
                ),
                Vehicle(
                    title = "Range Rover Sport SVR",
                    category = "Luxury Sports SUV",
                    photoUrl = "range_rover",
                    pricePerHour = 8000.0,
                    originalPricePerHour = 8000.0,
                    status = "Rented", // Active rented vehicle so GPS tracking animation is visible!
                    gpsLat = -1.3192,
                    gpsLng = 36.9275,
                    rating = 4.7f,
                    fuelType = "Petrol",
                    seats = 5,
                    transmission = "Automatic",
                    description = "Assertive sporting commanding stance with high luxury appointments and absolute performance. Supercharged V8 exhaust rumble that turns heads while keeping passengers in supreme comfort.",
                    locationName = "JKIA Airport Terminal Plaza"
                ),
                Vehicle(
                    title = "BMW i7 M70 Executive",
                    category = "Grand Sedan",
                    photoUrl = "bmw_i7",
                    pricePerHour = 11000.0,
                    originalPricePerHour = 11000.0,
                    status = "Available",
                    gpsLat = -1.2335,
                    gpsLng = 36.8150,
                    rating = 4.9f,
                    fuelType = "Electric",
                    seats = 5,
                    transmission = "Automatic",
                    description = "The ultimate executive electric cinema cruise experience. Rear passenger cabin includes a magnificent 31.3-inch theatre screen, fully reclining lounge seats, and a silent dual-motor high-volt drive.",
                    locationName = "Gigiri Diplomatic District"
                )
            )
            vehicleDao.insertVehicles(initialVehicles)

            // Seed initial reviews
            val sampleReviews = listOf(
                CarReview(vehicleId = 1, userName = "Alexander G.", rating = 5, comment = "Unbelievable acceleration. Highly recommend. It literally feels like a rocket ship taking off! No mechanical sound, just sheer speed."),
                CarReview(vehicleId = 1, userName = "Michael T.", rating = 4, comment = "Brilliant technology. App made unlocking/locking seamless."),
                CarReview(vehicleId = 2, userName = "Chloe P.", rating = 5, comment = "The absolute pinnacle of precision on tarmac. Standard of racing engineering that handles every corner flawlessly. Amazing sound!"),
                CarReview(vehicleId = 3, userName = "James R.", rating = 5, comment = "Rented for an weekend coastal drive with the top down. That V10 engine sound is purely intoxicating!")
            )
            for (review in sampleReviews) {
                reviewDao.insertReview(review)
            }

            // Seed a starter user loyalty profile
            loyaltyDao.insertProfile(
                LoyaltyProfile(
                    email = "jeffjmwangi@gmail.com",
                    pointsBalance = 250,
                    tier = "Gold",
                    totalBookings = 2,
                    totalSpent = 250000.0
                )
            )

            // Seed a default user profile
            userProfileDao.insertProfile(
                UserProfile(
                    email = "jeffjmwangi@gmail.com",
                    fullName = "Jeff J. Mwangi",
                    driverLicense = "DL-254-998877",
                    preferredVehicleCategory = "Supercar Coupe",
                    favoriteVehicleIds = "1,2",
                    phoneNumber = "+254 712 345678"
                )
            )
        }
    }
}
