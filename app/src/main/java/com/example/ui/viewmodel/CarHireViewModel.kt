package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.Booking
import com.example.data.model.CarReview
import com.example.data.model.LoyaltyProfile
import com.example.data.model.UserProfile
import com.example.data.model.Vehicle
import com.example.data.model.UpcomingEvent
import com.example.data.model.CarTracker
import com.example.data.repository.CarRepository
import com.example.data.api.GeminiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class CarHireViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = CarRepository(
        db.vehicleDao(),
        db.bookingDao(),
        db.reviewDao(),
        db.loyaltyDao(),
        db.userProfileDao(),
        db.upcomingEventDao(),
        db.carTrackerDao()
    )

    // Current logged-in user email
    val currentUserEmail = "jeffjmwangi@gmail.com"
    val currentUserName = "Jeff J. Mwangi"

    // Observed databases flows
    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingEvents: StateFlow<List<UpcomingEvent>> = repository.allUpcomingEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookings: StateFlow<List<Booking>> = repository.getBookingsByEmail(currentUserEmail)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val loyaltyProfile: StateFlow<LoyaltyProfile?> = repository.getLoyaltyProfile(currentUserEmail)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userProfile: StateFlow<UserProfile?> = repository.getUserProfile(currentUserEmail)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val carTrackers: StateFlow<List<CarTracker>> = repository.allTrackers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun adminRegisterTracker(
        regNo: String,
        vName: String,
        dName: String,
        dPhone: String,
        stat: String,
        loc: String,
        coords: String,
        speed: Int
    ) {
        viewModelScope.launch {
            val newTracker = CarTracker(
                registrationNumber = regNo,
                vehicleName = vName,
                driverName = dName,
                driverPhoneNumber = dPhone,
                status = stat,
                lastKnownLocation = loc,
                gpsCoordinates = coords,
                speedKmh = speed
            )
            repository.insertTracker(newTracker)
            _adminLog.value = "Registered GPS tracker for registration: $regNo linked to $dPhone"
            addNotification("Tracker Active", "Vehicle registration $regNo is now being tracked live under $dPhone.", "success")
        }
    }

    fun adminDeleteTracker(tracker: CarTracker) {
        viewModelScope.launch {
            repository.deleteTracker(tracker)
            _adminLog.value = "Deleted tracking registry for: ${tracker.registrationNumber}"
        }
    }

    // UI State selectors
    private val _selectedVehicleId = MutableStateFlow<Int?>(null)
    val selectedVehicleId = _selectedVehicleId.asStateFlow()

    private val _notificationLog = MutableStateFlow<List<AppNotification>>(emptyList())
    val notificationLog = _notificationLog.asStateFlow()

    private val _activeBookingReceipt = MutableStateFlow<Booking?>(null)
    val activeBookingReceipt = _activeBookingReceipt.asStateFlow()

    // Screen navigation route holder
    private val _currentTab = MutableStateFlow("explore") // explore, map, bookings, ai_support, loyalty, admin
    val currentTab = _currentTab.asStateFlow()

    // State flow to trigger a formal booking confirmation modal showing details & license agreement
    private val _showBookingConfirmationModal = MutableStateFlow<Booking?>(null)
    val showBookingConfirmationModal = _showBookingConfirmationModal.asStateFlow()

    fun dismissBookingConfirmation() {
        _showBookingConfirmationModal.value = null
    }

    fun triggerBookingConfirmation(booking: Booking) {
        _showBookingConfirmationModal.value = booking
    }

    // Light/Dark Theme Preference State
    private val themePrefs = application.getSharedPreferences("srg_theme_prefs", android.content.Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(themePrefs.getBoolean("is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleTheme() {
        val nextVal = !_isDarkMode.value
        _isDarkMode.value = nextVal
        themePrefs.edit().putBoolean("is_dark_mode", nextVal).apply()
    }

    // Review statistics map
    private val _selectedCarReviews = MutableStateFlow<List<CarReview>>(emptyList())
    val selectedCarReviews = _selectedCarReviews.asStateFlow()

    // AI Chat Support Widget State
    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(listOf(
        "Hello! I am SRG Bot, your expert AI guide at SRG car hire. Ask me anything about our luxury fleet, dynamic prices, GPS unlocking, digital agreements, or how to checkout securely!" to false
    ))
    val chatMessages = _chatMessages.asStateFlow()

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping = _isAiTyping.asStateFlow()

    // Admin authorization state
    private val _isAdminAuthorized = MutableStateFlow(false)
    val isAdminAuthorized = _isAdminAuthorized.asStateFlow()

    // Admin custom status or error message
    private val _adminLog = MutableStateFlow<String?>(null)
    val adminLog = _adminLog.asStateFlow()

    // Local simulated location updates for map tracking (e.g. simulating GPS moving around)
    private val _gpsSimulationAngle = MutableStateFlow(0.0)

    // Dynamic pricing notification banner state
    private val _dynamicPricingAlert = MutableStateFlow<String?>(null)
    val dynamicPricingAlert = _dynamicPricingAlert.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedInitialDataIfNecessary()
            addNotification("Welcome to SRG car hire", "Explore our premium sports and electric models. 120 welcome Loyalty Points credited!", "info")
            startSimulationLoops()
        }
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun selectVehicle(vehicleId: Int?) {
        _selectedVehicleId.value = vehicleId
        if (vehicleId != null) {
            viewModelScope.launch {
                repository.getReviewsForVehicle(vehicleId).collect { reviews ->
                    _selectedCarReviews.value = reviews
                }
            }
        } else {
            _selectedCarReviews.value = emptyList()
        }
    }

    // System Push Notification Helper
    fun addNotification(title: String, body: String, type: String = "info") {
        val newNotification = AppNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            body = body,
            timestamp = System.currentTimeMillis(),
            type = type,
            isRead = false
        )
        _notificationLog.value = listOf(newNotification) + _notificationLog.value
    }

    fun clearNotifications() {
        _notificationLog.value = emptyList()
    }

    // Dynamic simulation loops: real-time GPS motions and minor dynamic price swings to model true real-time operations
    private fun startSimulationLoops() {
        // GPS track loop (runs every 3 seconds to move active/rented vehicles on London Map)
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(4000)
                _gpsSimulationAngle.value += 0.05
                val angle = _gpsSimulationAngle.value
                
                val currentVehicles = vehicles.value
                currentVehicles.forEach { vehicle ->
                    if (vehicle.status == "Rented" || vehicle.id == 4) { // Simulate active track
                        // Drift around its starting central coordinate
                        val newLat = vehicle.gpsLat + (kotlin.math.sin(angle) * 0.0003)
                        val newLng = vehicle.gpsLng + (kotlin.math.cos(angle) * 0.0003)
                        repository.updateVehicleGps(vehicle.id, newLat, newLng)
                    }
                }
            }
        }

        // Dynamic Pricing Swings (runs every 20 seconds to raise/lower rates dynamically based on simulated 'peak midday demand' shifts)
        viewModelScope.launch(Dispatchers.IO) {
            val random = Random()
            while (true) {
                delay(20000)
                val currentVehicles = vehicles.value
                if (currentVehicles.isNotEmpty()) {
                    val targetIndex = random.nextInt(currentVehicles.size)
                    val vehicle = currentVehicles[targetIndex]
                    
                    // Modulate price between -5% to +10% of its baseOriginalPrice to represent real market pressures
                    val base = vehicle.originalPricePerHour
                    val swingPercent = -0.05 + (random.nextDouble() * 0.15) // [-5%, +10%]
                    val newPrice = Math.round(base * (1.0 + swingPercent) * 100.0) / 100.0
                    
                    repository.updateVehiclePrice(vehicle.id, newPrice)
                    
                    val changeWord = if (newPrice > base) "increased to reflect high midday demand" else "promoted with local off-peak hourly savings"
                    val formattedPrice = String.format(Locale.US, "$%.2f", newPrice)
                    _dynamicPricingAlert.value = "Dynamic Pricing Update: Live rate for ${vehicle.title} $changeWord at $formattedPrice/hr!"
                    
                    addNotification(
                        "Dynamic Price Updated",
                        "${vehicle.title} rate updated dynamically to $formattedPrice/hr",
                        "pricing"
                    )
                    
                    delay(6000)
                    _dynamicPricingAlert.value = null
                }
            }
        }
    }

    // Reservation Draft Lifecycle
    fun initiateBooking(vehicle: Vehicle, durationHours: Int) {
        viewModelScope.launch {
            val totalBaseAmount = vehicle.pricePerHour * durationHours
            val draft = Booking(
                vehicleId = vehicle.id,
                vehicleTitle = vehicle.title,
                vehicleCategory = vehicle.category,
                vehiclePhotoUrl = vehicle.photoUrl,
                pricePerHourAtBooking = vehicle.pricePerHour,
                durationHours = durationHours,
                totalSpent = totalBaseAmount,
                bookedAt = System.currentTimeMillis(),
                userEmail = currentUserEmail,
                status = "Awaiting Verification",
                isVerified = false,
                paymentStatus = "Unpaid"
            )
            val generatedId = repository.insertBooking(draft)
            val finalizedBooking = draft.copy(id = generatedId.toInt())
            _activeBookingReceipt.value = finalizedBooking
            _showBookingConfirmationModal.value = finalizedBooking
            
            addNotification(
                "Booking Initiated",
                "Successfully initiated reservation for ${vehicle.title}. Please complete electronic agreement signing.",
                "booking"
            )
            _currentTab.value = "profile"
        }
    }

    // Digital electronic agreement upload and license validation
    fun submitAgreementVerification(bookingId: Int, licenseNumber: String, signature: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _activeBookingReceipt.value?.let { currentDraft ->
                if (currentDraft.id == bookingId) {
                    val updatedBooking = currentDraft.copy(
                        status = "Awaiting Payment",
                        drivingLicenseNumber = licenseNumber,
                        signature = signature,
                        isVerified = true
                    )
                    repository.verifyBookingAgreement(bookingId, true, licenseNumber, signature)
                    repository.updateBookingStatus(bookingId, "Awaiting Payment")
                    _activeBookingReceipt.value = updatedBooking
                    
                    // Persist verified license number to user's phone profile
                    val currentProfile = repository.getUserProfileSuspend(currentUserEmail) ?: UserProfile(currentUserEmail)
                    repository.insertUserProfile(currentProfile.copy(driverLicense = licenseNumber))
                    
                    addNotification(
                        "Document Verified",
                        "Driving license $licenseNumber approved instantly via SRG Digital Verification backend.",
                        "verification"
                    )
                }
            }
        }
    }

    // Secure Credit Card payment gateway handler (simulated API gateway)
    fun processSecurePayment(bookingId: Int, cardNumber: String, expiry: String, cvv: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentBooking = _activeBookingReceipt.value
            if (currentBooking != null && currentBooking.id == bookingId) {
                // Perform gateway capture simulation
                delay(1500) // simulation delay
                
                // Update booking status in database to active/confirmed
                val updatedBooking = currentBooking.copy(
                    paymentStatus = "Paid",
                    status = "Active"
                )
                repository.updateBookingPayment(bookingId, "Paid")
                repository.updateBookingStatus(bookingId, "Active")
                repository.updateVehicleStatus(currentBooking.vehicleId, "Rented")
                
                _activeBookingReceipt.value = updatedBooking
                
                // Award Loyalty rewards points! E.g. 1 point for every 1000 ksh spent
                val loyaltyEarned = Math.max(1, (currentBooking.totalSpent / 1000).toInt())
                repository.incrementLoyaltyPoints(currentUserEmail, loyaltyEarned, currentBooking.totalSpent)
                
                addNotification(
                    "Secured Payment Approved",
                    "Transaction of Ksh. ${String.format(Locale.US, "%,.2f", currentBooking.totalSpent)} approved. GPS vehicle lock is now enabled!",
                    "payment"
                )
                addNotification(
                    "Loyalty Points Credited",
                    "Earned $loyaltyEarned loyalty points for your booking. Safe driving in Kenya!",
                    "loyalty"
                )
            }
        }
    }

    // Secure M-Pesa or Cash payment gateway handler (simulated)
    fun processAlternativePayment(bookingId: Int, paymentMethod: String, phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentBooking = _activeBookingReceipt.value
            if (currentBooking != null && currentBooking.id == bookingId) {
                // Perform payment simulation delay
                delay(1500)
                
                if (paymentMethod == "M-Pesa") {
                    // Update booking status to Pending (STK push sent, awaiting PIN confirm)
                    val updatedBooking = currentBooking.copy(
                        paymentStatus = "Pending"
                    )
                    repository.updateBookingPayment(bookingId, "Pending")
                    _activeBookingReceipt.value = updatedBooking
                    
                    // Save payment phone number to user's persistent phone profile
                    val currentProfile = repository.getUserProfileSuspend(currentUserEmail) ?: UserProfile(currentUserEmail)
                    repository.insertUserProfile(currentProfile.copy(phoneNumber = phoneNumber))
                    
                    addNotification(
                        "M-Pesa STK Dispatched",
                        "STK push PIN request sent to $phoneNumber for Ksh. ${String.format(Locale.US, "%,.2f", currentBooking.totalSpent)}. Please enter your PIN to authorize payment.",
                        "payment"
                    )
                } else {
                    // Live Cash handover updates instantly
                    val updatedBooking = currentBooking.copy(
                        paymentStatus = "Paid",
                        status = "Active"
                    )
                    repository.updateBookingPayment(bookingId, "Paid")
                    repository.updateBookingStatus(bookingId, "Active")
                    repository.updateVehicleStatus(currentBooking.vehicleId, "Rented")
                    
                    _activeBookingReceipt.value = updatedBooking
                    
                    // Save payment phone number to user's persistent phone profile
                    val currentProfile = repository.getUserProfileSuspend(currentUserEmail) ?: UserProfile(currentUserEmail)
                    repository.insertUserProfile(currentProfile.copy(phoneNumber = phoneNumber))
                    
                    // Award loyalty points: 1 point for every Ksh 1000 spent
                    val loyaltyEarned = Math.max(1, (currentBooking.totalSpent / 1000).toInt())
                    repository.incrementLoyaltyPoints(currentUserEmail, loyaltyEarned, currentBooking.totalSpent)
                    
                    addNotification(
                        "$paymentMethod Processed",
                        "Cash coordination approved. Fleet agent will contact you shortly on $phoneNumber.",
                        "payment"
                    )
                    addNotification(
                        "Loyalty Points Credited",
                        "Earned $loyaltyEarned loyalty points for your booking. Safe driving in Kenya!",
                        "loyalty"
                    )
                }
            }
        }
    }

    // Explicit verification helper when client actually pays (e.g., via simulated PIN prompt or status poll check)
    fun confirmMpesaPayment(bookingId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentBooking = _activeBookingReceipt.value
            if (currentBooking != null && currentBooking.id == bookingId) {
                val updatedBooking = currentBooking.copy(
                    paymentStatus = "Paid",
                    status = "Active"
                )
                repository.updateBookingPayment(bookingId, "Paid")
                repository.updateBookingStatus(bookingId, "Active")
                repository.updateVehicleStatus(currentBooking.vehicleId, "Rented")
                
                _activeBookingReceipt.value = updatedBooking
                
                // Award loyalty points: 1 point for every Ksh 1000 spent
                val loyaltyEarned = Math.max(1, (currentBooking.totalSpent / 1000).toInt())
                repository.incrementLoyaltyPoints(currentUserEmail, loyaltyEarned, currentBooking.totalSpent)
                
                addNotification(
                    "M-Pesa Payment Confirmed",
                    "STK Push payment of Ksh. ${String.format(Locale.US, "%,.2f", currentBooking.totalSpent)} verified. Smart GPS remote unlocking is active!",
                    "payment"
                )
                addNotification(
                    "Loyalty Points Credited",
                    "Earned $loyaltyEarned loyalty points for your booking. Safe driving in Kenya!",
                    "loyalty"
                )
            }
        }
    }

    // Store user profile details
    fun updateUserProfile(fullName: String, license: String, preferredCategory: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getUserProfileSuspend(currentUserEmail) ?: UserProfile(currentUserEmail)
            val updated = current.copy(
                fullName = fullName,
                driverLicense = license,
                preferredVehicleCategory = preferredCategory,
                phoneNumber = phone
            )
            repository.insertUserProfile(updated)
            addNotification("Profile Updated", "Your driver license credentials have been stored securely.", "info")
        }
    }

    // Add / remove favorites
    fun toggleFavoriteCar(vehicleId: Int) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile(currentUserEmail)
            val favsList = if (current.favoriteVehicleIds.isBlank()) {
                emptyList()
            } else {
                current.favoriteVehicleIds.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }
            
            val carIdStr = vehicleId.toString()
            val newFavs = if (favsList.contains(carIdStr)) {
                favsList - carIdStr
            } else {
                favsList + carIdStr
            }
            
            val updated = current.copy(favoriteVehicleIds = newFavs.filter { it.isNotBlank() }.joinToString(","))
            repository.insertUserProfile(updated)
            
            val vehicle = vehicles.value.find { it.id == vehicleId }
            val carTitle = vehicle?.title ?: "Vehicle"
            val isAdded = newFavs.contains(carIdStr)
            addNotification(
                if (isAdded) "Favorite Added" else "Favorite Removed",
                "\"$carTitle\" has been ${if (isAdded) "added to" else "removed from"} your personalized favorites.",
                "info"
            )
        }
    }

    // Initiate flexible rental booking spanning dates & times
    fun initiateFlexibleBooking(vehicle: Vehicle, pickupMs: Long, returnMs: Long) {
        viewModelScope.launch {
            val diffMs = returnMs - pickupMs
            // Minimum of 1 hour, standard calculation
            val hours = Math.max(1, (diffMs / (1000 * 60 * 60)).toInt())
            val totalSpentAmount = vehicle.pricePerHour * hours
            
            val draft = Booking(
                vehicleId = vehicle.id,
                vehicleTitle = vehicle.title,
                vehicleCategory = vehicle.category,
                vehiclePhotoUrl = vehicle.photoUrl,
                pricePerHourAtBooking = vehicle.pricePerHour,
                durationHours = hours,
                totalSpent = totalSpentAmount,
                bookedAt = System.currentTimeMillis(),
                userEmail = currentUserEmail,
                status = "Awaiting Verification",
                isVerified = false,
                paymentStatus = "Unpaid",
                pickupTime = pickupMs,
                returnTime = returnMs
            )
            val generatedId = repository.insertBooking(draft)
            val finalizedBooking = draft.copy(id = generatedId.toInt())
            _activeBookingReceipt.value = finalizedBooking
            _showBookingConfirmationModal.value = finalizedBooking
            
            addNotification(
                "Flexible Booking Created",
                "Successfully reserved ${vehicle.title} for a custom duration ($hours Hours) in Kenya.",
                "booking"
            )
            _currentTab.value = "profile"
        }
    }

    // Complete / Return Ride
    fun completeRide(bookingId: Int, vehicleId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBookingStatus(bookingId, "Completed")
            repository.updateVehicleStatus(vehicleId, "Available")
            _activeBookingReceipt.value = null
            
            addNotification(
                "Vehicle Safely Returned",
                "Your premium rental has been processed. GPS locks secured. Thank you for riding with SRG!",
                "info"
            )
        }
    }

    // Reviews & Star-ratings creation
    fun submitCarReview(vehicleId: Int, rating: Int, comment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newReview = CarReview(
                vehicleId = vehicleId,
                userName = currentUserName,
                rating = rating,
                comment = comment
            )
            repository.insertReview(newReview)
            
            // Recalculate average rating of vehicle
            val vehicleFlow = repository.getVehicleById(vehicleId)
            val vehicle = vehicleFlow.firstOrNull() ?: repository.getVehicleByIdSuspend(vehicleId)
            if (vehicle != null) {
                // Get all reviews
                val reviewsList = repository.getReviewsForVehicle(vehicleId).firstOrNull() ?: emptyList()
                val additionalRatingSum = reviewsList.sumOf { it.rating } + rating
                val averageRating = (additionalRatingSum.toFloat()) / (reviewsList.size + 1)
                val roundedRating = Math.round(averageRating * 10f) / 10f
                val updatedVehicle = vehicle.copy(rating = roundedRating)
                repository.updateVehicle(updatedVehicle)
            }
            
            addNotification(
                "Feedback Submitted",
                "Thank you for rating our vehicle at $rating Stars! Your review is posted.",
                "info"
            )
            
            // Refresh local selected reviews observable
            repository.getReviewsForVehicle(vehicleId).collect {
                _selectedCarReviews.value = it
            }
        }
    }

    // AI Support chatbot triggers
    fun sendSupportPrompt(prompt: String) {
        if (prompt.isBlank()) return
        
        val updatedHistory = _chatMessages.value + (prompt to true)
        _chatMessages.value = updatedHistory
        _isAiTyping.value = true
        
        viewModelScope.launch {
            try {
                // Pass conversational history for proper context window reasoning
                val response = GeminiHelper.getChatResponse(prompt, updatedHistory.takeLast(10))
                _chatMessages.value = _chatMessages.value + (response to false)
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ("I'm experiencing an internal error processing your request. Rest assured, SRG Car Hire has premium sports models ready on your map widget. Let me know if you need rental pricing guidelines!" to false)
            } finally {
                _isAiTyping.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            "Hello! I am SRG Bot, your expert AI guide at SRG car hire. Ask me anything about our luxury fleet, dynamic prices, GPS unlocking, digital agreements, or how to checkout securely!" to false
        )
    }

    private val sharedPrefs = application.getSharedPreferences("srg_admin_prefs", android.content.Context.MODE_PRIVATE)

    fun getAdminPasscode(): String {
        return sharedPrefs.getString("admin_passcode", "SRGADMIN") ?: "SRGADMIN"
    }

    fun updateAdminPasscode(newPasscode: String): Boolean {
        if (newPasscode.trim().length < 4) {
            _adminLog.value = "Error: Passcode must be at least 4 characters long."
            return false
        }
        sharedPrefs.edit().putString("admin_passcode", newPasscode.trim()).apply()
        _adminLog.value = "Passcode changed successfully to: ${newPasscode.trim()}"
        addNotification("Security Updated", "Administrative passcode was changed securely by admin.", "warning")
        return true
    }

    // Hidden Admin panel gate checks
    fun authorizeAdmin(passcode: String): Boolean {
        val currentPasscode = getAdminPasscode()
        return if (passcode.trim() == currentPasscode) {
            _isAdminAuthorized.value = true
            _adminLog.value = "System unlocked. Administrative mode: ENGAGED."
            addNotification("Executive Panel Engaged", "Welcome. Administrative permissions have been biometrically/passcode authorized.", "warning")
            true
        } else {
            _isAdminAuthorized.value = false
            _adminLog.value = "Authorization denied. Secure passcode incorrect."
            false
        }
    }

    fun deauthorizeAdmin() {
        _isAdminAuthorized.value = false
        _adminLog.value = null
    }

    // Administrative updates for prices, vehicles, and photo placeholders
    fun adminUpdateVehiclePrice(vehicleId: Int, newPrice: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val vehicle = repository.getVehicleByIdSuspend(vehicleId)
            if (vehicle != null) {
                val updated = vehicle.copy(pricePerHour = newPrice, originalPricePerHour = newPrice)
                repository.updateVehicle(updated)
                _adminLog.value = "Prices updated for ${vehicle.title} to $${String.format(Locale.US, "%.2f", newPrice)}/hr."
                addNotification("Price Override", "${vehicle.title} rate updated by admin to $${String.format(Locale.US, "%.2f", newPrice)}/hr", "warning")
            }
        }
    }

    fun adminAddVehicle(
        title: String,
        category: String,
        price: Double,
        fuelType: String,
        seats: Int,
        trans: String,
        description: String,
        location: String,
        photoUrl: String = "",
        additionalPhotos: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newVehicle = Vehicle(
                title = title,
                category = category,
                pricePerHour = price,
                originalPricePerHour = price,
                photoUrl = photoUrl.ifBlank { "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80" },
                additionalPhotos = additionalPhotos,
                status = "Available",
                gpsLat = -1.2833 + (Random().nextDouble() * 0.05 - 0.025),
                gpsLng = 36.8219 + (Random().nextDouble() * 0.05 - 0.025),
                rating = 5.0f,
                fuelType = fuelType,
                seats = seats,
                transmission = trans,
                description = description,
                locationName = location
            )
            repository.insertVehicle(newVehicle)
            _adminLog.value = "New luxury vehicle added successfully: $title ($category)."
            addNotification("Inventory Expanded", "$title added to local fleet at $location", "warning")
        }
    }

    fun adminDeleteVehicle(vehicleId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val vehicle = repository.getVehicleByIdSuspend(vehicleId)
            if (vehicle != null) {
                repository.deleteVehicle(vehicle)
                _adminLog.value = "Vehicle '${vehicle.title}' deleted from server database."
                addNotification("Fleet Reduced", "'${vehicle.title}' was decommissioned from secondary active fleet lists.", "warning")
            }
        }
    }

    fun adminAddUpcomingEvent(title: String, description: String, dateText: String, location: String, imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val event = UpcomingEvent(
                title = title,
                description = description,
                dateText = dateText,
                location = location,
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1544829099-b9a0c07fad1a?auto=format&fit=crop&w=800&q=80" }
            )
            repository.insertUpcomingEvent(event)
            _adminLog.value = "New event published: $title."
            addNotification("New Event Scheduled", "$title is now advertised on client carousels", "warning")
        }
    }

    fun adminUpdateUpcomingEvent(event: UpcomingEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUpcomingEvent(event)
            _adminLog.value = "Event updated: ${event.title}."
            addNotification("Event Revised", "'${event.title}' scheduled details updated securely", "warning")
        }
    }

    fun adminDeleteUpcomingEvent(event: UpcomingEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUpcomingEvent(event)
            _adminLog.value = "Event '${event.title}' removed from publication."
            addNotification("Event Cancelled", "Advertising for '${event.title}' has been withdrawn", "warning")
        }
    }
}


// Notification Entity Model
data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val type: String, // info, booking, verification, payment, loyalty, pricing, warning
    var isRead: Boolean = false
)
