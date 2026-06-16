package com.example.ui.screens

import android.widget.Toast
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import com.example.data.model.Booking
import com.example.data.model.CarReview
import com.example.data.model.LoyaltyProfile
import com.example.data.model.UserProfile
import com.example.data.model.Vehicle
import com.example.data.model.UpcomingEvent
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
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val showBookingConfirmationModal by viewModel.showBookingConfirmationModal.collectAsStateWithLifecycle()

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
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Theme Shift",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
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
                    Triple("profile", Icons.Default.Person, "Profile"),
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
            // Permanent premium luxury sports car background with beautiful smooth fade-in
            var imageLoaded by remember { mutableStateOf(false) }
            val animatedAlpha by animateFloatAsState(
                targetValue = if (imageLoaded) 0.18f else 0f,
                animationSpec = tween(durationMillis = 1500, easing = EaseInOutCubic),
                label = "PermanentCarBackgroundFade"
            )

            coil.compose.AsyncImage(
                model = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1200&q=80",
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                onState = { state ->
                    if (state is coil.compose.AsyncImagePainter.State.Success) {
                        imageLoaded = true
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = animatedAlpha)
            )

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
                    "profile" -> UserProfileView(viewModel, vehicles, bookings)
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
                                placeholder = { Text("Passcode", color = Color.Gray) },
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

    if (showBookingConfirmationModal != null) {
        BookingConfirmationModal(
            booking = showBookingConfirmationModal!!,
            userProfile = userProfile,
            onDismiss = { viewModel.dismissBookingConfirmation() }
        )
    }
}

