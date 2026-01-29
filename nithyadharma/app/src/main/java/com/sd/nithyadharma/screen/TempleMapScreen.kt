package com.sd.nithyadharma.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.compose.foundation.layout.size

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.sd.nithyadharma.util.LocationTracker
import com.sd.nithyadharma.util.Constants
import android.graphics.Color
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing // ADDED: Required for smooth pulsing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.Color as gcolor

import com.sd.nithyadharma.util.Constants.DEFAULT_MAP_ZOOM_LEVEL
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.sd.nithyadharma.model.TempleItem
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

// Imports for marker animation
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.animation.AccelerateDecelerateInterpolator
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import com.sd.nithyadharma.dao.AppDatabase

import com.sd.nithyadharma.util.PreferencesManager
import androidx.core.graphics.createBitmap
import com.sd.nithyadharma.R
import com.sd.nithyadharma.model.NDLanguage

@SuppressLint("ClickableViewAccessibility")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempleMapScreen(
    preferencesManager : PreferencesManager,
    onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Coroutine scope for launching suspend functions

    // NEW: Instantiate PreferencesManager
//    val preferencesManager = remember { PreferencesManager(context) }

    val currentLang by preferencesManager.getSelectedLanguage()
        .collectAsState(initial = NDLanguage.EN)

    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var pendingGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasInitialCenteringDone by remember { mutableStateOf(false) }

    // State to control if the map should automatically track the user's location
    var isTrackingMyLocation by remember { mutableStateOf(true) }

    // NEW: Collect the showVisitedTemples preference as state
    val hideVisitedTemples by preferencesManager.getHideVisitedTemples().collectAsState(initial = true)

    // NEW: Collect the showVisitedTemples preference as state
    val showOnlyMarkedTemples by preferencesManager.getShowOnlyMarkedTemples().collectAsState(initial = true)

    // NEW: State for showing the debug dialog for deleted IDs
    var showDeletedIdsDialog by remember { mutableStateOf(false) }
    val deletedTempleIds by preferencesManager.getDeletedTempleIds().collectAsState(initial = "")

    // Access AppDatabase and TempleDao directly
    val templeDao = remember { AppDatabase.getDatabase(context).templeDao() }

    // State to hold the list of nearby temples fetched from the DAO.
    var nearbyTemplesState by remember { mutableStateOf<List<TempleItem>>(emptyList()) }

    // This launcher is correctly defined here at the composable level.
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            LocationTracker.getCurrentLocation { geoPoint ->
                currentLocation = geoPoint
            }
        } else {
            Log.w("TempleMapScreen", "Location permission denied")
        }
    }

    // Configure osmdroid. This should only be done once per application lifecycle.
    Configuration.getInstance().load(
        context,
        context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    )

    BackHandler { onBackClick() }

