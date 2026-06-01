package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Booking
import com.example.data.model.CarReview
import com.example.data.model.LoyaltyProfile
import com.example.data.model.UserProfile
import com.example.data.model.Vehicle
import com.example.ui.viewmodel.AppNotification
import com.example.ui.viewmodel.CarHireViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SrgCarHireMainScreen(viewModel: CarHireViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val loyalty by viewModel.loyaltyProfile.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val notifications by viewModel.notificationLog.collectAsStateWithLifecycle()
    val selectedVehicleId by viewModel.selectedVehicleId.collectAsStateWithLifecycle()
    val activeBookingReceipt by viewModel.activeBookingReceipt.collectAsStateWithLifecycle()
    val dynamicAlert by viewModel.dynamicPricingAlert.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdminAuthorized.collectAsStateWithLifecycle()

    var showNotificationShade by remember { mutableStateOf(false) }
    var showAdminPasscodeDialog by remember { mutableStateOf(false) }
    
    // Auto Show dynamic price alerts on screen as standard elegant floating HUD
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable {
                            // Secret gesture: Tapping title / logo triggers administrative passcode
                            showAdminPasscodeDialog = true
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SRG Car Hire",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.5.sp,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showAdminPasscodeDialog = true },
                        modifier = Modifier.testTag("admin_lock_button")
                    ) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Default.Key else Icons.Default.Lock,
                            contentDescription = "Hidden Edit Panel Access",
                            tint = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showNotificationShade = !showNotificationShade },
                            modifier = Modifier.testTag("notification_bell_button")
                        ) {
                            Icon(
                                imageVector = if (notifications.any { !it.isRead }) Icons.Filled.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Push Network Updates",
                                tint = if (notifications.any { !it.isRead }) Color(0xFFFF1744) else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        if (notifications.any { !it.isRead }) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-6).dp, y = (6).dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF1744))
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val menuItems = listOf(
                    Triple("explore", Icons.Default.AirportShuttle, "Explore"),
                    Triple("map", Icons.Default.Map, "GPS Radar"),
                    Triple("bookings", Icons.Default.Book, "Rentals"),
                    Triple("profile", Icons.Default.Person, "Profile"),
                    Triple("ai_support", Icons.Default.Chat, "AI Bot"),
                    Triple("loyalty", Icons.Default.Star, "Rewards")
                )
                
                menuItems.forEach { (tab, icon, label) ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_$tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main views routing
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "MainTabsNavigation"
            ) { targetTab ->
                when (targetTab) {
                    "explore" -> ExploreVehiclesView(viewModel, vehicles)
                    "map" -> GPSInteractiveMapView(viewModel, vehicles)
                    "bookings" -> ActiveBookingsView(viewModel, bookings)
                    "profile" -> UserProfileView(viewModel, vehicles, bookings)
                    "ai_support" -> AISupportConciergeView(viewModel)
                    "loyalty" -> LoyaltyRewardsView(viewModel, loyalty)
                    "admin" -> HiddenExecutivePanelView(viewModel, vehicles)
                }
            }

            // Global floating overlay warning for Dynamic pricing updates
            AnimatedVisibility(
                visible = dynamicAlert != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter)
            ) {
                dynamicAlert?.let { alertText ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF1744)),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = "Peak hours premium boost signal",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = alertText,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Slidover Push Notification Shade UI
            AnimatedVisibility(
                visible = showNotificationShade,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .background(MaterialTheme.colorScheme.surface)
                    .align(Alignment.TopCenter)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Server Notifications",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        IconButton(onClick = { viewModel.clearNotifications() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear logs",
                                tint = Color(0xFFFF5252)
                            )
                        }
                    }
                    Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 8.dp))
                    
                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsNone,
                                    contentDescription = null,
                                    tint = Color(0x4DFFFFFF),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No recent status alerts.",
                                    color = Color(0x4DFFFFFF),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notifications) { notification ->
                                val (color, icon) = when (notification.type) {
                                    "booking" -> Color(0xFFD0BCFF) to Icons.Default.AirportShuttle
                                    "verification" -> Color(0xFF00E676) to Icons.Default.Verified
                                    "payment" -> Color(0xFFFFD700) to Icons.Default.CreditCard
                                    "loyalty" -> Color(0xFFE040FB) to Icons.Default.Star
                                    "pricing" -> Color(0xFFFF1744) to Icons.Default.TrendingUp
                                    "warning" -> Color(0xFFFF9100) to Icons.Default.Warning
                                    else -> Color.LightGray to Icons.Default.Info
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = notification.title,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = notification.body,
                                                color = Color(0x99FFFFFF),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Button(
                        onClick = { showNotificationShade = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E38)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(text = "Close Shade", color = Color.White)
                    }
                }
            }

            // Gated Administrative panel trigger passcode Dialog
            if (showAdminPasscodeDialog) {
                var enteredPasscode by remember { mutableStateOf("") }
                var pinError by remember { mutableStateOf<String?>(null) }
                val keyb = LocalSoftwareKeyboardController.current

                Dialog(onDismissRequest = { showAdminPasscodeDialog = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C22)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(width = 1.dp, color = Color(0xFFFFC107), shape = RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Executive Admin Authentication",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enter Admin Passcode to enable fleet overrides & image config features.",
                                color = Color(0xB3FFFFFF),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = enteredPasscode,
                                onValueChange = {
                                    enteredPasscode = it
                                    pinError = null
                                },
                                placeholder = { Text("Passcode (e.g. SRGADMIN)", color = Color.Gray) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFC107),
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_passcode_field")
                            )

                            if (pinError != null) {
                                Text(
                                    text = pinError!!,
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showAdminPasscodeDialog = false },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                                ) {
                                    Text("Discard", color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        keyb?.hide()
                                        if (viewModel.authorizeAdmin(enteredPasscode)) {
                                            viewModel.selectTab("admin")
                                            showAdminPasscodeDialog = false
                                            Toast.makeText(context, "Executive Override Mode Unlocked!", Toast.LENGTH_LONG).show()
                                        } else {
                                            pinError = "Secure mismatch code. Denied."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("submit_passcode_button")
                                ) {
                                    Text("Authorize", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- SUB-VIEW: EXPLORE VEHICLES -----------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreVehiclesView(viewModel: CarHireViewModel, vehicles: List<Vehicle>) {
    val uProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val selectedCarReviews by viewModel.selectedCarReviews.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Supercar", "Electric Sedan", "Luxury Sports SUV", "Convertible Supercar", "Grand Sedan")
    
    // Bottom detail Sheet dialog when vehicle is clicked
    var selectedDetailVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var activeReserveHours by remember { mutableStateOf(4) }
    
    val filteredVehicles = if (selectedCategory == "All") {
        vehicles
    } else {
        vehicles.filter { it.category == selectedCategory || it.category.contains(selectedCategory) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Visual Hero banner on Explore
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF381E72), MaterialTheme.colorScheme.background)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "MEMBER PLATINUM PORTAL",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text(
                                "Executive Fleet Arrivals",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Live autonomous tracking & pricing active across Nairobi sectors.",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Quick Horizontal Filter Row
            item {
                Text(
                    text = "Filter Categories",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Empty Fleet state validation
            if (filteredVehicles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color(0x33FFFFFF),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Zero matches inside local sector.",
                                color = Color(0x4DFFFFFF)
                            )
                        }
                    }
                }
            }

            items(filteredVehicles, key = { it.id }) { vehicle ->
                val favList = uProfile?.favoriteVehicleIds?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
                val isFavorite = favList.contains(vehicle.id.toString())
                VehicleCardItem(
                    vehicle = vehicle,
                    isFavorite = isFavorite,
                    onToggleFavorite = { viewModel.toggleFavoriteCar(vehicle.id) },
                    onSelect = {
                        selectedDetailVehicle = vehicle
                        viewModel.selectVehicle(vehicle.id) // pull reviews
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Detailed overlay dialogue sheet for Selected Vehicle
        if (selectedDetailVehicle != null) {
            val vInstance = selectedDetailVehicle!!
            Dialog(onDismissRequest = {
                selectedDetailVehicle = null
                viewModel.selectVehicle(null)
            }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 24.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = vInstance.category.uppercase(),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = vInstance.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            IconButton(onClick = {
                                selectedDetailVehicle = null
                                viewModel.selectVehicle(null)
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        
                        // Fake visual car vector drawing/placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .padding(vertical = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(60.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "SRG Premium Fleet Signature",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Vehicle detailed specifications pill rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Icons.Default.FlashOn to vInstance.fuelType,
                                Icons.Default.EventSeat to "${vInstance.seats} Seats",
                                Icons.Default.Settings to vInstance.transmission
                            ).forEach { (icon, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = label, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = "Specifications & Hub Info",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = vInstance.description,
                            color = Color(0xB3FFFFFF),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PinDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Located at: ${vInstance.locationName}",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Real-time Reviews of model section
                        Text(
                            text = "User Reviews & Ratings (${String.format(Locale.US, "%.1f", vInstance.rating)} ⭐)",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (selectedCarReviews.isEmpty()) {
                            Text(
                                "No client reviews posted yet. Be the first to rent and rate this sport coupe!",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        } else {
                            selectedCarReviews.take(3).forEach { review ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(review.userName, color = MaterialTheme.colorScheme.onBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Row {
                                                repeat(5) { ind ->
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = if (ind < review.rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(review.comment, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Form submit review
                        var reviewText by remember { mutableStateOf("") }
                        var reviewRating by remember { mutableStateOf(5) }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text("Post Anonymized Review", color = MaterialTheme.colorScheme.onBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Your Rating: ", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                repeat(5) { i ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (i < reviewRating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { reviewRating = i + 1 }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                placeholder = { Text("How did this beast drive?", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    if (reviewText.isNotBlank()) {
                                        viewModel.submitCarReview(vInstance.id, reviewRating, reviewText)
                                        reviewText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("submit_review_button")
                            ) {
                                Text("Submit", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Hourly Duration selection
                        Text(
                            text = "Reserve Duration Hours",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "$activeReserveHours Hours",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Slider(
                                value = activeReserveHours.toFloat(),
                                onValueChange = { activeReserveHours = it.toInt() },
                                valueRange = 1f..48f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.weight(2f)
                             )
                        }

                        // Rent button calculating total dynamic price
                        val dynamicTotalAmount = activeReserveHours * vInstance.pricePerHour
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("DYNAMIC HOURLY TOTAL", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 10.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Ksh. ${String.format(Locale.US, "%,.2f", dynamicTotalAmount)}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (vInstance.pricePerHour < vInstance.originalPricePerHour) {
                                        Text(
                                            text = "-Ksh. ${String.format(Locale.US, "%,.0f", (vInstance.originalPricePerHour - vInstance.pricePerHour) * activeReserveHours)} OFF-PEAK",
                                            color = Color(0xFF00E676),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = {
                                    viewModel.initiateBooking(vInstance, activeReserveHours)
                                    selectedDetailVehicle = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                enabled = vInstance.status == "Available",
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("submit_booking_button")
                            ) {
                                Text(
                                    text = if (vInstance.status == "Available") "LOCK IN CAR" else "FLEET RENTED",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleCardItem(
    vehicle: Vehicle,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("car_card_${vehicle.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vehicle.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = vehicle.category,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onToggleFavorite() },
                        modifier = Modifier.testTag("fav_button_${vehicle.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite ${vehicle.title}",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Dynamic Price indicator tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (vehicle.status == "Available") Color(0x2200E676) else Color(0x22FF1744))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = vehicle.status.uppercase(),
                            color = if (vehicle.status == "Available") Color(0xFF00E676) else Color(0xFFFF5252),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Visual Specs Overview List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "${vehicle.rating}", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text("•", color = MaterialTheme.colorScheme.outline)
                Text(text = vehicle.transmission, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                Text("•", color = MaterialTheme.colorScheme.outline)
                Text(text = vehicle.fuelType, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Price Tag per hour
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Dynamic Pricing Indicator",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Dynamic Pricing Updates:",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ksh. ${String.format(Locale.US, "%,.2f", vehicle.pricePerHour)} / hr",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (vehicle.pricePerHour < vehicle.originalPricePerHour) {
                        Text(
                            text = "OFF-PEAK RATE",
                            color = Color(0xFF00E676),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ----------------- SUB-VIEW: INTERACTIVE MAP -----------------
@Composable
fun GPSInteractiveMapView(viewModel: CarHireViewModel, vehicles: List<Vehicle>) {
    var hoveredVehicleId by remember { mutableStateOf<Int?>(null) }
    val showDetailSheet = vehicles.firstOrNull { it.id == hoveredVehicleId }

    // Map Coordinates for mapping GPS locations (Nairobi Central Sector)
    val minLat = -1.3500
    val maxLat = -1.2000
    val minLng = 36.6500
    val maxLng = 36.9500

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw real stylized Nairobi central map layout Canvas!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Detect click on map icons
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        
                        var matchedCarId: Int? = null
                        vehicles.forEach { vehicle ->
                            val mercX = ((vehicle.gpsLng - minLng) / (maxLng - minLng)).toFloat() * width
                            val mercY = (1f - ((vehicle.gpsLat - minLat) / (maxLat - minLat)).toFloat()) * height
                            
                            val distance = Math.hypot((offset.x - mercX).toDouble(), (offset.y - mercY).toDouble())
                            if (distance < 45f) { // 45 pixels tap target
                                matchedCarId = vehicle.id
                            }
                        }
                        hoveredVehicleId = matchedCarId
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Obsidian grid background lines
                val cols = 8
                val rows = 12
                val cellW = w / cols
                val cellH = h / rows
                
                for (col in 0..cols) {
                    drawLine(
                        color = Color(0x0AFFFFFF),
                        start = Offset(col * cellW, 0f),
                        end = Offset(col * cellW, h),
                        strokeWidth = 1f
                    )
                }
                for (row in 0..rows) {
                    drawLine(
                        color = Color(0x0AFFFFFF),
                        start = Offset(0f, row * cellH),
                        end = Offset(w, row * cellH),
                        strokeWidth = 1f
                    )
                }

                // DRAW NAIROBI NATIONAL PARK (Green sector in the South/Lower map side)
                drawCircle(
                    color = Color(0xFF0F3D0F).copy(alpha = 0.5f),
                    radius = 250f,
                    center = Offset(w * 0.5f, h * 0.85f)
                )

                // DRAW KARURA FOREST (Green reserve in the North/Upper map side)
                drawCircle(
                    color = Color(0xFF0A2D1B).copy(alpha = 0.5f),
                    radius = 160f,
                    center = Offset(w * 0.40f, h * 0.15f)
                )

                // DRAW UHURU PARK / CENTRAL PARK (Green oasis near center)
                drawCircle(
                    color = Color(0xFF0D331A).copy(alpha = 0.6f),
                    radius = 75f,
                    center = Offset(w * 0.45f, h * 0.48f)
                )

                // MAIN NAIROBI ROADS / HIGHWAYS
                // Mombasa Road / A104 Superhighway (Crosses from bottom-right towards top-left)
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(w * 0.9f, h * 0.9f),
                    end = Offset(w * 0.1f, h * 0.3f),
                    strokeWidth = 8f
                )
                
                // Thika Road Highway (Branching towards north-east)
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(w * 0.45f, h * 0.48f),
                    end = Offset(w * 0.9f, h * 0.1f),
                    strokeWidth = 6f
                )

                // Ngong Road / Langata Road
                drawLine(
                    color = Color(0x1AFFFFFF),
                    start = Offset(0f, h * 0.7f),
                    end = Offset(w * 0.45f, h * 0.48f),
                    strokeWidth = 6f
                )

                // Draw each premium car's live blinking coordinate!
                vehicles.forEach { vehicle ->
                    val mercX = ((vehicle.gpsLng - minLng) / (maxLng - minLng)).toFloat() * w
                    val mercY = (1f - ((vehicle.gpsLat - minLat) / (maxLat - minLat)).toFloat()) * h

                    // Pulse outer halo
                    val isRented = vehicle.status == "Rented"
                    val outerPulseColor = if (isRented) Color(0xFFFF1744).copy(alpha = 0.3f) else Color(0xFFD0BCFF).copy(alpha = 0.3f)
                    val innerColor = if (isRented) Color(0xFFFF5252) else Color(0xFFD0BCFF)

                    drawCircle(
                        color = outerPulseColor,
                        radius = 28f,
                        center = Offset(mercX, mercY)
                    )
                    drawCircle(
                        color = innerColor,
                        radius = 12f,
                        center = Offset(mercX, mercY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = Offset(mercX, mercY)
                    )
                }
            }
        }

        // Top Map HUD Status overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "GPS Sensor Status online",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Kenya GPS Satellites: CONNECTED",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rented dynamic vehicle tracks updated in real-time.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Map Legends HUD on bottom left
        Box(
            modifier = Modifier
                .padding(bottom = 120.dp, start = 16.dp)
                .align(Alignment.BottomStart)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Available", color = MaterialTheme.colorScheme.onSurface, fontSize = 9.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF5252)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Ride (Live Moves)", color = MaterialTheme.colorScheme.onSurface, fontSize = 9.sp)
                    }
                }
            }
        }

        // Map Float Instructions on bottom right
        Box(
            modifier = Modifier
                .padding(bottom = 120.dp, end = 16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x40000000)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    "Tap Node to Book",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Clicked Map Vehicle overlay preview floating sheet
        AnimatedVisibility(
            visible = showDetailSheet != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            showDetailSheet?.let { vItem ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    vItem.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Dynamic Rate: Ksh. ${String.format(Locale.US, "%,.2f", vItem.pricePerHour)}/hr",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { hoveredVehicleId = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close HUD", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        
                        Text(
                            text = "Location: ${vItem.locationName} (GPS coords: ${String.format(Locale.US, "%.5f", vItem.gpsLat)}, ${String.format(Locale.US, "%.5f", vItem.gpsLng)})",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    viewModel.selectVehicle(vItem.id) // view full reviews/book on explore
                                    viewModel.selectTab("explore")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("map_select_detail_button")
                            ) {
                                Text("RATING & SPECS", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- SUB-VIEW: ACTIVE BOOKINGS & AGREEMENT / GATEWAY -----------------
@Composable
fun ActiveBookingsView(viewModel: CarHireViewModel, bookings: List<Booking>) {
    val activeReceipt by viewModel.activeBookingReceipt.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Multi stage booking variables
    var showSignaturePanel by remember { mutableStateOf(false) }
    var showSecurePaymentPanel by remember { mutableStateOf(false) }
    
    // Secure billing fields
    var licenseField by remember { mutableStateOf("") }
    var signField by remember { mutableStateOf("") }
    var cardField by remember { mutableStateOf("") }
    var expiryField by remember { mutableStateOf("") }
    var cvvField by remember { mutableStateOf("") }
    
    var selectedPaymentMethod by remember { mutableStateOf("Credit Card") }
    var mpesaPhoneNumber by remember { mutableStateOf("") }
    
    LaunchedEffect(userProfile) {
        userProfile?.let {
            if (licenseField.isBlank() && it.driverLicense.isNotBlank()) {
                licenseField = it.driverLicense
            }
            if (mpesaPhoneNumber.isBlank() && it.phoneNumber.isNotBlank()) {
                mpesaPhoneNumber = it.phoneNumber
            }
        }
    }
    
    var agreementAccepted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Your Electronic Rental File",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Track agreements, complete secure transactions, and unlock active engines via GPS.",
                color = Color(0x99FFFFFF),
                fontSize = 11.sp
            )
            Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 12.dp))
        }

        // Active pending checkout progress tracker
        if (activeReceipt != null) {
            val booking = activeReceipt!!
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("OUTSTANDING FILE RESERVATION", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                Text(booking.vehicleTitle, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = booking.status.uppercase(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Duration Chosen:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
                            Text("${booking.durationHours} Hours", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Rental Charge:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
                            Text("Ksh. ${String.format(Locale.US, "%,.2f", booking.totalSpent)} total", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // SUBUNIT: STEPS CHECK LIST
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (booking.isVerified) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (booking.isVerified) Color(0xFF00E676) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("1. Digital Agreement Signed & License Verified", color = if (booking.isVerified) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (booking.paymentStatus == "Paid") Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (booking.paymentStatus == "Paid") Color(0xFF00E676) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("2. Secure Central Stripe Payment Secured", color = if (booking.paymentStatus == "Paid") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Trigger Panels based on outstanding tasks
                        if (!booking.isVerified) {
                            Button(
                                onClick = { showSignaturePanel = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("trigger_signature_button")
                            ) {
                                Text("SIGN PRE-LEASE AGREEMENT", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else if (booking.paymentStatus == "Unpaid") {
                            Button(
                                onClick = { showSecurePaymentPanel = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("trigger_payment_button")
                            ) {
                                Text("PROCESS SECURED PAYMENT GATEWAY", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            // Confirmed ride controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.completeRide(booking.id, booking.vehicleId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("return_car_button")
                                ) {
                                    Text("RETURN VEHICLE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // SIGNATURE DISCOVERY PANEL
        if (showSignaturePanel) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SRG Automated Digital Verification", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Fake lease agreement legal boilerplate
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .verticalScroll(rememberScrollState())
                                .padding(10.dp)
                        ) {
                            Text(
                                "SRG LEGAL TERMS CONDITIONS (KENYA):\n" +
                                "1. Maximum vehicle speed limits: Driver strictly recognizes responsibility for speed cameras and highways in Nairobi Kenya.\n" +
                                "2. Nairobi Expressway and Tolls: Motorist is liable for all Expressway tolls (payable via M-Pesa or Cash) and parking fees incurred.\n" +
                                "3. GPS Tracking and Immobilizer: The vehicle contains automated satellite-linked GPS. The motor is unlocked dynamically upon payment approval and is monitored. Unauthorized cross-border driving of rental cars is forbidden.",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                lineHeight = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = agreementAccepted,
                                onCheckedChange = { agreementAccepted = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text("I agree to all SRG leasing terms.", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = licenseField,
                            onValueChange = { licenseField = it },
                            placeholder = { Text("Driving License Number (e.g. LN123456)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("license_field")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = signField,
                            onValueChange = { signField = it },
                            placeholder = { Text("Electronic Initials Signature", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signature_field")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showSignaturePanel = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Discard", color = MaterialTheme.colorScheme.onSurface)
                            }
                            Button(
                                onClick = {
                                    if (licenseField.isNotBlank() && signField.isNotBlank() && agreementAccepted) {
                                        activeReceipt?.let {
                                            viewModel.submitAgreementVerification(it.id, licenseField, signField)
                                        }
                                        android.widget.Toast.makeText(context, "License and signature saved to phone!", android.widget.Toast.LENGTH_SHORT).show()
                                        showSignaturePanel = false
                                        showSecurePaymentPanel = true // Auto proceed to keep checkout flow flawless!
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                enabled = licenseField.isNotBlank() && signField.isNotBlank() && agreementAccepted,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("confirm_signature_button")
                            ) {
                                Text("VERIFY NOW", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // SECURED PAYMENT PANEL
        if (showSecurePaymentPanel) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Secure Gateway Checkout", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SECURE CO.", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Segmented Tab Selectors
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val modes = listOf("Credit Card", "M-Pesa", "Cash")
                            modes.forEach { m ->
                                val active = selectedPaymentMethod == m
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { selectedPaymentMethod = m }
                                        .wrapContentSize(Alignment.Center)
                                        .testTag("pay_mode_$m")
                                ) {
                                    Text(
                                        text = m,
                                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Conditionals based on payment options
                        if (selectedPaymentMethod == "Credit Card") {
                            OutlinedTextField(
                                value = cardField,
                                onValueChange = { cardField = it },
                                placeholder = { Text("Card Number (16 Digits)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_number_field")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = expiryField,
                                    onValueChange = { expiryField = it },
                                    placeholder = { Text("MM/YY", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("card_expiry_field")
                                )
                                OutlinedTextField(
                                    value = cvvField,
                                    onValueChange = { cvvField = it },
                                    placeholder = { Text("CVV", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("card_cvv_field")
                                )
                            }
                        } else if (selectedPaymentMethod == "M-Pesa") {
                            Text(
                                "LIPA NA M-PESA INSTANT",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Enter your Safaricom mobile phone line below. A secure STK Push Prompt PIN request will pop up on your handset screen to approve Ksh payment instantly.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = mpesaPhoneNumber,
                                onValueChange = { mpesaPhoneNumber = it },
                                label = { Text("M-Pesa Mobile Number", fontSize = 11.sp) },
                                placeholder = { Text("E.g. 07XXXXXXXX", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mpesa_phone_field")
                            )
                        } else {
                            Text(
                                "PAY BY CASH HANDOVER",
                                color = Color(0xFFFFC107),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Pay cash directly to our fleet delivery driver on delivery coordinates. Confirm your coordinate phone contact below so our agent can call you to organize drop-off. Support Hotline: +254 712 345 678.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = mpesaPhoneNumber,
                                onValueChange = { mpesaPhoneNumber = it },
                                label = { Text("Contact Phone Line for Dispatch Coordinate", fontSize = 11.sp) },
                                placeholder = { Text("E.g. 07XXXXXXXX", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cash_phone_field")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        activeReceipt?.let {
                            Text(
                                "AMOUNT TO DEBIT: Ksh. ${String.format(Locale.US, "%,.2f", it.totalSpent)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showSecurePaymentPanel = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                            }
                            
                            val inputIsValid = when (selectedPaymentMethod) {
                                "Credit Card" -> cardField.length >= 12 && expiryField.isNotBlank() && cvvField.length >= 3
                                "M-Pesa" -> mpesaPhoneNumber.length >= 9
                                "Cash" -> mpesaPhoneNumber.length >= 9
                                else -> false
                            }
                            
                            Button(
                                onClick = {
                                    if (inputIsValid) {
                                        activeReceipt?.let {
                                            if (selectedPaymentMethod == "Credit Card") {
                                                viewModel.processSecurePayment(it.id, cardField, expiryField, cvvField)
                                            } else {
                                                viewModel.processAlternativePayment(it.id, selectedPaymentMethod, mpesaPhoneNumber)
                                            }
                                        }
                                        android.widget.Toast.makeText(
                                            context,
                                            if (selectedPaymentMethod == "M-Pesa") "Lipa Na M-Pesa STK push check sent to phone!" else "Secure premium credit card validated and paid to database!",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                        showSecurePaymentPanel = false
                                        cardField = ""
                                        expiryField = ""
                                        cvvField = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                enabled = inputIsValid,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("confirm_payment_button")
                            ) {
                                val buttonLabel = when (selectedPaymentMethod) {
                                    "Credit Card" -> "DEBIT APPROVED"
                                    "M-Pesa" -> "LIPA NA MPESA"
                                    "Cash" -> "CONFIRM CASH"
                                    else -> "DEBIT APPROVED"
                                }
                                Text(buttonLabel, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Historial reservations logging list
        item {
            Text(
                text = "Reservation Ledger History",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        val pastList = bookings.filter { it.status == "Completed" || it.status == "Cancelled" || it.status == "Active" }
        if (pastList.isEmpty() && activeReceipt == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = Color(0x33FFFFFF),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You do not own active rides right now.",
                            color = Color(0x66FFFFFF),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(pastList) { historical ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                    border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(historical.vehicleTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            val fDate = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(historical.bookedAt))
                            Text("Rented on $fDate", color = Color.Gray, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Ksh. ${String.format(Locale.US, "%,.2f", historical.totalSpent)}", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (historical.status == "Active") Color(0x2200E676) else Color(0x11FFFFFF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    historical.status.uppercase(),
                                    color = if (historical.status == "Active") Color(0xFF00E676) else Color.Gray,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- SUB-VIEW: AI CHAT SUPPORT WIDGET -----------------
@Composable
fun AISupportConciergeView(viewModel: CarHireViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isTyping by viewModel.isAiTyping.collectAsStateWithLifecycle()
    var inputQuery by remember { mutableStateOf("") }
    
    val listState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    val suggestions = listOf(
        "Show me sports cars",
        "How is dynamic price computed?",
        "Where is the Porsche GT3 RS?",
        "Tell me about the loyalty program"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SRG Bot AI Representative",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Automated support trained exclusively on SRG car hire systems.",
                    color = Color(0x80FFFFFF),
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear Chat", tint = Color.LightGray)
            }
        }
        
        Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 12.dp))

        // Chat Bubble Area scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(listState)
        ) {
            messages.forEach { (text, isUser) ->
                ChatBubble(text, isUser)
                Spacer(modifier = Modifier.height(10.dp))
            }
            
            if (isTyping) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SRG Bot is writing guides...", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            // Auto scroll down during convo shifts
            LaunchedEffect(messages.size) {
                scope.launch {
                    listState.animateScrollTo(listState.maxValue)
                }
            }
        }

        // Suggestions Horizontal Slider
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(suggestions) { keyword ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.sendSupportPrompt(keyword) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(keyword, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp)
                }
            }
        }

        // Search Prompt Input box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Ask about fleet, GPS coordinates, agreements...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_field")
            )
            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        viewModel.sendSupportPrompt(inputQuery)
                        inputQuery = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("submit_ai_button")
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Submit support query", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    val align = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColors = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                color = textColors,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// ----------------- SUB-VIEW: ENHANCED USER PROFILE -----------------
@Composable
fun UserProfileView(viewModel: CarHireViewModel, allVehicles: List<Vehicle>, allBookings: List<Booking>) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Editable state fields
    var fullNameVal by remember { mutableStateOf("Jeff J. Mwangi") }
    var licenseVal by remember { mutableStateOf("DL-99482X-NBO") }
    var preferredCatVal by remember { mutableStateOf("All") }
    var phoneVal by remember { mutableStateOf("0712345678") }
    var isLicenseMasked by remember { mutableStateOf(true) }
    var hasInitialized by remember { mutableStateOf(false) }
    
    // Sync UI states when database answers
    LaunchedEffect(profileState) {
        if (!hasInitialized && profileState != null) {
            val it = profileState!!
            fullNameVal = it.fullName.ifBlank { "Jeff J. Mwangi" }
            licenseVal = it.driverLicense.ifBlank { "DL-99482X-NBO" }
            preferredCatVal = it.preferredVehicleCategory.ifBlank { "All" }
            phoneVal = it.phoneNumber.ifBlank { "0712345678" }
            hasInitialized = true
        }
    }
    
    val categoryOptions = listOf("All", "Supercar", "Electric Sedan", "Luxury Sports SUV", "Convertible Supercar")
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // PROFILE SECTION HEADER CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (fullNameVal.isNotBlank()) fullNameVal.take(2).uppercase() else "JM",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = fullNameVal,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "jeffjmwangi@gmail.com",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SRG PLATINUM EXCLUSIVE",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
        
        // SECURE VAULT INFORMATION BLOCK
        item {
            Text(
                text = "Secure Member Vault Credentials",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Full Name Input
                    OutlinedTextField(
                        value = fullNameVal,
                        onValueChange = { fullNameVal = it },
                        label = { Text("Enter Legal Full Name", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_field")
                    )
                    
                    // Secure Masked Driving License Input
                    OutlinedTextField(
                        value = licenseVal,
                        onValueChange = { licenseVal = it },
                        label = { Text("Driver's License (Masked File)", fontSize = 11.sp) },
                        visualTransformation = if (isLicenseMasked) PasswordVisualTransformation() else VisualTransformation.None,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        trailingIcon = {
                            IconButton(onClick = { isLicenseMasked = !isLicenseMasked }) {
                                Icon(
                                    imageVector = if (isLicenseMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle license visibility",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_license_field")
                    )
                    
                    // Contact phone number for Lipa Na M-Pesa automatedSTK push logic
                    OutlinedTextField(
                        value = phoneVal,
                        onValueChange = { phoneVal = it },
                        label = { Text("Phone Contact Number (Direct M-Pesa Line)", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_phone_field")
                    )
                    
                    // Preferred Vehicle Category Filter Dropdown selectors
                    Text(
                        text = "Preferred Vehicle Class",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryOptions.forEach { opt ->
                            val active = preferredCatVal == opt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                                    .border(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .clickable { preferredCatVal = opt }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = opt,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Button(
                        onClick = {
                            viewModel.updateUserProfile(fullNameVal, licenseVal, preferredCatVal, phoneVal)
                            android.widget.Toast.makeText(context, "Saved securely to phone storage database!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("profile_save_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE SECURELY TO VAULT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // VEHICLE FAVORITES HORIZONTAL DOCK
        item {
            Text(
                text = "Your Luxury Favorites Fleet",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        val favStrIds = profileState?.favoriteVehicleIds?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        val favCars = allVehicles.filter { favStrIds.contains(it.id.toString()) }
        
        if (favCars.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No luxury vehicles in favorites yet. Tap the heart icon in the Explore panel to instantly store vehicles in this VIP bay.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(favCars) { car ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable {
                        viewModel.selectVehicle(car.id)
                        viewModel.selectTab("explore")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(car.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(car.category, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Ksh. ${String.format(Locale.US, "%,.0f", car.pricePerHour)} / hr",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                car.status,
                                color = if (car.status == "Available") Color(0xFF00E676) else Color(0xFFFF5252),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.toggleFavoriteCar(car.id) }) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Unfavorite",
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // COMPREHENSIVE HISTORIC RENTAL LOGS
        item {
            Text(
                text = "Your Rent Ledger Vault History",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        if (allBookings.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You have no active or completed rentals in progress.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(allBookings) { rItem ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(rItem.vehicleTitle, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                val bookDate = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US).format(Date(rItem.bookedAt))
                                Text("Reserved at: $bookDate", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 10.sp)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    rItem.status,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Duration Span", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 10.sp)
                                Text("${rItem.durationHours} Hours", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Amount Paid", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 10.sp)
                                Text("Ksh. ${String.format(Locale.US, "%,.0f", rItem.totalSpent)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ----------------- SUB-VIEW: LOYALTY REWARDS DOCK -----------------
@Composable
fun LoyaltyRewardsView(viewModel: CarHireViewModel, loyalty: LoyaltyProfile?) {
    val score = loyalty?.pointsBalance ?: 120
    val tierStr = loyalty?.tier ?: "Silver"
    val completedCount = loyalty?.totalBookings ?: 0
    val totalSpent = loyalty?.totalSpent ?: 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Loyalty Club Portal",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Secure dynamic credits and hourly discounts for every ride.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
        }

        // Digital Rewards Membership card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = when (tierStr) {
                                "Platinum" -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                                "Gold" -> listOf(Color(0xFFFFB300), Color(0xFFFFE082))
                                else -> listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondary)
                            }
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SRG CLUB MEMBER",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tierStr.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column {
                        Text(
                            text = loyalty?.email ?: "jeffjmwangi@gmail.com",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$score PTS",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }

        // Progress towards next loyalty levels
        item {
            val progress = when {
                score >= 1000 -> 1.0f
                score >= 400 -> (score - 400).toFloat() / 600f
                else -> score.toFloat() / 400f
            }
            val nextTier = when {
                score >= 1000 -> "MAX PLATINUM TIER ACHIEVED!"
                score >= 400 -> "Next Tier: Platinum at 1000 PTS"
                else -> "Next Tier: Gold at 400 PTS"
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = nextTier, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        // Perks overview cards
        item {
            Text("Elite Tier Direct Privileges", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        val perks = listOf(
            Triple("Silver Level (Starter)", "120 Welcome points. Secure central checkouts.", true),
            Triple("Gold Level (400 PTS)", "Get instant 10% dynamic price checkout discount on all cars.", score >= 400),
            Triple("Platinum Level (1000 PTS)", "Get instant 20% dynamic price discount + prioritize active hub delivery.", score >= 1000)
        )

        items(perks) { (title, desc, active) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (active) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Active status",
                        tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(desc, color = if (active) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), fontSize = 11.sp)
                    }
                }
            }
        }
        
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ledger Bookings Recorded:", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                        Text("$completedCount rides", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gross Funds Settled:", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                        Text("Ksh. ${String.format(Locale.US, "%,.2f", totalSpent)}", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------- SUB-VIEW: ADMINISTRATIVE HIDDEN PANEL -----------------
@Composable
fun HiddenExecutivePanelView(viewModel: CarHireViewModel, vehicles: List<Vehicle>) {
    val adminLog by viewModel.adminLog.collectAsStateWithLifecycle()
    
    // Create new vehicle fields
    var titleF by remember { mutableStateOf("") }
    var categoryF by remember { mutableStateOf("Supercar") }
    var priceF by remember { mutableStateOf("") }
    var fuelF by remember { mutableStateOf("Electric") }
    var seatsF by remember { mutableStateOf("5") }
    var transF by remember { mutableStateOf("Automatic") }
    var descF by remember { mutableStateOf("") }
    var locF by remember { mutableStateOf("London West") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Administrative Override Control",
                        color = Color(0xFFFFC107),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Modify vehicle assets, bypass pricing indexes, or expand fleet inventory.",
                        color = Color(0x80FFFFFF),
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = { viewModel.deauthorizeAdmin() }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Lock panel", tint = Color.Red)
                }
            }
            Divider(color = Color(0x33FFFFFF), modifier = Modifier.padding(vertical = 12.dp))
        }

        if (adminLog != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C12)),
                    border = BorderStroke(1.dp, Color(0xFFFFC107))
                ) {
                    Text(
                        text = adminLog!!,
                        color = Color(0xFFFFC107),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // PART 1: OVERRIDE PRICES OF VEHICLES
        item {
            Text("Adjust Current Live Hourly Prices", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        items(vehicles) { vehicle ->
            var customPriceField by remember { mutableStateOf(vehicle.pricePerHour.toString()) }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(vehicle.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(vehicle.category, color = Color.Gray, fontSize = 11.sp)
                    }
                    
                    OutlinedTextField(
                        value = customPriceField,
                        onValueChange = { customPriceField = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFC107)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.8f)
                            .height(48.dp)
                            .testTag("admin_price_tf_${vehicle.id}")
                    )

                    Button(
                        onClick = {
                            val dPrice = customPriceField.toDoubleOrNull()
                            if (dPrice != null) {
                                viewModel.adminUpdateVehiclePrice(vehicle.id, dPrice)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        modifier = Modifier
                            .weight(0.7f)
                            .height(40.dp)
                            .testTag("admin_price_save_${vehicle.id}")
                    ) {
                        Text("SAVE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // PART 2: EXPAND FLEET / ADD NEW CARS
        item {
            Text("Add New Luxury Fleet Vehicle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleF,
                        onValueChange = { titleF = it },
                        placeholder = { Text("Vehicle Name (e.g., Ferrari SF90)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_add_title")
                    )

                    OutlinedTextField(
                        value = categoryF,
                        onValueChange = { categoryF = it },
                        placeholder = { Text("Category (e.g. Supercar, EV, SUV)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_add_category")
                    )

                    OutlinedTextField(
                        value = priceF,
                        onValueChange = { priceF = it },
                        placeholder = { Text("Price Per Hour (e.g. 195.00)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_add_price")
                    )

                    OutlinedTextField(
                        value = fuelF,
                        onValueChange = { fuelF = it },
                        placeholder = { Text("Fuel (Petrol / Electric)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = seatsF,
                            onValueChange = { seatsF = it },
                            placeholder = { Text("Seats (e.g. 2)", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = transF,
                            onValueChange = { transF = it },
                            placeholder = { Text("Transmission", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            modifier = Modifier.weight(1.5f)
                        )
                    }

                    OutlinedTextField(
                        value = descF,
                        onValueChange = { descF = it },
                        placeholder = { Text("Detailed description (aerodynamics, horsepower...)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = locF,
                        onValueChange = { locF = it },
                        placeholder = { Text("London Hub Location (e.g. Kensington Central)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val priceVal = priceF.toDoubleOrNull() ?: 100.0
                            val seatsVal = seatsF.toIntOrNull() ?: 5
                            if (titleF.isNotBlank()) {
                                viewModel.adminAddVehicle(
                                    title = titleF,
                                    category = categoryF,
                                    price = priceVal,
                                    fuelType = fuelF,
                                    seats = seatsVal,
                                    trans = transF,
                                    description = descF,
                                    location = locF
                                )
                                // Blank fields
                                titleF = ""
                                priceF = ""
                                descF = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_submit_add_car")
                    ) {
                        Text("PROVISION TO SERVER FLEET", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
