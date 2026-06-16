package com.example.data.repository

import com.example.data.dao.VehicleDao
import com.example.data.dao.BookingDao
import com.example.data.dao.ReviewDao
import com.example.data.dao.LoyaltyDao
import com.example.data.dao.UserProfileDao
import com.example.data.dao.UpcomingEventDao
import com.example.data.dao.CarTrackerDao
import com.example.data.model.Vehicle
import com.example.data.model.Booking
import com.example.data.model.CarReview
import com.example.data.model.LoyaltyProfile
import com.example.data.model.UserProfile
import com.example.data.model.UpcomingEvent
import com.example.data.model.CarTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CarRepository(
    private val vehicleDao: VehicleDao,
    private val bookingDao: BookingDao,
    private val reviewDao: ReviewDao,
    private val loyaltyDao: LoyaltyDao,
    private val userProfileDao: UserProfileDao,
    private val upcomingEventDao: UpcomingEventDao,
    private val carTrackerDao: CarTrackerDao
) {
    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    val allUpcomingEvents: Flow<List<UpcomingEvent>> = upcomingEventDao.getAllEvents()

    val allTrackers: Flow<List<CarTracker>> = carTrackerDao.getAllTrackers()

    suspend fun insertTracker(tracker: CarTracker) = carTrackerDao.insertTracker(tracker)

    suspend fun deleteTracker(tracker: CarTracker) = carTrackerDao.deleteTracker(tracker)

    suspend fun insertUpcomingEvent(event: UpcomingEvent) = upcomingEventDao.insertEvent(event)

    suspend fun updateUpcomingEvent(event: UpcomingEvent) = upcomingEventDao.updateEvent(event)

    suspend fun deleteUpcomingEvent(event: UpcomingEvent) = upcomingEventDao.deleteEvent(event)

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
                    title = "BMW Mini Cooper S",
                    category = "Small cars",
                    photoUrl = "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&w=800&q=80",
                    additionalPhotos = "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1502877338535-766e1452684a?auto=format&fit=crop&w=800&q=80",
                    pricePerHour = 3000.0,
                    originalPricePerHour = 3000.0,
                    status = "Available",
                    gpsLat = -1.2833,
                    gpsLng = 36.8219,
                    rating = 4.8f,
                    fuelType = "Petrol",
                    seats = 4,
                    transmission = "Automatic",
                    description = "Charming, agile, and incredibly quick on its feet. The turbocharged Mini Cooper S delivers go-kart handling, retro-modern cabin interfaces, and premium small-car luxury.",
                    locationName = "Milimani Corporate Estates"
                ),
                Vehicle(
                    title = "Mercedes-Benz C200 Saloon",
                    category = "Saloon cars",
                    photoUrl = "https://images.unsplash.com/photo-1622330248237-519e4541310d?auto=format&fit=crop&w=800&q=80",
                    additionalPhotos = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1553440569-bcc63803a83d?auto=format&fit=crop&w=800&q=80",
                    pricePerHour = 5500.0,
                    originalPricePerHour = 6000.0,
                    status = "Available",
                    gpsLat = -1.2721,
                    gpsLng = 36.8150,
                    rating = 4.7f,
                    fuelType = "Hybrid",
                    seats = 5,
                    transmission = "Automatic",
                    description = "The absolute paradigm of execution luxury and comfort. Premium hybrid efficiency combined with Whisper-quiet executive style, spacious rear legroom, and multi-zone ambient lighting.",
                    locationName = "Nairobi Central CBD Hub"
                ),
                Vehicle(
                    title = "Tesla Model S Plaid",
                    category = "High end cars",
                    photoUrl = "https://images.unsplash.com/photo-1614162692292-7ac56d7f7f1e?auto=format&fit=crop&w=800&q=80",
                    additionalPhotos = "https://images.unsplash.com/photo-1563720223185-11003d516935?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=800&q=80",
                    pricePerHour = 9500.0,
                    originalPricePerHour = 9500.0,
                    status = "Available",
                    gpsLat = -1.2580,
                    gpsLng = 36.8044,
                    rating = 4.9f,
                    fuelType = "Electric",
                    seats = 5,
                    transmission = "Automatic",
                    description = "Launch from 0 to 60 mph in 1.99 seconds. Features tri-motor electric all-wheel drive, futuristic yoke steering, epic gaming rig screen, and ultimate carbon-ceramic active styling.",
                    locationName = "Westlands Premium Suites"
                ),
                Vehicle(
                    title = "Porsche 911 GT3 RS",
                    category = "High end cars",
                    photoUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                    additionalPhotos = "https://images.unsplash.com/photo-1611245801163-68f3780bf724?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80",
                    pricePerHour = 14500.0,
                    originalPricePerHour = 14500.0,
                    status = "Available",
                    gpsLat = -1.3200,
                    gpsLng = 36.7020,
                    rating = 5.0f,
                    fuelType = "Petrol",
                    seats = 2,
                    transmission = "PDK Automatic",
                    description = "Pure motorsport engineering for the street. Aggressive carbon aerodynamics, a high-revving naturally aspirated flat-six engine pulling 9,000 rpm, and race-grade active suspension.",
                    locationName = "Karen Luxury Hills"
                ),
                Vehicle(
                    title = "Land Rover Defender 110",
                    category = "Seven seaters",
                    photoUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80",
                    additionalPhotos = "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1525609004556-c46c7d6cf0a3?auto=format&fit=crop&w=800&q=80",
                    pricePerHour = 8500.0,
                    originalPricePerHour = 8500.0,
                    status = "Rented",
                    gpsLat = -1.3192,
                    gpsLng = 36.9275,
                    rating = 4.8f,
                    fuelType = "Petrol",
                    seats = 7,
                    transmission = "Automatic",
                    description = "Iconic design reimagined for ultimate overland exploration. Outfitted with three rows accommodating 7 adult passengers, active electronic air suspension, and extreme low-range offroad gearing.",
                    locationName = "JKIA Airport Terminal Plaza"
                ),
                Vehicle(
                    title = "Eurocopter EC130 Luxury Helicopter",
                    category = "aircraft",
                    photoUrl = "https://images.unsplash.com/photo-1540962351504-03099e0a754b?auto=format&fit=crop&w=800&q=80",
                    additionalPhotos = "https://images.unsplash.com/photo-1494905998402-395d579af36f?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1583121274602-3e2820c69888?auto=format&fit=crop&w=800&q=80",
                    pricePerHour = 45000.0,
                    originalPricePerHour = 45000.0,
                    status = "Available",
                    gpsLat = -1.2335,
                    gpsLng = 36.8150,
                    rating = 4.9f,
                    fuelType = "Jet A-1",
                    seats = 6,
                    transmission = "Automated FADEC",
                    description = "Avoid traffic entirely and cruise the skylines in pure executive refinement. The EC130 offers high-visibility panoramic wraps, a whisper-quiet Fenestron tail rotor, plush leather captain seats, and direct point-to-point transit privileges.",
                    locationName = "Gigiri Diplomatic District Heliport"
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
                    pointsBalance = 0,
                    tier = "Silver",
                    totalBookings = 0,
                    totalSpent = 0.0
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

            // Seed initial upcoming events
            val sampleEvents = listOf(
                UpcomingEvent(
                    title = "Supercar Track Day Experience",
                    description = "Take our legendary Porsche 911 GT3 RS or Tesla Model S Plaid to the regional circuit for a professional high-speed track day. Professional track instruction included.",
                    dateText = "Saturday, July 4, 2026",
                    location = "Tatu City Circuit",
                    imageUrl = "https://images.unsplash.com/photo-1544829099-b9a0c07fad1a?auto=format&fit=crop&w=800&q=80"
                ),
                UpcomingEvent(
                    title = "Electric Future Tech Summit",
                    description = "A celebration of modern emissions-free performance. Explore our luxury electric fleet, receive direct battery diagnostics from charging staff, and attend exclusive panels on autonomous features.",
                    dateText = "Saturday, July 11, 2026",
                    location = "HQ Main Conference Hall",
                    imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?auto=format&fit=crop&w=800&q=80"
                ),
                UpcomingEvent(
                    title = "Helicopter Sunset Safari Tour",
                    description = "Skip the roads entirely for an afternoon. Board our Eurocopter EC130 for scenic loops above the Rift Valley, capped off with sunset bush cocktails and VIP ground transfers.",
                    dateText = "Sunday, July 19, 2026",
                    location = "Wilson Airport Hangar 4",
                    imageUrl = "https://images.unsplash.com/photo-1540962351504-03099e0a754b?auto=format&fit=crop&w=800&q=80"
                )
            )
            for (event in sampleEvents) {
                upcomingEventDao.insertEvent(event)
            }

            // Seed initial tracking registrations under current driver's phone numbers
            val sampleTrackers = listOf(
                CarTracker(
                    registrationNumber = "KCG 432B",
                    vehicleName = "Tesla Model S Plaid",
                    driverName = "Jeff Mwangi",
                    driverPhoneNumber = "+254 712 345678",
                    status = "En Route",
                    lastKnownLocation = "Westlands Premium Suites",
                    gpsCoordinates = "-1.2580, 36.8044",
                    speedKmh = 72
                ),
                CarTracker(
                    registrationNumber = "KDH 108C",
                    vehicleName = "Porsche 911 GT3 RS",
                    driverName = "Alex G. (Associate)",
                    driverPhoneNumber = "+254 755 889900",
                    status = "Stationary",
                    lastKnownLocation = "Karen Luxury Hills",
                    gpsCoordinates = "-1.3200, 36.7020",
                    speedKmh = 0
                ),
                CarTracker(
                    registrationNumber = "KDK 554W",
                    vehicleName = "Mercedes-Benz C200 Saloon",
                    driverName = "Chloe P. (Client)",
                    driverPhoneNumber = "+254 701 112233",
                    status = "En Route",
                    lastKnownLocation = "Nairobi Central CBD Hub",
                    gpsCoordinates = "-1.2721, 36.8150",
                    speedKmh = 45
                )
            )
            for (tracker in sampleTrackers) {
                carTrackerDao.insertTracker(tracker)
            }
        }
    }
}

