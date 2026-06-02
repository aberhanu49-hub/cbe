package com.example.traffic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

// Class representing a vehicle in the simulation
data class Vehicle(
    var id: Int,
    var x: Float,
    var y: Float,
    var speed: Float,
    var color: Color,
    val direction: Direction,
    val roadIndex: Int
)

enum class Direction { NORTH, SOUTH, EAST, WEST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrafficSimulatorScreen(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var isRunning by remember { mutableStateOf(true) }
    var signalMode by remember { mutableStateOf("AI Auto") } // "Manual", "AI Auto", "Green Wave"
    
    // Signal timers
    var eastWestGreenTime by remember { mutableStateOf(25f) }
    var northSouthGreenTime by remember { mutableStateOf(20f) }
    
    // Selected intersection for analysis
    var selectedIntersection by remember { mutableStateOf("Mexico Square") }
    
    // Real-time metrics
    var totalFlowRate by remember { mutableStateOf(1450) }
    var averageDelaySeconds by remember { mutableStateOf(14f) }
    var congestionIndex by remember { mutableStateOf(32.5f) }
    
    // Simulation Tick state
    var tickCount by remember { mutableStateOf(0) }
    
    // Simulator parameters
    var activeTrafficDensity by remember { mutableStateOf(0.6f) } // 0 to 1
    
    // Real-time synchronization log
    val syncLogs = remember { mutableStateListOf("System initialized", "Synced real-time CCTV feeds", "Synchronizing CBE Payment terminal flows") }

    // Vehicles state
    val vehiclesList = remember { mutableStateListOf<Vehicle>() }
    
    // Initialize vehicles
    LaunchedEffect(Unit) {
        val rand = Random()
        // Create initial cars
        for (i in 1..24) {
            val direction = Direction.values()[rand.nextInt(4)]
            val roadIndex = rand.nextInt(3)
            val color = when (rand.nextInt(4)) {
                0 -> Color(0xFFE53935) // Red
                1 -> Color(0xFF1E88E5) // Blue
                2 -> Color(0xFFFDD835) // Yellow
                else -> Color(0xFF43A047) // Green
            }
            vehiclesList.add(
                Vehicle(
                    id = i,
                    x = rand.nextFloat() * 1000f,
                    y = rand.nextFloat() * 1000f,
                    speed = 2f + rand.nextFloat() * 4f,
                    color = color,
                    direction = direction,
                    roadIndex = roadIndex
                )
            )
        }
    }

    // Active simulation processing loop
    LaunchedEffect(isRunning, signalMode, activeTrafficDensity, eastWestGreenTime, northSouthGreenTime) {
        if (isRunning) {
            while (true) {
                delay(30) // ~30fps update
                tickCount++
                
                // Update vehicle positions based on direction
                for (i in vehiclesList.indices) {
                    val car = vehiclesList[i]
                    val speedScalar = (1.5f - congestionIndex / 100f).coerceAtLeast(0.3f)
                    val step = car.speed * speedScalar
                    
                    when (car.direction) {
                        Direction.EAST -> {
                            car.x += step
                            if (car.x > 1000f) car.x = -50f
                        }
                        Direction.WEST -> {
                            car.x -= step
                            if (car.x < -50f) car.x = 1000f
                        }
                        Direction.SOUTH -> {
                            car.y += step
                            if (car.y > 1000f) car.y = -50f
                        }
                        Direction.NORTH -> {
                            car.y -= step
                            if (car.y < -50f) car.y = 1000f
                        }
                    }
                    // Trigger recomposition by force setting / rewriting
                    vehiclesList[i] = car.copy()
                }

                // Randomly change real-time metrics to simulate flow
                if (tickCount % 50 == 0) {
                    val optimalRatio = (eastWestGreenTime / (northSouthGreenTime + 0.1f))
                    if (optimalRatio in 1.1f..1.4f) {
                        congestionIndex = (congestionIndex - 0.5f).coerceAtLeast(15.0f)
                        averageDelaySeconds = (averageDelaySeconds - 0.2f).coerceAtLeast(8f)
                        totalFlowRate += (10..30).random()
                    } else {
                        congestionIndex = (congestionIndex + 0.8f).coerceAtMost(85.0f)
                        averageDelaySeconds = (averageDelaySeconds + 0.4f).coerceAtMost(45f)
                    }
                    
                    if (Random().nextInt(8) == 1) {
                        val flowChanges = listOf(
                            "Optimized Signal timings at $selectedIntersection",
                            "High density traffic detected northbound on Churchill Road",
                            "CBEBirr Toll Synced: 25 Vehicles processed auto-debit",
                            "Real-time sensor calibration complete",
                            "Analytics update: Peak starting in 45 minutes"
                        )
                        syncLogs.add(0, flowChanges.random())
                        if (syncLogs.size > 8) syncLogs.removeLast()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Urban Traffic Flow Simulator", fontSize = 20.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF5C075C) // Rich CBE purple
                ),
                actions = {
                    IconButton(onClick = { isRunning = !isRunning }) {
                        Icon(
                            if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Sim",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF5EEF5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 1. Simulation Frame And Control Panel Side-by-Side (or Stacked)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val drawWidth = maxWidth
                    val drawHeight = maxHeight

                    // Canvas to render the road grid, interactive buttons, heatmap overlay, moving vehicles
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE8F5E9)) // light park background
                    ) {
                        // Draw Roads
                        drawCityGrid(size.width, size.height)

                        // Draw bottleneck heatmaps (Glows based on congestion)
                        drawTrafficBottlenecks(size.width, size.height, congestionIndex)

                        // Draw moving cars
                        for (car in vehiclesList) {
                            drawVehicle(car, size.width, size.height)
                        }

                        // Draw traffic lights
                        drawTrafficLights(size.width, size.height, signalMode, tickCount, eastWestGreenTime, northSouthGreenTime)
                    }

                    // Floating Intersection Info overlay
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(10.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Column {
                            Text("Active Region: $selectedIntersection", color = Color.White, fontSize = 12.sp, style = MaterialTheme.typography.titleSmall)
                            Text("Flow: $totalFlowRate vehicles/hr", color = Color(0xFF81C784), fontSize = 10.sp)
                            Text("Congestion: ${congestionIndex.toInt()}%", color = if (congestionIndex < 40) Color.Green else if (congestionIndex < 70) Color.Yellow else Color.Red, fontSize = 10.sp)
                            Text("Avg Speed: ${(55 - congestionIndex * 0.45).toInt()} km/h", color = Color.Cyan, fontSize = 10.sp)
                        }
                    }

                    // Floating Legend
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .padding(8.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5252), RoundedCornerShape(4.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("High Congestion Bottleneck", fontSize = 9.sp, color = Color.DarkGray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF69F0AE), RoundedCornerShape(4.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Flowing Clean Lane", fontSize = 9.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Real-Time Parameters & Signal Timings Optimization
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.7f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    item {
                        Text("Signal Timing Optimization Matrix", style = MaterialTheme.typography.titleMedium, color = Color(0xFF5C075C))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("City Planners optimize green-light windows based on dynamic CCTV sensors and local flows", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        // Timing Optimization Mode Select
                        Text("Optimization Engine Mode", fontSize = 12.sp, color = Color.DarkGray)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Manual Control", "AI Auto", "Green Wave").forEach { mode ->
                                val selected = signalMode == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = { signalMode = mode },
                                    label = { Text(mode, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF851C80),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        // Interactive Adjustments (Sliders)
                        Text("East-West Green Timing: ${eastWestGreenTime.toInt()} secs", fontSize = 12.sp, color = Color.DarkGray)
                        Slider(
                            value = eastWestGreenTime,
                            onValueChange = { eastWestGreenTime = it },
                            valueRange = 5f..60f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF851C80),
                                activeTrackColor = Color(0xFF851C80)
                            )
                        )
                        
                        Text("North-South Green Timing: ${northSouthGreenTime.toInt()} secs", fontSize = 12.sp, color = Color.DarkGray)
                        Slider(
                            value = northSouthGreenTime,
                            onValueChange = { northSouthGreenTime = it },
                            valueRange = 5f..60f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFC08A50),
                                activeTrackColor = Color(0xFFC08A50)
                            )
                        )
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 10.dp))
                        Text("Predictive Peak Traffic Hours", style = MaterialTheme.typography.titleSmall, color = Color(0xFF5C075C))
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Predictive load graph on Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color(0xFFEEEEEE), RoundedCornerShape(6.dp))
                                .padding(6.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawPeakAnalyticsGraph(size.width, size.height)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("06:00 (Off-peak)", fontSize = 8.sp, color = Color.Gray)
                            Text("09:00 (Morning Peak)", fontSize = 8.sp, color = Color.Red)
                            Text("13:00 (Mid-day)", fontSize = 8.sp, color = Color.Gray)
                            Text("18:00 (Evening Peak)", fontSize = 8.sp, color = Color.Red)
                            Text("22:00 (Night)", fontSize = 8.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        // Real-time integration sync window
                        Text("Real-Time Sensor & Bank API Sync Logs", fontSize = 12.sp, color = Color.DarkGray, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E101E), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            syncLogs.forEach { log ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = "", tint = Color(0xFFE9C5A2), modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(log, color = Color(0xFFE2D6E2), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Draw city intersections schematically
private fun DrawScope.drawCityGrid(width: Float, height: Float) {
    val roadWidth = 100f
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.DKGRAY
    }
    
    // Draw EW Road 1 (Middle)
    drawRect(
        color = Color(0xFF37474F),
        topLeft = Offset(0f, height/2 - roadWidth/2),
        size = Size(width, roadWidth)
    )

    // Draw NS Road 1 (Middle)
    drawRect(
        color = Color(0xFF37474F),
        topLeft = Offset(width/2 - roadWidth/2, 0f),
        size = Size(roadWidth, height)
    )

    // Draw lanes EW dashed
    var x = 0f
    while (x < width) {
        drawLine(
            color = Color.Yellow,
            start = Offset(x, height/2),
            end = Offset(x + 15f, height/2),
            strokeWidth = 2f
        )
        x += 35f
    }

    // Draw lanes NS dashed
    var y = 0f
    while (y < height) {
        drawLine(
            color = Color.Yellow,
            start = Offset(width/2, y),
            end = Offset(width/2, y + 15f),
            strokeWidth = 2f
        )
        y += 35f
    }

    // Draw white zebra border lines
    drawRect(
        color = Color.White,
        topLeft = Offset(width/2 - roadWidth/2, height/2 - roadWidth/2),
        size = Size(roadWidth, 4f)
    )
    drawRect(
        color = Color.White,
        topLeft = Offset(width/2 - roadWidth/2, height/2 + roadWidth/2 - 4f),
        size = Size(roadWidth, 4f)
    )
}

// Bottlenecks represented as pulsing circles (Heatmap)
private fun DrawScope.drawTrafficBottlenecks(width: Float, height: Float, congestion: Float) {
    val radius = 120f + (congestion * 1.5f)
    val opacity = (congestion / 100f).coerceIn(0.1f, 0.85f)
    
    // Pulsing heatmap color at intersection center
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Red.copy(alpha = opacity),
                Color.Yellow.copy(alpha = opacity * 0.4f),
                Color.Transparent
            ),
            center = Offset(width/2, height/2),
            radius = radius
        ),
        radius = radius,
        center = Offset(width/2, height/2)
    )
}

// Draw animated vehicle on road
private fun DrawScope.drawVehicle(car: Vehicle, canvasWidth: Float, canvasHeight: Float) {
    // Standard vehicle coordinates scaled to road boundaries
    val rx = (car.x / 1000f) * canvasWidth
    val ry = (car.y / 1000f) * canvasHeight
    
    val roadWidth = 100f
    
    val finalX: Float
    val finalY: Float
    
    when (car.direction) {
        Direction.EAST, Direction.WEST -> {
            finalX = rx
            // Shift slight for lane separation
            finalY = canvasHeight/2 + (if (car.direction == Direction.EAST) 20f else -20f)
        }
        Direction.NORTH, Direction.SOUTH -> {
            // Shift slight for lane separation
            finalX = canvasWidth/2 + (if (car.direction == Direction.SOUTH) -20f else 20f)
            finalY = ry
        }
    }
    
    // Draw vehicle box
    drawRoundRect(
        color = car.color,
        topLeft = Offset(finalX - 12f, finalY - 8f),
        size = Size(24f, 16f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    
    // Draw windshield (schematic visual styling)
    drawRect(
        color = Color.Black,
        topLeft = Offset(finalX + (if (car.direction == Direction.EAST) 6f else -10f), finalY - 5f),
        size = Size(4f, 10f)
    )
}

// Traffic Signal colors changing dynamically
private fun DrawScope.drawTrafficLights(
    width: Float,
    height: Float,
    mode: String,
    tick: Int,
    ewGreen: Float,
    nsGreen: Float
) {
    val cycleLength = (ewGreen + nsGreen).toInt().coerceAtLeast(10)
    val ewGreenInt = ewGreen.toInt()
    val currCycleSecs = (tick / 30) % cycleLength
    
    val isEwGreen = currCycleSecs < ewGreenInt
    
    val ewColor = if (isEwGreen) Color.Green else Color.Red
    val nsColor = if (!isEwGreen) Color.Green else Color.Red

    val lightSpacing = 70f
    
    // East-West Traffic Lights
    drawCircle(
        color = Color.Black,
        radius = 12f,
        center = Offset(width/2 - lightSpacing, height/2 - 30f)
    )
    drawCircle(
        color = ewColor,
        radius = 8f,
        center = Offset(width/2 - lightSpacing, height/2 - 30f)
    )

    drawCircle(
        color = Color.Black,
        radius = 12f,
        center = Offset(width/2 + lightSpacing, height/2 + 30f)
    )
    drawCircle(
        color = ewColor,
        radius = 8f,
        center = Offset(width/2 + lightSpacing, height/2 + 30f)
    )

    // North-South Traffic Lights
    drawCircle(
        color = Color.Black,
        radius = 12f,
        center = Offset(width/2 + 30f, height/2 - lightSpacing)
    )
    drawCircle(
        color = nsColor,
        radius = 8f,
        center = Offset(width/2 + 30f, height/2 - lightSpacing)
    )

    drawCircle(
        color = Color.Black,
        radius = 12f,
        center = Offset(width/2 - 30f, height/2 + lightSpacing)
    )
    drawCircle(
        color = nsColor,
        radius = 8f,
        center = Offset(width/2 - 30f, height/2 + lightSpacing)
    )
}

// Predictive hour analytics graph (24h period)
private fun DrawScope.drawPeakAnalyticsGraph(width: Float, height: Float) {
    // Generate load curve (Congestion vs time-of-day)
    val points = listOf(
        Offset(0.0f, 0.2f),   // 00:00
        Offset(0.15f, 0.15f), // 04:00
        Offset(0.25f, 0.75f), // 07:00 (Peak load)
        Offset(0.35f, 0.90f), // 09:00 (Peak load)
        Offset(0.5f, 0.40f),  // 12:00
        Offset(0.6f, 0.50f),  // 14:00
        Offset(0.7f, 0.85f),  // 17:00 (Peak load)
        Offset(0.8f, 0.95f),  // 18:00 (Peak load)
        Offset(0.9f, 0.45f),  // 21:00
        Offset(1.0f, 0.25f)   // 24:00
    )

    // Draw graph gridlines
    for (i in 1..4) {
        val y = height * (i / 5f)
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
        )
    }

    // Draw curve path
    for (i in 0 until points.size - 1) {
        val startPt = points[i]
        val endPt = points[i + 1]
        
        val sx = startPt.x * width
        val sy = height - (startPt.y * height)
        val ex = endPt.x * width
        val ey = height - (endPt.y * height)

        drawLine(
            color = Color(0xFF851C80),
            start = Offset(sx, sy),
            end = Offset(ex, ey),
            strokeWidth = 4f
        )
        
        // Draw tiny gradient circles on nodes
        drawCircle(
            color = if (startPt.y > 0.7f) Color.Red else Color(0xFFC08A50),
            radius = 5f,
            center = Offset(sx, sy)
        )
    }
}