// ----------------- SUB-VIEW: EXPLORE VEHICLES -----------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreVehiclesView(viewModel: CarHireViewModel, vehicles: List<Vehicle>) {
    val uProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val selectedCarReviews by viewModel.selectedCarReviews.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("All") }
    var transparentCarBackground by remember { mutableStateOf(false) }
    val categories = listOf("All", "Small cars", "Saloon cars", "High end cars", "Seven seaters", "aircraft")
    
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

            // UPCOMING EVENTS ADVERTISING CAROUSEL (CLIENT PORTAL)
            item {
                val upcomingEvents by viewModel.upcomingEvents.collectAsStateWithLifecycle()
                var selectedEventDetail by remember { mutableStateOf<UpcomingEvent?>(null) }
                
                if (upcomingEvents.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = "Upcoming Events",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Exclusive Upcoming Events",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${upcomingEvents.size} Advertised",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(15.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upcoming_events_carousel")
                        ) {
                            items(upcomingEvents) { event ->
                                Card(
                                    modifier = Modifier
                                        .width(280.dp)
                                        .height(170.dp)
                                        .clickable { selectedEventDetail = event }
                                        .testTag("event_card_${event.id}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        androidx.compose.foundation.Image(
                                            painter = coil.compose.rememberAsyncImagePainter(
                                                model = event.imageUrl.ifBlank { "https://images.unsplash.com/photo-1544829099-b9a0c07fad1a?auto=format&fit=crop&w=800&q=80" }
                                            ),
                                            contentDescription = event.title,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.85f)
                                                        )
                                                    )
                                                )
                                        )
                                        
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(10.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFFC107))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "VIP ACCESS",
                                                color = Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Event,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFC107),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Text(
                                                    text = event.dateText,
                                                    color = Color(0xFFEEEEEE),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(2.dp))
                                            
                                            Text(
                                                text = event.title,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            
                                            Spacer(modifier = Modifier.height(2.dp))
                                            
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = Color.LightGray,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Text(
                                                    text = event.location,
                                                    color = Color.LightGray,
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                selectedEventDetail?.let { event ->
                    Dialog(onDismissRequest = { selectedEventDetail = null }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .testTag("event_detail_dialog_${event.id}"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = coil.compose.rememberAsyncImagePainter(
                                            model = event.imageUrl.ifBlank { "https://images.unsplash.com/photo-1544829099-b9a0c07fad1a?auto=format&fit=crop&w=800&q=80" }
                                        ),
                                        contentDescription = event.title,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                                )
                                            )
                                    )
                                    Text(
                                        text = event.title,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    )
                                }
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Text(event.dateText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Text(event.location, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                    
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    
                                    Text(
                                        text = event.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { selectedEventDetail = null },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Close", fontSize = 12.sp)
                                        }
                                        Button(
                                            onClick = {
                                                selectedEventDetail = null
                                                viewModel.addNotification(
                                                    "VIP Ticket Reserved",
                                                    "Your automatic VIP Access Pass for '${event.title}' is registered on your current member profile.",
                                                    "info"
                                                )
                                                Toast.makeText(context, "VIP Pass Registered Successfully!", Toast.LENGTH_LONG).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.weight(1f).testTag("event_reserve_ticket_button")
                                        ) {
                                            Text("Claim Pass", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            // CAR BOOKING FORM COMPONENT WITH FIELDS FOR VEHICLE SELECTION, PICKUP DATE & RETURN DATE
            item {
                var bookingFormExpanded by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("car_booking_form_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { bookingFormExpanded = !bookingFormExpanded }
                                .testTag("toggle_booking_form"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Booking Planner",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Plan Your Live Rental Journey",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Custom date ranges & custom vehicle booking",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (bookingFormExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle booking form",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        AnimatedVisibility(visible = bookingFormExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                
                                // 1. VEHICLE SELECTION DROPDOWN
                                Text(
                                    text = "Select Premium Vehicle",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                var selectedVehicleIndex by remember { mutableStateOf(0) }
                                val availableVehicles = vehicles.filter { it.status == "Available" }
                                
                                if (availableVehicles.isEmpty()) {
                                    Text("No vehicles currently available for booking.", fontSize = 11.sp, color = Color.Red)
                                } else {
                                    var dropdownExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(
                                            onClick = { dropdownExpanded = true },
                                            modifier = Modifier.fillMaxWidth().testTag("booking_vehicle_dropdown_button"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            val currentVeh = availableVehicles.getOrNull(selectedVehicleIndex)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = currentVeh?.let { "${it.title} (${it.category}) - Ksh. ${it.pricePerHour}/hr" } ?: "Select a car...",
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        
                                        DropdownMenu(
                                            expanded = dropdownExpanded,
                                            onDismissRequest = { dropdownExpanded = false },
                                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            availableVehicles.forEachIndexed { idx, veh ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "${veh.title} (${veh.category}) - Ksh. ${veh.pricePerHour}/hr",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedVehicleIndex = idx
                                                        dropdownExpanded = false
                                                    },
                                                    modifier = Modifier.testTag("booking_vehicle_option_${veh.id}")
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // 2. DATE FIELDS (PICKUP & RETURN DATE)
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                                val currentCal = Calendar.getInstance()
                                
                                var pickupDateText by remember { 
                                    mutableStateOf(sdf.format(currentCal.time)) 
                                }
                                
                                val returnCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                                var returnDateText by remember { 
                                    mutableStateOf(sdf.format(returnCal.time)) 
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Pickup Date & Time",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = pickupDateText,
                                            onValueChange = { pickupDateText = it },
                                            placeholder = { Text("YYYY-MM-DD HH:MM", fontSize = 10.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("booking_pickup_date_field")
                                        )
                                    }
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Return Date & Time",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = returnDateText,
                                            onValueChange = { returnDateText = it },
                                            placeholder = { Text("YYYY-MM-DD HH:MM", fontSize = 10.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("booking_return_date_field")
                                        )
                                    }
                                }
                                
                                // Helper Preset Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AssistChip(
                                        onClick = {
                                            val claimCal = Calendar.getInstance()
                                            pickupDateText = sdf.format(claimCal.time)
                                            claimCal.add(Calendar.HOUR, 4)
                                            returnDateText = sdf.format(claimCal.time)
                                        },
                                        label = { Text("Rent 4 Hours", fontSize = 10.sp) },
                                        modifier = Modifier.testTag("preset_rent_4h")
                                    )
                                    AssistChip(
                                        onClick = {
                                            val claimCal = Calendar.getInstance()
                                            pickupDateText = sdf.format(claimCal.time)
                                            claimCal.add(Calendar.DAY_OF_YEAR, 1)
                                            returnDateText = sdf.format(claimCal.time)
                                        },
                                        label = { Text("Rent 1 Day", fontSize = 10.sp) },
                                        modifier = Modifier.testTag("preset_rent_1d")
                                    )
                                    AssistChip(
                                        onClick = {
                                            val claimCal = Calendar.getInstance()
                                            pickupDateText = sdf.format(claimCal.time)
                                            claimCal.add(Calendar.DAY_OF_YEAR, 3)
                                            returnDateText = sdf.format(claimCal.time)
                                        },
                                        label = { Text("Rent 3 Days", fontSize = 10.sp) },
                                        modifier = Modifier.testTag("preset_rent_3d")
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // 3. COMPUTATION PREVIEW
                                var calculatedHours by remember { mutableStateOf(24) }
                                var calculatedTotalCost by remember { mutableStateOf(0.0) }
                                var errorParseText by remember { mutableStateOf<String?>(null) }
                                
                                val selectedVeh = availableVehicles.getOrNull(selectedVehicleIndex)
                                
                                LaunchedEffect(pickupDateText, returnDateText, selectedVehicleIndex, vehicles) {
                                    try {
                                        val pDate = sdf.parse(pickupDateText.trim())
                                        val rDate = sdf.parse(returnDateText.trim())
                                        if (pDate != null && rDate != null) {
                                            val diff = rDate.time - pDate.time
                                            if (diff <= 0) {
                                                errorParseText = "Return must be after pickup time."
                                                calculatedHours = 0
                                                calculatedTotalCost = 0.0
                                            } else {
                                                errorParseText = null
                                                val hours = Math.max(1, (diff / (1000 * 60 * 60)).toInt())
                                                calculatedHours = hours
                                                if (selectedVeh != null) {
                                                    calculatedTotalCost = selectedVeh.pricePerHour * hours
                                                }
                                            }
                                        } else {
                                            errorParseText = "Incorrect Date Format."
                                        }
                                    } catch (e: Exception) {
                                        errorParseText = "Use format: YYYY-MM-DD HH:MM (e.g. 2026-06-15 12:00)"
                                    }
                                }
                                
                                if (errorParseText != null) {
                                    Text(text = errorParseText!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.testTag("booking_parse_error"))
                                } else if (selectedVeh != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("RENTAL COMPUTATION SUMMARY", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text("Est. Duration: $calculatedHours Hours", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("Rate: Ksh. ${selectedVeh.pricePerHour}/hr", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                            }
                                            Text(
                                                text = "Ksh. ${String.format(Locale.US, "%,.2f", calculatedTotalCost)}",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.testTag("booking_calculated_price")
                                            )
                                        }
                                    }
                                }
                                
                                // Booking submit action
                                Button(
                                    onClick = {
                                        try {
                                            val pDate = sdf.parse(pickupDateText.trim())
                                            val rDate = sdf.parse(returnDateText.trim())
                                            if (pDate != null && rDate != null && selectedVeh != null) {
                                                viewModel.initiateFlexibleBooking(selectedVeh, pDate.time, rDate.time)
                                                bookingFormExpanded = false
                                                Toast.makeText(context, "Draft Rental Booked! Directing to Pre-Lease Signatures.", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Invalid input. Please correct dates.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = errorParseText == null && selectedVeh != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("booking_initiate_button")
                                ) {
                                    Text("INITIATE DRAFT RENTAL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            // Quick Horizontal Filter Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Categories",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                            .clickable { transparentCarBackground = !transparentCarBackground }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("transparent_bg_toggle")
                    ) {
                        Checkbox(
                            checked = transparentCarBackground,
                            onCheckedChange = { transparentCarBackground = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Transparent BG",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
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
                    },
                    transparentBackground = transparentCarBackground
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
                var isEntered by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    isEntered = true
                }
                val scale by animateFloatAsState(
                    targetValue = if (isEntered) 1f else 0.92f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "DialogScale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isEntered) 1f else 0f,
                    animationSpec = tween(durationMillis = 350, easing = EaseInOutCubic),
                    label = "DialogAlpha"
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 24.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            alpha = alpha
                        )
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
                        
                        // Interactive Multi-Image Gallery
                        val allImagesList = remember(vInstance) {
                            val list = mutableListOf<String>()
                            if (vInstance.photoUrl.isNotBlank()) {
                                list.add(vInstance.photoUrl)
                            }
                            if (vInstance.additionalPhotos.isNotBlank()) {
                                vInstance.additionalPhotos.split(",").map { it.trim() }.forEach {
                                    if (it.isNotBlank()) list.add(it)
                                }
                            }
                            list
                        }
                        var activeImageIndex by remember(vInstance) { mutableStateOf(0) }
                        val activeImage = if (allImagesList.isNotEmpty() && activeImageIndex < allImagesList.size) {
                            allImagesList[activeImageIndex]
                        } else {
                            if (vInstance.photoUrl.isNotBlank()) vInstance.photoUrl else "placeholder"
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                coil.compose.AsyncImage(
                                    model = activeImage,
                                    contentDescription = vInstance.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery),
                                    error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery)
                                )
                            }

                            if (allImagesList.size > 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(allImagesList.size) { idx ->
                                        val isSelected = idx == activeImageIndex
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                .border(
                                                    2.dp,
                                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { activeImageIndex = idx }
                                        ) {
                                            coil.compose.AsyncImage(
                                                model = allImagesList[idx],
                                                contentDescription = "Thumbnail $idx",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery),
                                                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery)
                                            )
                                        }
                                    }
                                }
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
    onSelect: () -> Unit,
    transparentBackground: Boolean = false
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(vehicle.id) {
        isVisible = true
    }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = EaseInOutCubic),
        label = "VehicleCardItemFade"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = animatedAlpha)
            .clickable { onSelect() }
            .testTag("car_card_${vehicle.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (transparentBackground) Color.Transparent else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (transparentBackground) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (transparentBackground) 0.dp else 2.dp)
    ) {
        Column {
            coil.compose.AsyncImage(
                model = vehicle.photoUrl,
                contentDescription = vehicle.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery),
                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery)
            )
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
             // Clicked Map Vehicle overlay preview floating sheet
        AnimatedVisibility(
            visible = showDetailSheet != null,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(600, easing = EaseInOutCubic)),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
            ) + fadeOut(animationSpec = tween(400, easing = EaseInOutCubic)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            showDetailSheet?.let { vItem ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Beautiful vehicle active thumbnail on the map pop up
                            Box(
                                modifier = Modifier
                                    .size(width = 110.dp, height = 75.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.2f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                coil.compose.AsyncImage(
                                    model = vItem.photoUrl,
                                    contentDescription = vItem.title,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = vItem.title,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // Status Badge on Map popup
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (vItem.status == "Available") Color(0xFF00E676).copy(alpha = 0.15f)
                                                else Color(0xFFFF5252).copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = vItem.status.uppercase(),
                                            color = if (vItem.status == "Available") Color(0xFF00E676) else Color(0xFFFF5252),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Text(
                                    text = vItem.category,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                                    Text("${vItem.rating}", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("•", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
                                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                    Text(vItem.locationName, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            // Close Button for the pop up card overlay
                            IconButton(
                                onClick = { hoveredVehicleId = null },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Popup", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Horizontally aligned mini-specs grid row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                                Text(vItem.transmission, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                                Text("${vItem.seats} Seats", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                                Text(vItem.fuelType, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TOTAL PREPAID RATE", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Ksh. ${String.format(Locale.US, "%,.2f", vItem.pricePerHour)}/hr",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.selectVehicle(vItem.id) // Select dynamically
                                    viewModel.selectTab("explore")     // Go to booking panel
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("map_select_detail_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(12.dp))
                                    Text("BOOK NOW", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }     }
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

                        Spacer(modifier = Modifier.height(10.dp))

                        SignaturePad(
                            modifier = Modifier.padding(vertical = 4.dp),
                            onSignatureDrawn = { points ->
                                if (points.size > 5) {
                                    signField = "Signed via Signature TouchPad"
                                }
                            },
                            onClear = {
                                signField = ""
                            }
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
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isTyping by viewModel.isAiTyping.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var inputQuery by remember { mutableStateOf("") }
    
    val listState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    val suggestions = listOf(
        "Show me sports cars",
        "How is dynamic price computed?",
        "Where is the Porsche GT3 RS?",
        "Tell me about the loyalty program",
        "Explain digital agreements"
    )

    // Check if real API Key is injected
    val hasRealApiKey = com.example.BuildConfig.GEMINI_API_KEY.isNotBlank() && 
                        com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" &&
                        com.example.BuildConfig.GEMINI_API_KEY != "null"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Concierge Header Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Bot Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SRG Bot AI Concierge",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Real-time rental expert representative",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
            
            OutlinedButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.height(36.dp).testTag("clear_chat_button"),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 12.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Chat", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(4.dp))
                Text("RESET", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // DYNAMIC CONNECTION STATUS INDICATION (Replaces hardcoded warning banner)
        val statusBg = if (hasRealApiKey) Color(0x154CAF50) else Color(0x15FF9800)
        val statusBorder = if (hasRealApiKey) Color(0x404CAF50) else Color(0x40FF9800)
        val statusText = if (hasRealApiKey) "GEMINI ENGINE: ACTIVE" else "LOCAL OFFLINE ASSISTANT: BACKUP ACTIVE"
        val statusDesc = if (hasRealApiKey) 
            "Connected securely to Google central servers via gemini-3.5-flash." 
            else "Using local backup intelligence because the central API key is offline. High-speed predictions are active!"
        val statusIcon = if (hasRealApiKey) Icons.Default.CloudQueue else Icons.Default.OfflineBolt
        val statusColor = if (hasRealApiKey) Color(0xFF4CAF50) else Color(0xFFFFB74D)

        Card(
            colors = CardDefaults.cardColors(containerColor = statusBg),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, statusBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = "Status icon",
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = statusDesc,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Chat Bubble Scrollable Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(listState)
        ) {
            messages.forEach { (text, isUser) ->
                ChatBubble(text = text, isUser = isUser)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (isTyping) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "SRG Assistant is formulating expert advice...",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Auto scroll down during convo shifts
            LaunchedEffect(messages.size) {
                scope.launch {
                    listState.animateScrollTo(listState.maxValue)
                }
            }
        }

        // Suggestions Slider
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            items(suggestions) { keyword ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .clickable { viewModel.sendSupportPrompt(keyword) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = keyword,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Search Prompt Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { 
                    Text(
                        "Ask about fleets, prices, locations, or agreements...", 
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
                        fontSize = 11.sp
                    ) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("submit_ai_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Submit support query",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    val align = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColors = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val bubbleBorder = if (isUser) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Bot",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = bubbleBorder,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = text,
                        color = textColors,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
            
            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

// ----------------- SUB-VIEW: ENHANCED USER PROFILE -----------------
@Composable
fun UserProfileView(viewModel: CarHireViewModel, allVehicles: List<Vehicle>, allBookings: List<Booking>) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeReceipt by viewModel.activeBookingReceipt.collectAsStateWithLifecycle()
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
    var agreementAccepted by remember { mutableStateOf(false) }

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
            
            // Set fields for checkout flow
            licenseField = it.driverLicense
            mpesaPhoneNumber = it.phoneNumber
            
            hasInitialized = true
        }
    }
    
    val categoryOptions = listOf("All", "Small cars", "Saloon cars", "High end cars", "Seven seaters", "aircraft")
    
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
                        Text(
                            text = "www.srgcarhire.co.ke",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.srgcarhire.co.ke"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                }
                            }
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
        
        // ACTIVE OUTSTANDING RENTAL ENGINE HUB
        if (activeReceipt != null) {
            val booking = activeReceipt!!
            
            item {
                Text(
                    text = "Active Ride Control & Verification",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
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
        if (showSignaturePanel && activeReceipt != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SRG Automated Digital Verification", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
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

                        Spacer(modifier = Modifier.height(10.dp))

                        SignaturePad(
                            modifier = Modifier.padding(vertical = 4.dp),
                            onSignatureDrawn = { points ->
                                if (points.size > 5) {
                                    signField = "Signed via Signature TouchPad"
                                }
                            },
                            onClear = {
                                signField = ""
                            }
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
        if (showSecurePaymentPanel && activeReceipt != null) {
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
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Website",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Official Web Booking Portal",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "www.srgcarhire.co.ke",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.srgcarhire.co.ke"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                }
                            }
                        )
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
    val context = LocalContext.current
    
    // Create new vehicle fields
    var titleF by remember { mutableStateOf("") }
    var categoryF by remember { mutableStateOf("Small cars") }
    var priceF by remember { mutableStateOf("") }
    var fuelF by remember { mutableStateOf("Electric") }
    var seatsF by remember { mutableStateOf("5") }
    var transF by remember { mutableStateOf("Automatic") }
    var descF by remember { mutableStateOf("") }
    var locF by remember { mutableStateOf("Nairobi Westlands Hub") }
    var photoUrlF by remember { mutableStateOf("") }
    var additionalPhotosF by remember { mutableStateOf("") }

    // Admin state for changing password
    var tempNewPasscode by remember { mutableStateOf("") }

    // State parameters for tracking registrations
    val trackers by viewModel.carTrackers.collectAsStateWithLifecycle(initialValue = emptyList())
    var trackerRegNum by remember { mutableStateOf("") }
    var trackerCarName by remember { mutableStateOf("") }
    var trackerDriverName by remember { mutableStateOf("") }
    var trackerDriverPhone by remember { mutableStateOf("") }
    var trackerStatus by remember { mutableStateOf("En Route") }
    var trackerLoc by remember { mutableStateOf("Nairobi Westlands Hub") }
    var trackerCoords by remember { mutableStateOf("-1.2580, 36.8044") }
    var trackerSpeed by remember { mutableStateOf("60") }

    // Image Picker from Gallery contract
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                photoUrlF = uri.toString()
                Toast.makeText(context, "Selected photo from device gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Multiple Images Picker from Gallery contract
    val multiplePhotosPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                additionalPhotosF = uris.joinToString(",") { it.toString() }
                Toast.makeText(context, "Selected ${uris.size} photos from device gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    )

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

        // Change Administrative Passcode Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.fillMaxWidth().testTag("admin_change_password_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Change Administrative Passcode",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = tempNewPasscode,
                            onValueChange = { tempNewPasscode = it },
                            placeholder = { Text("New passcode", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFC107)
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("admin_new_passcode_field")
                        )
                        Button(
                            onClick = {
                                if (tempNewPasscode.isNotBlank()) {
                                    if (viewModel.updateAdminPasscode(tempNewPasscode)) {
                                        Toast.makeText(context, "Passcode updated successfully!", Toast.LENGTH_SHORT).show()
                                        tempNewPasscode = ""
                                    } else {
                                        Toast.makeText(context, "Failed to update passcode.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Passcode cannot be blank.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                            modifier = Modifier.testTag("admin_change_passcode_submit")
                        ) {
                            Text("Update", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
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

        // --- GPS VEHICLE TRACKER & ACTIVE REGISTRY ENGINE ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                border = BorderStroke(1.dp, Color(0xFFFFC107)),
                modifier = Modifier.fillMaxWidth().testTag("admin_tracking_registry_card")
            ) {
                var showAddTrackerPanel by remember { mutableStateOf(false) }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddTrackerPanel = !showAddTrackerPanel }
                            .testTag("admin_toggle_tracker_panel"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "GPS Tracker & Active Driver Registry",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${trackers.size} active vehicles under tracking agents",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = if (showAddTrackerPanel) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle tracker form",
                            tint = Color.LightGray
                        )
                    }

                    if (showAddTrackerPanel) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color(0x1DFFFFFF))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Register Transit Vehicle Tracker",
                            color = Color(0xFFFFC107),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = trackerRegNum,
                            onValueChange = { trackerRegNum = it },
                            placeholder = { Text("Registration Plate (e.g. KCG 432B)", color = Color.Gray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("tracker_reg_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = trackerCarName,
                                onValueChange = { trackerCarName = it },
                                placeholder = { Text("Car name (e.g. Tesla Model S)", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("tracker_car_input")
                            )

                            OutlinedTextField(
                                value = trackerDriverName,
                                onValueChange = { trackerDriverName = it },
                                placeholder = { Text("Driver Name (e.g. Jeff)", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("tracker_driver_input")
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val cleanPhone = trackerDriverPhone.trim().replace("\\s".toRegex(), "")
                        val isPhoneValid = cleanPhone.isEmpty() || cleanPhone.matches("^(?:\\+254|254|0)?([71]\\d{8})$".toRegex())

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = trackerDriverPhone,
                                onValueChange = { trackerDriverPhone = it },
                                placeholder = { Text("Driver's Phone (e.g. +254 712...)", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                singleLine = true,
                                isError = !isPhoneValid,
                                modifier = Modifier.weight(1.2f).testTag("tracker_phone_input")
                            )

                            OutlinedTextField(
                                value = trackerSpeed,
                                onValueChange = { trackerSpeed = it },
                                placeholder = { Text("Speed (km/h)", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.8f).testTag("tracker_speed_input")
                            )
                        }

                        if (!isPhoneValid) {
                            Text(
                                text = "Invalid Kenya phone number format (must match e.g. 07XXXXXXXX or +2547XXXXXXXX)",
                                color = Color(0xFFEF5350),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = trackerLoc,
                                onValueChange = { trackerLoc = it },
                                placeholder = { Text("Current Hub Location", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("tracker_location_input")
                            )

                            OutlinedTextField(
                                value = trackerCoords,
                                onValueChange = { trackerCoords = it },
                                placeholder = { Text("GPS Lat, Lng", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("tracker_coords_input")
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Status select pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Driver Status:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val statuses = listOf("En Route", "Stationary", "Completed")
                            statuses.forEach { s ->
                                val selected = trackerStatus == s
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) Color(0xFFFFC107) else Color(0xFF23232A))
                                        .clickable { trackerStatus = s }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                        .testTag("tracker_status_pill_$s")
                                ) {
                                    Text(
                                        text = s,
                                        color = if (selected) Color.Black else Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val phoneTrimmed = trackerDriverPhone.trim().replace("\\s".toRegex(), "")
                                val validated = phoneTrimmed.matches("^(?:\\+254|254|0)?([71]\\d{8})$".toRegex())

                                if (trackerRegNum.isNotBlank() && trackerCarName.isNotBlank() && trackerDriverPhone.isNotBlank()) {
                                    if (!validated) {
                                        Toast.makeText(context, "ERROR: Phone number must match Kenyan format (07... or +254...)", Toast.LENGTH_LONG).show()
                                    } else {
                                        val spd = trackerSpeed.toIntOrNull() ?: 50
                                        val formattedPhone = if (phoneTrimmed.startsWith("+254")) {
                                            phoneTrimmed
                                        } else if (phoneTrimmed.startsWith("254")) {
                                            "+$phoneTrimmed"
                                        } else if (phoneTrimmed.startsWith("0")) {
                                            "+254" + phoneTrimmed.substring(1)
                                        } else {
                                            "+254$phoneTrimmed"
                                        }

                                        viewModel.adminRegisterTracker(
                                            regNo = trackerRegNum.uppercase().trim(),
                                            vName = trackerCarName,
                                            dName = trackerDriverName,
                                            dPhone = formattedPhone,
                                            stat = trackerStatus,
                                            loc = trackerLoc,
                                            coords = trackerCoords,
                                            speed = spd
                                        )
                                        // Reset fields
                                        trackerRegNum = ""
                                        trackerCarName = ""
                                        trackerDriverName = ""
                                        trackerDriverPhone = ""
                                        trackerLoc = "Nairobi Westlands Hub"
                                        trackerCoords = "-1.2580, 36.8044"
                                        trackerSpeed = "60"
                                        Toast.makeText(context, "Registered new transit tracker successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Please configure Plate Number, Vehicle and Driver's phone!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("submit_tracker_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ACTIVATE LIVE GPS TRACKER", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0x19FFFFFF))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Live Active Trackers Registry Directory",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (trackers.isEmpty()) {
                        Text(
                            text = "No active tracker systems deployed inside Nairobi central map.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            trackers.forEach { tracker ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B20)),
                                    border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Authentic reflective license plate style badge
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(Color(0xFFFFF176))
                                                        .border(0.5.dp, Color.Black)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = tracker.registrationNumber,
                                                        color = Color.Black,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = tracker.vehicleName,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Direct Dialer Action + De-registration Action Buttons
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        try {
                                                            val intent = android.content.Intent(
                                                                android.content.Intent.ACTION_DIAL,
                                                                android.net.Uri.parse("tel:${tracker.driverPhoneNumber}")
                                                            )
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Dialer unavailable: ${tracker.driverPhoneNumber}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PhoneInTalk,
                                                        contentDescription = "Call driver",
                                                        tint = Color(0xFFFFC107),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = {
                                                        viewModel.adminDeleteTracker(tracker)
                                                    },
                                                    modifier = Modifier.size(28.dp).testTag("delete_tracker_${tracker.registrationNumber}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove tracker",
                                                        tint = Color(0xFFFF5252),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Driver info + details
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Driver: ${tracker.driverName}",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Phone: ${tracker.driverPhoneNumber}",
                                                    color = Color.LightGray,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    val statusLamp = if (tracker.status == "En Route") Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(statusLamp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = tracker.status.uppercase(),
                                                        color = if (tracker.status == "En Route") Color(0xFF81C784) else Color.LightGray,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = "${tracker.speedKmh} km/h • ${tracker.lastKnownLocation}",
                                                    color = Color.Gray,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "GPS Coordinates: ${tracker.gpsCoordinates}",
                                            color = Color.DarkGray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
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

                    IconButton(
                        onClick = {
                            viewModel.adminDeleteVehicle(vehicle.id)
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("admin_delete_vehicle_${vehicle.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete fleet asset",
                            tint = Color(0xFFFF5252)
                        )
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = photoUrlF,
                            onValueChange = { photoUrlF = it },
                            placeholder = { Text("Primary Photo URL (Optional)", color = Color.Gray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("admin_photo_url_field")
                        )
                        Button(
                            onClick = {
                                singlePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23232A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(56.dp).testTag("admin_upload_image_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "Upload from phone", tint = Color(0xFFFFC107))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Phone", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = additionalPhotosF,
                            onValueChange = { additionalPhotosF = it },
                            placeholder = { Text("Several Image URLs (comma-separated)", color = Color.Gray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("admin_additional_photos_field")
                        )
                        Button(
                            onClick = {
                                multiplePhotosPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23232A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(56.dp).testTag("admin_upload_multiple_images_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "Upload multiple", tint = Color(0xFFFFC107))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Multiple", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Simulated Quick Photo Preset Uploader
                    Text("Click below to upload / auto-fill with premium high-res photos:", color = Color(0xFFFFC107), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        val presetGallery = listOf(
                            Triple("Small Car", "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1502877338535-766e1452684a?auto=format&fit=crop&w=800&q=80"),
                            Triple("Saloon", "https://images.unsplash.com/photo-1622330248237-519e4541310d?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1553440569-bcc63803a83d?auto=format&fit=crop&w=800&q=80"),
                            Triple("Supercar", "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1611245801163-68f3780bf724?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80"),
                            Triple("Big Utility", "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1525609004556-c46c7d6cf0a3?auto=format&fit=crop&w=800&q=80"),
                            Triple("Helicopter", "https://images.unsplash.com/photo-1540962351504-03099e0a754b?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1494905998402-395d579af36f?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1583121274602-3e2820c69888?auto=format&fit=crop&w=800&q=80")
                        )
                        items(presetGallery.size) { index ->
                            val item = presetGallery[index]
                            AssistChip(
                                onClick = {
                                    photoUrlF = item.second
                                    additionalPhotosF = item.third
                                    Toast.makeText(context, "Preset details uploaded to edit panel!", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(item.first, color = Color.White, fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFF23232A),
                                    leadingIconContentColor = Color(0xFFFFC107)
                                )
                            )
                        }
                    }

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
                                    location = locF,
                                    photoUrl = photoUrlF,
                                    additionalPhotos = additionalPhotosF
                                )
                                // Blank fields
                                titleF = ""
                                priceF = ""
                                descF = ""
                                photoUrlF = ""
                                additionalPhotosF = ""
                                Toast.makeText(context, "Custom fleet item successfully added to active database!", Toast.LENGTH_SHORT).show()
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
        
        // SECTION: UPCOMING ADVERTISED EVENTS MANAGEMENT
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.fillMaxWidth().testTag("admin_events_manager_card")
            ) {
                var showAddEventPanel by remember { mutableStateOf(false) }
                
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddEventPanel = !showAddEventPanel }
                            .testTag("admin_toggle_events_manager"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Manage Advertising Events",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = if (showAddEventPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFFFFC107)
                        )
                    }
                    
                    AnimatedVisibility(visible = showAddEventPanel) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            var evTitle by remember { mutableStateOf("") }
                            var evDesc by remember { mutableStateOf("") }
                            var evDate by remember { mutableStateOf("") }
                            var evLocation by remember { mutableStateOf("") }
                            var evImage by remember { mutableStateOf("") }
                            
                            Text("PUBLISH NEW ADVERTISING EVENT", color = Color(0xFFFFC107), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            
                            OutlinedTextField(
                                value = evTitle,
                                onValueChange = { evTitle = it },
                                label = { Text("Event Title") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFFFFC107),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedBorderColor = Color(0xFFFFC107)
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("admin_event_title_input")
                            )
                            
                            OutlinedTextField(
                                value = evDate,
                                onValueChange = { evDate = it },
                                label = { Text("Event Date Range") },
                                placeholder = { Text("e.g. Saturday, July 4, 2026", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFFFFC107),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedBorderColor = Color(0xFFFFC107)
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("admin_event_date_input")
                            )
                            
                            OutlinedTextField(
                                value = evLocation,
                                onValueChange = { evLocation = it },
                                label = { Text("Location") },
                                placeholder = { Text("e.g. Wilson Airport Hangar 4", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFFFFC107),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedBorderColor = Color(0xFFFFC107)
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("admin_event_location_input")
                            )
                            
                            OutlinedTextField(
                                value = evImage,
                                onValueChange = { evImage = it },
                                label = { Text("Header Image URL") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFFFFC107),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedBorderColor = Color(0xFFFFC107)
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("admin_event_image_input")
                            )
                            
                            OutlinedTextField(
                                value = evDesc,
                                onValueChange = { evDesc = it },
                                label = { Text("Event Description") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFFFFC107),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedBorderColor = Color(0xFFFFC107)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("admin_event_desc_input")
                            )
                            
                            Button(
                                onClick = {
                                    if (evTitle.isNotBlank() && evDate.isNotBlank() && evLocation.isNotBlank() && evDesc.isNotBlank()) {
                                        viewModel.adminAddUpcomingEvent(
                                            title = evTitle,
                                            description = evDesc,
                                            dateText = evDate,
                                            location = evLocation,
                                            imageUrl = evImage
                                        )
                                        evTitle = ""
                                        evDesc = ""
                                        evDate = ""
                                        evLocation = ""
                                        evImage = ""
                                        Toast.makeText(context, "Upcoming event scheduled and published!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Fill in all mandatory fields (Title, Date, Location, Desc)", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                                modifier = Modifier.fillMaxWidth().testTag("admin_event_submit_button")
                            ) {
                                Text("PUBLISH ADVERTISING EVENT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            
                            Divider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 8.dp))
                            
                            val upcomingEvents by viewModel.upcomingEvents.collectAsStateWithLifecycle()
                            
                            Text("CURRENT ADVERTISED EVENTS (${upcomingEvents.size})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            
                            if (upcomingEvents.isEmpty()) {
                                Text("No events scheduled.", color = Color.Gray, fontSize = 11.sp)
                            } else {
                                upcomingEvents.forEach { event ->
                                    var isEditing by remember { mutableStateOf(false) }
                                    var editTitle by remember { mutableStateOf(event.title) }
                                    var editDate by remember { mutableStateOf(event.dateText) }
                                    var editLocation by remember { mutableStateOf(event.location) }
                                    var editDesc by remember { mutableStateOf(event.description) }
                                    
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F24)),
                                        border = BorderStroke(1.dp, Color(0x19FFFFFF)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (isEditing) {
                                                OutlinedTextField(
                                                    value = editTitle,
                                                    onValueChange = { editTitle = it },
                                                    label = { Text("Title") },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFFFC107)),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = editDate,
                                                    onValueChange = { editDate = it },
                                                    label = { Text("Date") },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFFFC107)),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = editLocation,
                                                    onValueChange = { editLocation = it },
                                                    label = { Text("Location") },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFFFC107)),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = editDesc,
                                                    onValueChange = { editDesc = it },
                                                    label = { Text("Description") },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFFFC107)),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { isEditing = false },
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Cancel", color = Color.White, fontSize = 11.sp)
                                                    }
                                                    Button(
                                                        onClick = {
                                                            val updatedEvent = event.copy(
                                                                title = editTitle,
                                                                dateText = editDate,
                                                                location = editLocation,
                                                                description = editDesc
                                                            )
                                                            viewModel.adminUpdateUpcomingEvent(updatedEvent)
                                                            isEditing = false
                                                            Toast.makeText(context, "Event updated!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                                                        modifier = Modifier.weight(1f).testTag("admin_event_save_${event.id}")
                                                    ) {
                                                        Text("Save", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Text(event.dateText, color = Color(0xFFFFC107), fontSize = 10.sp)
                                                        Text(event.location, color = Color.Gray, fontSize = 10.sp)
                                                    }
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        IconButton(
                                                            onClick = { isEditing = true },
                                                            modifier = Modifier.size(32.dp).testTag("admin_event_edit_${event.id}")
                                                        ) {
                                                            Icon(Icons.Default.Edit, contentDescription = "Edit event", tint = Color.White, modifier = Modifier.size(16.dp))
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.adminDeleteUpcomingEvent(event)
                                                                Toast.makeText(context, "Deleted event advertising card", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(32.dp).testTag("admin_event_delete_${event.id}")
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Delete event", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                                Text(event.description, color = Color.LightGray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }

    }
}

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onSignatureDrawn: (List<androidx.compose.ui.geometry.Offset>) -> Unit,
    onClear: () -> Unit
) {
    val points = remember { mutableStateListOf<androidx.compose.ui.geometry.Offset>() }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Touch Digital Signature Pad:",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                text = "Reset Pad",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        points.clear()
                        onClear()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            points.add(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newPoint = points.lastOrNull()?.let { it + dragAmount } ?: change.position
                            points.add(newPoint)
                            onSignatureDrawn(points)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (points.size > 1) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        val first = points.first()
                        moveTo(first.x, first.y)
                        for (i in 1 until points.size) {
                            val p = points[i]
                            lineTo(p.x, p.y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF00E676),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 6f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )
                } else if (points.isEmpty()) {
                    // Draw centered watermark guideline
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            alpha = 100
                            textSize = 34f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText("DRAW YOUR SIGNATURE HERE", size.width / 2f, size.height / 2f + 12f, paint)
                    }
                }
            }
        }
    }
}

// ----------------- BOOKING CONFIRMATION MODAL & SYSTEM PASS DISPATCH SYSTEM -----------------
@Composable
fun BookingConfirmationModal(
    booking: Booking,
    userProfile: UserProfile?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US) }
    
    // Stateful simulations for SMS dispatch
    var smsState by remember { mutableStateOf("Idle") } // Idle, Formatting, Encrypting, Sending, Sent
    val scope = rememberCoroutineScope()
    
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    RoundedCornerShape(24.dp)
                ),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Ring
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Confirmation Status Verified",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "RESERVATION SECURED",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "REF ID: #SRG-${booking.id}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Invoice Summary Box
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "RENTAL RECEIPT SUMMARY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        
                        // Row: Vehicle Model
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vehicle", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = booking.vehicleTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Row: Category
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Class", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = booking.vehicleCategory,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Row: Rental Duration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Duration", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "${booking.durationHours} Hours",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Row: Dates / Times
                        val startMs = if (booking.pickupTime > 0) booking.pickupTime else booking.bookedAt
                        val endMs = if (booking.returnTime > 0) booking.returnTime else startMs + (booking.durationHours * 3600 * 1000L)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pickup", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = sdf.format(Date(startMs)),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Return Target", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = sdf.format(Date(endMs)),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        // Row: Billing Sum
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL ESTIMATED BILLING",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Ksh. ${String.format(Locale.US, "%,.2f", booking.totalSpent)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("modal_confirmed_cost")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Digital Copy of Agreement Scroll Pane
                Text(
                    text = "DIGITAL LEASE & COVENANT DEED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SRG EXECS GENERAL COVENANT AGREEMENT (Ver 4.10)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "1. BINDING RENTAL LEASE:\nBy confirming this reservation, Lessee accepts that all terms under the SRG Executive Fleet Services apply. The vehicle ID ${booking.vehicleId} registered under SRG records is legally assigned.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "2. DIGITAL DRIVER COMPLIANCE:\nLessee warrants that they hold a valid unexpired driving permit. If not previously uploaded, digital signing and validation are mandatory before active remote unlocking of GPS telemetry will be completed.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "3. HIGHWAY SPEED LIMITS & SAFETY:\nFor safety and corporate policy, a speed limit ceiling of 110 km/h is enforced. Breaching telemetry thresholds flags automated notifications to HQ and local controllers.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "4. GPS GEOLOCKING BOUNDS:\nThe vehicle contains integrated GPS Radar. Venturing past designated borders or pre-declared routes without support clearance will activate active remote engine containment locks.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "5. ASSET SANITATION & RETURN:\nLessee agrees to return the asset in similar hygienic parameters. Re-fueling or recharging to a minimum of 30% baseline is requested. Failure might invite restoration surcharges.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                        
                        Text(
                            text = "SIGNED DIGITALLY VIA:\nEmail: ${booking.userEmail}\nDate: ${sdf.format(Date(booking.bookedAt))}\nDevice Verification: SRG-SECURE-ID-${booking.id}",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 11.sp
                        )
                    }
                    
                    // Small scroll guide gradient fading block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.62f))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active channels dispatch buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 1: Local System Notification Pass
                    Button(
                        onClick = {
                            triggerSystemNotification(context, booking)
                            Toast.makeText(context, "Real system notification pass posted!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_get_notification")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bell Pass", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Action 2: Digital copy delivery via stateful SMS simulation
                    Button(
                        onClick = {
                            if (smsState == "Idle") {
                                scope.launch {
                                    smsState = "Formatting"
                                    kotlinx.coroutines.delay(800)
                                    smsState = "Encrypting"
                                    kotlinx.coroutines.delay(1000)
                                    smsState = "Sending"
                                    kotlinx.coroutines.delay(1200)
                                    smsState = "Sent"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (smsState == "Sent") Color(0xFF2E7D32) else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (smsState == "Sent") Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_get_sms")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (smsState) {
                                    "Sent" -> Icons.Default.Check
                                    "Idle" -> Icons.Default.Sms
                                    else -> Icons.Default.Sync
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (smsState) {
                                    "Idle" -> "Send SMS"
                                    "Formatting" -> "Formatting..."
                                    "Encrypting" -> "Encrypting..."
                                    "Sending" -> "Sending..."
                                    "Sent" -> "SMS Delivered!"
                                    else -> "Processing..."
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (smsState != "Idle") {
                    Text(
                        text = when (smsState) {
                            "Formatting" -> "Formatting compact lease details for cellular transmission..."
                            "Encrypting" -> "Signing agreement payload with SHA-256 digital signature..."
                            "Sending" -> "Dispatched package via Telco gateway to ${userProfile?.phoneNumber ?: "+254 712 345678"}..."
                            "Sent" -> "Message successfully processed and acknowledged by cellular networks!"
                            else -> ""
                        },
                        fontSize = 9.sp,
                        color = if (smsState == "Sent") Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Done / Dismiss Bottom Main action
                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_close_confirmation")
                ) {
                    Text(
                        "DISMISS AGREEMENT & PROCEED",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

fun triggerSystemNotification(context: Context, booking: Booking) {
    val channelId = "srg_rentals_notification_channel"
    val channelName = "SRG Car Hire"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "SRG Booking and Digital Lease Agreement verification notices"
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    val formattedCost = String.format(Locale.US, "%,.2f", booking.totalSpent)
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("SRG Lease Secured: ${booking.vehicleTitle}")
        .setContentText("Booking #SRG-${booking.id} verified. Rate: Ksh. ${booking.pricePerHourAtBooking}/hr. Est: Ksh. $formattedCost.")
        .setStyle(NotificationCompat.BigTextStyle().bigText(
            "Booking confirmation receipt and digital agreement pass successfully generated for ${booking.vehicleTitle}.\n" +
            "Ref ID: #SRG-${booking.id}\n" +
            "Duration: ${booking.durationHours} Hours\n" +
            "Verification level: Digital Signature Approved\n" +
            "Total Billing: Ksh. $formattedCost"
        ))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        
    try {
        notificationManager.notify(booking.id, builder.build())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