//    val fallbackLocation = GeoPoint(10.9629, 79.3881)
    val radiusMiles = Constants.RADIUS_MILES

    // MapView needs to be initialized here (or in a remember block) so it's ready.
    lateinit var mapView: MapView

    var currentMarker by remember { mutableStateOf<Marker?>(null) } // Marker for single tap location

    // Gesture detector for single taps to place a temporary marker
    val gestureDetector = remember {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                try {
                    // When user taps on map, stop tracking their location
                    isTrackingMyLocation = false

                    currentMarker?.let { marker ->
                        mapView.overlays.remove(marker)
                    }
                    val iGeoPoint = mapView.projection?.fromPixels(e.x.toInt(), e.y.toInt())
                        ?: mapView.mapCenter
                    pendingGeoPoint = GeoPoint(iGeoPoint.latitude, iGeoPoint.longitude)

                    Log.i("TempleMapScreen", "Single tap confirmed! New pending location: $pendingGeoPoint")
                    val newMarker = Marker(mapView).apply {
                        position = pendingGeoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = ResourcesCompat.getDrawable(
                            mapView.resources,
                            org.osmdroid.library.R.drawable.person,
                            null
                        )
                    }
                    mapView.overlays.add(newMarker)
                    mapView.controller.animateTo(pendingGeoPoint)
                    currentMarker = newMarker
                    mapView.invalidate() // Refresh map to show the new marker
                } catch (ex: Exception) {
                    Log.e("TempleMapScreen", "Error handling single tap marker: ${ex.message}", ex)
                }
                return true
            }
            override fun onDown(e: MotionEvent): Boolean = true // Must return true for GestureDetector to process other events
        })
    }

    // Initialize MapView within a remember block to ensure it's created once
    mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(DEFAULT_MAP_ZOOM_LEVEL)

            setOnTouchListener { _, event ->
                // Let gesture detector handle taps first, but also let osmdroid handle other gestures
                gestureDetector.onTouchEvent(event)
                false // Important: Return false so osmdroid still handles other map gestures (scroll, zoom)
            }
        }
    }

    // --- START: PULSATION CODE ---

    // Define the single instance of MyLocationNewOverlay that will be animated
    val myLocationOverlay = remember {
        // Create an initial static magenta dot (will be immediately overwritten by the effect below)
        val markerSize = 105
        val baseRadius = 42f
        val newCenter = markerSize / 2f // New center point (52.5f)

        val personBitmap = createBitmap(markerSize, markerSize)
        val canvas = Canvas(personBitmap)
        val paint = Paint().apply {
            color = Color.RED
            isAntiAlias = true
        }
        canvas.drawCircle(newCenter, newCenter, baseRadius, paint)

        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            setPersonIcon(personBitmap)
            enableFollowLocation() // Set to follow location initially
        }
    }

    // 1. Compose Animation Setup (Generates the scale value)
    val infiniteTransition = rememberInfiniteTransition(label = "markerPulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f, // Start smaller than base size
        targetValue = 1.2f,  // Grow larger than base size
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScaleAnimation"
    )

    // 2. LaunchedEffect to update the native osmdroid marker icon on every frame
    LaunchedEffect(pulseScale) {
        // Base marker size/radius

        val baseMarkerRadius = 42f
        val newMarkerSize = 105
        val newCenter = newMarkerSize / 2f

        // Calculate the new radius based on the animated scale value
        val currentRadius = baseMarkerRadius * pulseScale

        // Redraw the Bitmap with the new size
        val personBitmap = Bitmap.createBitmap(newMarkerSize, newMarkerSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(personBitmap)
        val paint = Paint().apply {
            color = Color.parseColor("#FF7F00")
            isAntiAlias = true
        }

        // Draw the circle with the PULSATING RADIUS
        canvas.drawCircle(newCenter, newCenter, currentRadius, paint)

        // Update the native osmdroid marker icon and force a redraw
        myLocationOverlay.setPersonIcon(personBitmap)
        mapView.invalidate()
    }

    // LaunchedEffect to request location permission and get initial location.
    // This effect runs once when the composable enters the composition.
    LaunchedEffect(Unit) {
        // Register a callback for significant location changes from LocationTracker
        LocationTracker.onLocationChanged = { newGeoPoint ->
            currentLocation = newGeoPoint // This will trigger the `LaunchedEffect(currentLocation)` below
        }

        // Check and request location permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            LocationTracker.getCurrentLocation { geoPoint ->
                currentLocation = geoPoint // Initial location set
            }
        }
        else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            Log.i("TempleMapScreen", "Location permission not granted, launching permission request.")
        }
    }

    // Effect to TRIGGER fetching nearby temples and update map center whenever currentLocation changes.
    // This effect does NOT draw markers directly. It updates `nearbyTemplesState`.
    LaunchedEffect(currentLocation) {

        val userGeoPoint = currentLocation ?: return@LaunchedEffect

        isLoading = false // Location is ready

        if (!hasInitialCenteringDone) {
            mapView.controller.setCenter(userGeoPoint)
            mapView.controller.setZoom(DEFAULT_MAP_ZOOM_LEVEL)
            hasInitialCenteringDone = true
        }

        // Only set map center if isTrackingMyLocation is true
        if (isTrackingMyLocation) {
            mapView.controller.setCenter(currentLocation)
            mapView.controller.setZoom(DEFAULT_MAP_ZOOM_LEVEL)
        }

        // Initiate the asynchronous fetch of nearby temples
        scope.launch {
            val fetchedTemples = templeDao.getNearbyTemples( // Direct call on templeDao
                centerLat = userGeoPoint.latitude,
                centerLon = userGeoPoint.longitude,
                radiusMiles = radiusMiles
            )
            // Update the state variable. This will trigger the next LaunchedEffect for map drawing.
            nearbyTemplesState = fetchedTemples
            Log.d("TempleMapScreen", "Fetched ${nearbyTemplesState.size} nearby temples from DAO for ${userGeoPoint.latitude}, ${userGeoPoint.longitude}")
        }
    }

    // Effect to update map markers whenever the `nearbyTemplesState` list OR `showVisitedTemples` changes.
    LaunchedEffect(nearbyTemplesState, hideVisitedTemples, showOnlyMarkedTemples) { // Added showVisitedTemples as a dependency
        // Clear all existing overlays to prevent duplicates, but re-add the essential ones.
        mapView.overlays.clear()

        // --- MODIFIED: Use the externally defined, pulsating myLocationOverlay ---
        myLocationOverlay.enableMyLocation()
        mapView.overlays.add(myLocationOverlay)

        // If a temporary single-tap marker exists, add it back after clearing
        currentMarker?.let { marker ->
            mapView.overlays.add(marker)
        }

        Log.d("TempleMapScreen", "showOnlyMarkedTemples ${showOnlyMarkedTemples} , hideVisitedTemples ${hideVisitedTemples}")

        // Now, draw markers based on the LATEST `nearbyTemplesState`
        if (nearbyTemplesState.isNotEmpty()) {

            nearbyTemplesState.forEach { temple ->
                // --- NEW: Skip if temple is not marked when user wants to see only marked temples ---
                // This is the core logic for filtering based on the toggle and visit_dt
                if (showOnlyMarkedTemples) {
                    if (temple.marked?.equals("y") != true) {
//                        Log.d("TempleMapScreen", "Skipping marker for non marked temple ${temple.name} (ID: ${temple.temple_id}) as 'show marked' is true.")
                        return@forEach // Skip to the next temple in the loop
                    }
                    else {
//                        Log.d("TempleMapScreen", "marked temple ${temple.name} (ID: ${temple.temple_id}) as 'show marked' is true.")
                    }
                }
                // --- End NEW Skip Logic ---

                // --- NEW: Skip if temple is visited and user wants to hide visited temples ---
                // This is the core logic for filtering based on the toggle and visit_dt
                if (hideVisitedTemples && temple.visit_dt != null) {
//                    Log.d("TempleMapScreen", "Skipping marker for visited temple ${temple.name} (ID: ${temple.temple_id}) as 'hide Visited' is true.")
                    return@forEach // Skip to the next temple in the loop
                }

                // --- 1. Skip rendering if deity is unrecognized ---
                val drawableResId = when {
                    temple.deity.contains("shiva", ignoreCase = true) -> R.mipmap.marker_purple
                    temple.deity.contains("vishnu", ignoreCase = true) -> R.mipmap.marker_blue
                    temple.deity.contains("murugan", ignoreCase = true) -> R.mipmap.marker_green
//                    temple.deity.contains("shakthi", ignoreCase = true) -> R.mipmap.marker_yellow
//                    temple.deity.contains("ganesh", ignoreCase = true) -> R.mipmap.marker_orange
                    else -> -1 // Sentinel value to indicate "skip"
                }

                if (drawableResId == -1) {
//                    Log.d("TempleMapScreen", "Skipping marker for temple ${temple.name} (ID: ${temple.temple_id}) due to unrecognized deity type.")
                    return@forEach // Skip to the next temple in the loop
                }

                try {
                    val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate()
                    val (lat, lon) = temple.latlong.split(",").map { it.trim().toDouble() }
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(lat, lon)
                        title = temple.name + ", " + temple.place + " (" + temple.temple_id +")"
                        icon = drawable
                        // Conditionally set snippet based on temple.tags being null
                        snippet = temple.tags ?: "" // Still set to empty string if null, then control visibility in infoWindow
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) // Anchor point of marker icon

                        infoWindow = object : MarkerInfoWindow(R.layout.markerbubblepack, mapView) {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onOpen(item: Any?) {
                                if (item is Marker) {
                                    super.onOpen(item)
                                    val titleView = mView.findViewById<TextView>(R.id.bubble_title)
                                    val descView = mView.findViewById<TextView>(R.id.bubble_description)
                                    val dirBtn = mView.findViewById<Button>(R.id.bubble_moreinfo)

                                    val deleteBtn = mView.findViewById<Button>(R.id.bubble_delete)
                                    val visitedTxtBox = mView.findViewById<TextView>(R.id.bubble_subdescription)

                                    if ( Constants.PAYING_CUSTOMER) {
                                        deleteBtn.visibility = View.VISIBLE
                                        visitedTxtBox.visibility = View.VISIBLE
                                    } else {
                                        deleteBtn.visibility = View.GONE
                                        visitedTxtBox.visibility = View.GONE
                                    }

                                    val layout = mView as? ViewGroup

                                    // Style the overall layout
                                    layout?.apply {
                                        setPadding(20, 20, 20, 20)
                                        setBackgroundColor(Color.parseColor("#FFF9F0")) // soft cream
                                        background = GradientDrawable().apply {
                                            setColor(Color.parseColor("#FFF9F0")) // light background
                                            cornerRadius = 32f
                                            setStroke(2, Color.parseColor("#CCCCCC"))
                                        }
                                    }
                                    // Style the title
                                    titleView?.apply {
                                        text = item.title
                                        setTextColor(Color.parseColor("#222222"))
                                        textSize = 16f
                                        typeface = Typeface.DEFAULT_BOLD
                                    }
                                    // MODIFIED: Conditionally set text and visibility for description
                                    descView?.apply {
                                        if (item.snippet.isNullOrEmpty()) {
                                            visibility = View.GONE // Hide if snippet is null or empty
                                        } else {
                                            visibility = View.VISIBLE // Show otherwise
                                            text = item.snippet
                                            setTextColor(Color.parseColor("#555555"))
                                            textSize = 14f
                                            setPadding(0, 10, 0, 10)
                                        }
                                    }

                                    // Style and set listener for the directions button
                                    dirBtn?.apply {
//                                        visibility = View.VISIBLE
                                        text = "go →"
                                        setTextColor(Color.YELLOW)
                                        textSize = 14f
                                        minWidth = 0
                                        minHeight = 0
                                        minimumWidth = 0
                                        minimumHeight = 0
                                        setPadding(50, 8, 50, 8)

                                        background = GradientDrawable().apply {
                                            setColor(Color.parseColor("#8B4513")) // SaddleBrown
                                            cornerRadius = 24f
                                        }
                                        val params = layoutParams as? ViewGroup.MarginLayoutParams
                                        params?.setMargins(16, 12, 0, 0)
                                        layoutParams = params

                                        setOnClickListener {
                                            val gmmIntentUri = Uri.parse("google.navigation:q=${item.position.latitude},${item.position.longitude}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                            mapIntent.setPackage("com.google.android.apps.maps")
                                            context.startActivity(mapIntent)
                                        }
                                    }

                                    var currentTempleItemForInfoWindow by mutableStateOf<TempleItem?>(null)
                                    // --- Visited Status Logic: Fetch Fresh Data on Open ---
                                    // This local mutable state is only for the UI within this specific info window
                                    // It helps keep the UI responsive while the DB update happens
                                    if ( Constants.PAYING_CUSTOMER) {

                                        val templeIdFromMarker = temple.temple_id
                                        if (templeIdFromMarker != null) {
                                            scope.launch {
                                                currentTempleItemForInfoWindow = templeDao.getTempleById(templeIdFromMarker)

                                                // Update the TextView immediately after fetching fresh data
                                                visitedTxtBox?.apply {
                                                    text =
                                                        if (currentTempleItemForInfoWindow?.visit_dt != null) {
                                                            "✓ Visited (${currentTempleItemForInfoWindow!!.visit_dt})"
                                                        } else {
                                                            "□ Not Visited"
                                                        }
                                                    setTextColor(if (currentTempleItemForInfoWindow?.visit_dt != null) Color.BLUE else Color.BLACK)
                                                    mView.invalidate() // Invalidate the InfoWindow's view
                                                }
                                            }
                                        }

                                        visitedTxtBox?.apply {
                                            visibility = View.VISIBLE
                                            // The initial text "Loading status..." is removed.
                                            // The text will be set by the 'scope.launch' above as soon as data is fetched.
                                            textSize = 14f
                                            isClickable = true
                                            isFocusable = true

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                val attrs =
                                                    intArrayOf(android.R.attr.selectableItemBackground)
                                                val typedArray =
                                                    context.theme.obtainStyledAttributes(attrs)
                                                val backgroundRes = typedArray.getResourceId(0, 0)
                                                typedArray.recycle()
                                                background = ContextCompat.getDrawable(
                                                    context,
                                                    backgroundRes
                                                )
                                            }
                                            setPadding(0, 10, 0, 10)

                                            setOnClickListener {
                                                currentTempleItemForInfoWindow?.let { templeInInfoWindow -> // Use the freshly fetched temple item from local state
                                                    scope.launch {
                                                        val isCurrentlyVisited =
                                                            templeInInfoWindow.visit_dt != null

                                                        // Only proceed if the temple is NOT currently visited (i.e., visit_dt is null)
                                                        if (!isCurrentlyVisited) {
                                                            val currentMonth =
                                                                LocalDate.now().month.name.lowercase(
                                                                    Locale.getDefault()
                                                                )
                                                            val formattedMonth =
                                                                currentMonth.replaceFirstChar {
                                                                    if (it.isLowerCase()) it.titlecase(
                                                                        Locale.getDefault()
                                                                    ) else it.toString()
                                                                }
                                                            val currentYearShort =
                                                                LocalDate.now().year.toString()
                                                                    .takeLast(2)
                                                            val newVisitedDate =
                                                                "$formattedMonth $currentYearShort"

                                                            // Update the DB
                                                            templeDao.updateTempleVisitedDate(
                                                                templeInInfoWindow.temple_id,
                                                                newVisitedDate
                                                            )
                                                            Log.d(
                                                                "TempleMapScreen",
                                                                "Temple ${templeInInfoWindow.name} (ID: ${templeInInfoWindow.temple_id}) visit_dt updated to: $newVisitedDate. InfoWindow updated locally."
                                                            )

                                                            // --- IMMEDIATE UI UPDATE ON THE INFOWINDOW ---
                                                            // Update the local state of the TextView directly for immediate feedback
                                                            visitedTxtBox?.apply {
                                                                text = "✓ Visited ($newVisitedDate)"
                                                                setTextColor(Color.GREEN)
                                                                mView.invalidate() // Invalidate InfoWindow to reflect changes
                                                            }

                                                            // Update the `currentTempleItemForInfoWindow` local state after DB update.
                                                            // This makes sure if the user clicks again rapidly, the 'isCurrentlyVisited'
                                                            // logic will use the most recent value without another DB fetch.
                                                            currentTempleItemForInfoWindow =
                                                                templeInInfoWindow.copy(visit_dt = newVisitedDate)

                                                            // After updating, re-fetch temples for the current location
                                                            // This ensures the map markers and info window state are fresh,
                                                            // especially if the "Show Visited" toggle is off.
                                                            currentLocation?.let { geoPoint ->
                                                                val refetchedTemples =
                                                                    templeDao.getNearbyTemples(
                                                                        centerLat = geoPoint.latitude,
                                                                        centerLon = geoPoint.longitude,
                                                                        radiusMiles = radiusMiles
                                                                    )
                                                                nearbyTemplesState =
                                                                    refetchedTemples
                                                                Log.d(
                                                                    "TempleMapScreen",
                                                                    "Re-fetched ${nearbyTemplesState.size} temples after visit_dt update."
                                                                )
                                                            }
                                                            item.closeInfoWindow() // Close the info window so it can refresh (e.g., marker might disappear)

                                                        } else {
                                                            // If it's already visited, do nothing to the date or DB.
                                                            // The UI should already reflect the visited state from the onOpen call.
                                                            Log.d(
                                                                "TempleMapScreen",
                                                                "Temple ${templeInInfoWindow.name} (ID: ${templeInInfoWindow.temple_id}) is already visited. Not changing visit_dt."
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Style and set listener for the directions button -todo-
                                        deleteBtn?.apply {
//                                        visibility = View.VISIBLE
                                            text = "Delete"
                                            setTextColor(Color.YELLOW)
                                            textSize = 14f
                                            minWidth = 0
                                            minHeight = 0
                                            minimumWidth = 0
                                            minimumHeight = 0
                                            setPadding(50, 8, 50, 8)

                                            background = GradientDrawable().apply {
                                                setColor(Color.parseColor("#8B0000")) // dark red
                                                cornerRadius = 24f
                                            }
                                            val params = layoutParams as? ViewGroup.MarginLayoutParams
                                            params?.setMargins(16, 12, 0, 0)
                                            layoutParams = params

                                            setOnClickListener {
                                                // this must put the id of the item in a place comma separated so i can see and copy and delete items from a global view
                                                temple.temple_id?.let { id ->
                                                    scope.launch {
                                                        preferencesManager.addDeletedTempleId(id)
                                                        // delete from table as weall ?? todo
                                                        Log.d("TempleMapScreen", "Marked temple ID $id for deletion.")
                                                        item.closeInfoWindow() // Close info window after action
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // --- End Visited Status Logic ---
                                }
                            }
                        }
                        setOnMarkerClickListener { marker, _ ->
                            InfoWindow.closeAllInfoWindowsOn(mapView) // Close other info windows
                            marker.showInfoWindow() // Show this marker's info window
                            true // Consume the event
                        }
                    }
                    mapView.overlays.add(marker) // Add the marker to the map

                    // --- Animation on laying the marker (Fade-in) ---
                    val handler = Handler(Looper.getMainLooper())
                    marker.setAlpha(0f) // Start invisible
                    val alphaAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
//                        ValueAnimator.setDuration = 500L // 500ms fade-in duration
                        interpolator = AccelerateDecelerateInterpolator() // Smooth acceleration/deceleration
                        addUpdateListener { animation ->
                            marker.setAlpha(animation.animatedValue as Float)
                            mapView.invalidate() // Request redraw on each alpha update
                        }
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                marker.setAlpha(1f) // Ensure final state is fully visible
                                mapView.invalidate()
                            }
                        })
                    }
                    // Start the animation with a small delay for staggered appearance if many markers
                    handler.postDelayed({ alphaAnimator.start() }, 50L) // Small delay for effect
                    // --- End Animation ---

                } catch (e: Exception) {
                    Log.e("TempleMap", "Error processing temple for marker: ${e.message}", e)
                }
            }
        }
        mapView.invalidate() // Invalidate map to ensure all overlays and markers are drawn
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                title = {
                    Text(LocaleManager.getString("tm_title", currentLang))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
                    val pulseSize by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ), label = "pulseSizeAnimation"
                    )
                    // Updated IconButton to explicitly enable tracking
                    IconButton(
                        onClick = {
                            isTrackingMyLocation = true // Start tracking again
                            LocationTracker.getCurrentLocation { geoPoint ->
                                currentLocation = geoPoint
                                mapView.controller.animateTo(geoPoint)
                            }
                        },
                        modifier = Modifier
                            .background(gcolor.Transparent, CircleShape)
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Current Location",
                            modifier = Modifier.scale(pulseSize)
                        )
                    }
                    IconButton(
                        onClick = {
                            isTrackingMyLocation = true // Start tracking again
                            pendingGeoPoint?.let {
                                currentLocation = it // This will trigger LaunchedEffect(currentLocation)
                            }
                        },
                        modifier = Modifier
                            .background(gcolor.Transparent, CircleShape)
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "user selected Location",
                            modifier = Modifier.scale(pulseSize)
                        )
                    }
                    // NEW: Debug Button for Deleted IDs (Comment out for public release)
                    if ( Constants.PAYING_CUSTOMER) {
                        IconButton(onClick = { showDeletedIdsDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Debug Deleted IDs")
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                val playArrowId = "playArrow"
                val inlineContent = mapOf(
                    Pair(
                        playArrowId,
                        InlineTextContent(
                            placeholder = Placeholder(
                                width = 24.sp, // Adjust icon size as needed
                                height = 24.sp,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play to see temples",
//                                tint = LocalContentColor.current
//                                tint = gcolor.Black
                            )
                        }
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val localizedText = LocaleManager.getString("tm_bottom", currentLang)  // e.g. "Click anywhere on map and press %1s on top right to see temples there"

                    Text(
                        text = buildAnnotatedString {
                            // Split the localized string at the %1s placeholder
                            val parts = localizedText.split("[icon]", limit = 2)
                            // Part before the placeholder
                            append(parts[0])  // "Click anywhere on map and press "
                            // Insert the inline image
                            appendInlineContent(playArrowId)
                            // Part after the placeholder (if any)
                            if (parts.size > 1) {
                                append(parts[1])  // " on top right to see temples there"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        inlineContent = inlineContent
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
//                    color = gcolor.Yellow,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Finding your location...Turn on Location services" +
                            " if you havent for this page to work.",
                    fontSize = 18.sp
                )
            }
        }
        else {
            AndroidView(
                factory = { mapView },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }

        // NEW: Debug Dialog for Deleted IDs (Comment out for public release)
        if (showDeletedIdsDialog) {
            AlertDialog(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurface,
                onDismissRequest = { showDeletedIdsDialog = false },
                title = { Text("Deleted Temple IDs (Debug)") },
                text = {
                    Column {
                        Text("IDs: ${deletedTempleIds.ifEmpty { "None" }}")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("Deleted Temple IDs", deletedTempleIds)
                            clipboardManager.setPrimaryClip(clipData)
                            Log.d("TempleMapScreen", "Copied deleted IDs to clipboard: $deletedTempleIds")
                        }) {
                            Text("Copy to Clipboard")
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch {
                                preferencesManager.clearDeletedTempleIds()
                                showDeletedIdsDialog = false // Close dialog after clearing
                            }
                        }) {
                            Text("Clear All IDs")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDeletedIdsDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}