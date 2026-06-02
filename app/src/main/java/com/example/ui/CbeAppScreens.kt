package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.TransactionRecord
import com.example.traffic.TrafficSimulatorScreen
import com.example.ui.theme.*
import java.io.File

@Composable
fun CbeAppContent(viewModel: CbeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val context = LocalContext.current

    // Handle system back press
    androidx.activity.compose.BackHandler(enabled = true) {
        val handled = viewModel.handleBackPress()
        if (!handled) {
            // Minimize or exit app
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.LOGIN -> LoginPhoneScreen(viewModel)
                AppScreen.PIN_ENTRY -> PinEntryScreen(viewModel)
                AppScreen.DASHBOARD -> DashboardScreen(viewModel)
                AppScreen.SEND_MONEY_FORM -> SendMoneyFormScreen(viewModel)
                AppScreen.SUCCESS_SUMMARY -> TransactionSuccessScreen(viewModel)
                AppScreen.TRANSACTION_MENU -> TransactionMenuScreen(viewModel)
                AppScreen.TRANSACTIONS_LIST -> TransactionsListScreen(viewModel)
                AppScreen.INVOICE_RECEIPT_VIEW -> InvoiceReceiptViewScreen(viewModel)
                AppScreen.TRAFFIC_PORTAL -> TrafficSimulatorScreen(onBack = { viewModel.handleBackPress() })
                AppScreen.SMS_INBOX -> SmsInboxScreen(viewModel)
                AppScreen.SMS_CHAT -> SmsChatScreen(viewModel)
            }
        }

        // Persistent "Quick Editor Overlay" toggle visible in upper corner on Success/Invoice screens
        if (currentScreen == AppScreen.SUCCESS_SUMMARY || currentScreen == AppScreen.INVOICE_RECEIPT_VIEW) {
            QuickSettingsFab(viewModel)
        }
    }
}

// ----------------------------------------------------------------------------------
// SCREEN 1: LOGIN (Image 3)
// ----------------------------------------------------------------------------------
@Composable
fun LoginPhoneScreen(viewModel: CbeViewModel) {
    val phone by viewModel.loginPhone.collectAsState()
    val pinError by viewModel.pinError.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Upper background curve
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.7f)
                quadraticTo(size.width * 0.5f, size.height, 0f, size.height * 0.7f)
                close()
            }
            drawPath(
                path = path,
                color = CbePurple
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language Picker (Top Left)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("English", color = Color.White, fontSize = 14.sp)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CBE Birr Custom Logo matching image 21.17.jpeg precisely
            CbeBirrPlusLogo()

            Spacer(modifier = Modifier.height(14.dp))
            Text("Welcome to CBEBirr plus app!", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("Login", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(80.dp))

            // Phone Field
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Phone number", color = CbeTextLight, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CbeGrey, RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(CbePurple, RoundedCornerShape(6.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text("+251", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        TextField(
                            value = phone.replace("+251", "").trim(),
                            onValueChange = { viewModel.loginPhone.value = "+251 " + it },
                            placeholder = { Text("945954856") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action button
            Button(
                onClick = { viewModel.navigateTo(AppScreen.PIN_ENTRY) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CbePurple),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text("Next", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // SIMULATED PIN EXCEPTION TOAST (Directly matches image 3: "Wrong PIN Please try again!")
            AnimatedVisibility(visible = pinError != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = pinError ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Row {
                Text("Don't have an account? ", color = CbeTextDark, fontSize = 14.sp)
                Text("Create Account", color = CbePurple, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ChatBubble, contentDescription = null, tint = CbePurple, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Do you have any question? Chatbot", color = CbeTextDark, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Two bottom brown buttons: USSD - Offline vs Exchange Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CbeGold),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.SettingsPhone, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("USSD - OFFLINE", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CbeGold),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exchange rate", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "© 2026 Commercial Bank of Ethiopia, All Rights Reserved 6.0.2 version.",
                color = CbeTextLight,
                fontSize = 10.sp
            )
        }
    }
}

// ----------------------------------------------------------------------------------
// SCREEN 2: PIN ENTRY (Image 4)
// ----------------------------------------------------------------------------------
@Composable
fun PinEntryScreen(viewModel: CbeViewModel) {
    val pinBuffer by viewModel.pinBuffer.collectAsState()
    val pinError by viewModel.pinError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CbeBackground)
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.handleBackPress() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CbeTextDark)
            }
            Text("English", color = CbeTextDark, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text("Login Authentication", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CbeTextDark)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Please enter valid PIN to continue the process", fontSize = 14.sp, color = CbeTextLight)

        Spacer(modifier = Modifier.height(30.dp))

        // Visual 4 Digit Buffers
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            for (i in 0 until 4) {
                val entered = pinBuffer.length > i
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .border(1.5.dp, if (entered) CbePurple else Color.Gray, RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (entered) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(CbePurple, CircleShape)
                        )
                    }
                }
            }
        }

        if (pinError != null) {
            Text(
                text = pinError ?: "",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Grid Keyboard list
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("BIO", "0", "DEL")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (key in row) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .clickable {
                                    when (key) {
                                        "DEL" -> {
                                            if (viewModel.pinBuffer.value.isNotEmpty()) {
                                                viewModel.pinBuffer.value =
                                                    viewModel.pinBuffer.value.dropLast(1)
                                            }
                                        }

                                        "BIO" -> {
                                            viewModel.triggerBiometricAuth()
                                        }

                                        else -> {
                                            viewModel.appendPin(key)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "DEL") {
                                Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = CbeTextDark)
                            } else if (key == "BIO") {
                                Icon(Icons.Default.Fingerprint, contentDescription = "Biometrics", tint = CbePurple, modifier = Modifier.size(28.dp))
                            } else {
                                Text(key, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CbeTextDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// SCREEN 3: DASHBOARD (Image 5)
// ----------------------------------------------------------------------------------
@Composable
fun DashboardScreen(viewModel: CbeViewModel) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val syncText by viewModel.syncStatus.collectAsState()
    var isBalanceVisible by remember { mutableStateOf(false) }
    var isRewardVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CbeBackground)
    ) {
        // Top Purple CBE Birr Bank card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CbePurple, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(6.dp), color = Color.White) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("CBE", color = CbePurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("CBEBirr Plus", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("ባለበት ሁሉ አለ!", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.TRAFFIC_PORTAL) }) {
                            Icon(Icons.Default.Traffic, contentDescription = "Traffic Optimizer", tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                        }
                        
                        // Dynamic Messages trigger envelope in header card
                        Box(
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppScreen.SMS_INBOX) }
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.MailOutline,
                                contentDescription = "SMS alerts feed",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .offset(x = 10.dp, y = (-4).dp)
                                    .background(Color.Red, CircleShape)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("99+", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("EN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Golden logo header segment (using high accuracy spiral emblem from 21.23.jpeg)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CommercialBankEmblem(modifier = Modifier, sizeDp = 38)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("የኢትዮጵያ ንግድ ባንክ", color = Color.White, fontSize = 13.sp)
                        Text("Commercial Bank of Ethiopia", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Light)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("+251945954***", color = Color.White, fontSize = 13.sp)
                Text(profile?.accountHolderName ?: "SURAFEL!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(20.dp))

                // Balance and Reward values
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Balance (ETB)", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { isBalanceVisible = !isBalanceVisible }
                            )
                        }
                        Text(
                            if (isBalanceVisible) "ETB ${profile?.balance ?: "25,300.50"}" else "******",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Reward (Pts)", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (isRewardVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { isRewardVisible = !isRewardVisible }
                            )
                        }
                        Text(
                            if (isRewardVisible) "${profile?.rewardPoints ?: 1680}" else "******",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Dashboard body containing service button cells
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mekedonia", fontSize = 14.sp, color = CbePurple, fontWeight = FontWeight.Bold)
                    Text("Transaction Detail", fontSize = 14.sp, color = CbePurple, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.TRANSACTION_MENU) }
                    )
                }
            }

            item {
                // Button grid layout
                val gridCells = listOf(
                    GridCell("Linked Bank Acct", Icons.Default.AccountBalance),
                    GridCell("Send Money", Icons.Default.Send, tag = "SEND_MONEY"),
                    GridCell("To CBE Acct", Icons.Default.Person),
                    GridCell("Air Time", Icons.Default.PhonelinkRing),
                    GridCell("Cash Out", Icons.Default.LocalAtm),
                    GridCell("Airtime Package", Icons.Default.BookmarkBorder),
                    GridCell("Scheduled Pay", Icons.Default.Alarm),
                    GridCell("MagicPay", Icons.Default.AutoAwesome)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (rowIdx in 0 until 2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (colIdx in 0 until 4) {
                                val cell = gridCells[rowIdx * 4 + colIdx]
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable {
                                            if (cell.tag == "SEND_MONEY") {
                                                viewModel.navigateTo(AppScreen.SEND_MONEY_FORM)
                                            } else {
                                                // other buttons trigger simulated action or alerts
                                                Toast.makeText(context, "${cell.title} clicked! Available in full CBE Plus version.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(cell.icon, contentDescription = "", tint = CbePurple, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(cell.title, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, lineHeight = 11.sp, color = CbeTextDark, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Amharic banner card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CbePurpleDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("በሲቢኤ ብር ፕላስ የባንክ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("ሂሳብዎን ያንቀሳቅሱ!", color = CbeGoldLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Extra actions row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = CbePurple)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Other Bank Transfer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CbeTextDark)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Wallet, contentDescription = null, tint = CbePurple)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Wallet & Micro Finance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CbeTextDark)
                        }
                    }
                }
            }
            
            // Sync status and metadata check
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LockClock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(syncText, color = Color.Gray, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Persistent floating overlay button for same-size QR code
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { viewModel.navigateTo(AppScreen.TRANSACTION_MENU) },
                color = CbePurple,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("QR Code", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Standard bottom nav
        NavigationBar(containerColor = CbePurple, contentColor = Color.White) {
            NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White) }, label = { Text("Home", color = Color.White, fontSize = 10.sp) })
            NavigationBarItem(selected = false, onClick = { viewModel.navigateTo(AppScreen.SEND_MONEY_FORM) }, icon = { Icon(Icons.Default.Payment, contentDescription = "Pay") }, label = { Text("Pay", fontSize = 10.sp) })
            NavigationBarItem(selected = false, onClick = { viewModel.navigateTo(AppScreen.TRAFFIC_PORTAL) }, icon = { Icon(Icons.Default.Traffic, contentDescription = "Traffic Optimizer") }, label = { Text("Planner", fontSize = 10.sp) })
            NavigationBarItem(selected = false, onClick = { viewModel.navigateTo(AppScreen.TRANSACTION_MENU) }, icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "MiniStatement") }, label = { Text("Stat.", fontSize = 10.sp) })
        }
    }
}

data class GridCell(val title: String, val icon: ImageVector, val tag: String = "")

// ----------------------------------------------------------------------------------
// SCREEN 4: SEND MONEY FORM (Image 1)
// ----------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyFormScreen(viewModel: CbeViewModel) {
    val recAcc by viewModel.receiverAccountNumber.collectAsState()
    val amount by viewModel.sendAmount.collectAsState()
    val reason by viewModel.sendReason.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CBE Send Money", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleBackPress() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) }
                    Text("EN", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CbePurple)
            )
        },
        containerColor = CbeBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Recipient Account
                    Text("* Receivers Account Number", color = CbePurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = recAcc,
                        onValueChange = { viewModel.receiverAccountNumber.value = it },
                        placeholder = { Text("Receivers Account Number") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = CbePurple) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CbePurple,
                            unfocusedBorderColor = CbeGrey
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Amount
                    Text("* Amount", color = CbePurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { viewModel.sendAmount.value = it },
                        placeholder = { Text("Amount (ETB)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = CbePurple) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CbePurple,
                            unfocusedBorderColor = CbeGrey
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Reason
                    Text("Reason", color = CbePurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { viewModel.sendReason.value = it },
                        placeholder = { Text("Reason") },
                        leadingIcon = { Icon(Icons.Default.QuestionMark, contentDescription = null, tint = CbePurple) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CbePurple,
                            unfocusedBorderColor = CbeGrey
                        )
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = { viewModel.executeSendMoney() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CbePurple),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text("Send Money", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// SCREEN 5: SUCCESS SUMMARY (Image 2)
// ----------------------------------------------------------------------------------
@Composable
fun TransactionSuccessScreen(viewModel: CbeViewModel) {
    val tx by viewModel.selectedTransaction.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CbeBackground)
    ) {
        // Upper custom header actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(CbePurple)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                tx?.let { viewModel.generatePdfReceipt(it) }
            }) {
                Icon(Icons.Default.FileDownload, contentDescription = "Download Receipt File", tint = Color.White)
            }
            Text("Transaction Details", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success banner block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CbePurple)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Thank you!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Success", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Message", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Custom compiled message containing real editable params
                Text(
                    text = "ETB ${tx?.amount ?: "200"} debited from ${tx?.senderName ?: "SURAFEL ABRIHA HAILE"} for ${tx?.receiverName ?: "Maereg Abreha Abera"} on ${tx?.date ?: "31 May 2026"} with Transaction ID: ${tx?.transactionId ?: "DEV81EJHWNC"} via CBEBirr mobile app's Send Money.",
                    color = CbeTextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // CUSTOM CONSTRUCTED DYNAMIC QR CODE matching sizing of Image 2
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(1.dp, CbeGrey, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawMockQrCode(0f, 0f, size.width)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CBE Birr logo footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEEEEEE))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(modifier = Modifier.size(24.dp), shape = RoundedCornerShape(4.dp), color = CbePurple) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Birr", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Commercial Bank of Ethiopia", color = CbeTextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("you will receive a confirmation SMS shortly!", color = Color.Gray, fontSize = 12.sp, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CbePurple),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("Back to home", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    tx?.let { viewModel.selectTransactionReceipt(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CbePurple),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Get Receipt", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMockQrCode(x: Float, y: Float, size: Float) {
    val gridCount = 29
    val pixelSize = size / gridCount
    val colorBlack = Color.Black
    val colorWhite = Color.White

    // Fill white background for cleaner high-contrast barcode display
    drawRect(colorWhite, Offset(0f, 0f), Size(size, size))

    fun drawFinder(fx: Float, fy: Float) {
        drawRect(colorBlack, Offset(fx, fy), Size(pixelSize * 7, pixelSize * 7))
        drawRect(colorWhite, Offset(fx + pixelSize, fy + pixelSize), Size(pixelSize * 5, pixelSize * 5))
        drawRect(colorBlack, Offset(fx + pixelSize * 2, fy + pixelSize * 2), Size(pixelSize * 3, pixelSize * 3))
    }

    // 1. Draw three outer corner finder patterns
    drawFinder(0f, 0f)
    drawFinder((gridCount - 7) * pixelSize, 0f)
    drawFinder(0f, (gridCount - 7) * pixelSize)

    // 2. Draw alignment pattern in the bottom right (centered at row 22, col 22)
    val ax = 20 * pixelSize
    val ay = 20 * pixelSize
    drawRect(colorBlack, Offset(ax, ay), Size(pixelSize * 5, pixelSize * 5))
    drawRect(colorWhite, Offset(ax + pixelSize, ay + pixelSize), Size(pixelSize * 3, pixelSize * 3))
    drawRect(colorBlack, Offset(ax + pixelSize * 2, ay + pixelSize * 2), Size(pixelSize, pixelSize))

    // 3. Draw deterministic high-accuracy timing and code bits
    val random = java.util.Random(99)
    for (row in 0 until gridCount) {
        for (col in 0 until gridCount) {
            // Skip finder quiet zones
            val isTopLeftFinderOrQuiet = row <= 7 && col <= 7
            val isTopRightFinderOrQuiet = row <= 7 && col >= (gridCount - 8)
            val isBottomLeftFinderOrQuiet = row >= (gridCount - 8) && col <= 7
            
            if (isTopLeftFinderOrQuiet || isTopRightFinderOrQuiet || isBottomLeftFinderOrQuiet) {
                continue
            }

            // Skip bottom-right alignment pattern
            if (row >= 20 && row <= 24 && col >= 20 && col <= 24) {
                continue
            }

            // Skip middle center logo zone to facilitate premium overlay
            if (row >= 11 && row <= 17 && col >= 11 && col <= 17) {
                continue
            }

            // Horizontal timing pattern row 6
            if (row == 6) {
                if (col % 2 == 0) {
                    drawRect(colorBlack, Offset(col * pixelSize, row * pixelSize), Size(pixelSize, pixelSize))
                }
                continue
            }

            // Vertical timing pattern col 6
            if (col == 6) {
                if (row % 2 == 0) {
                    drawRect(colorBlack, Offset(col * pixelSize, row * pixelSize), Size(pixelSize, pixelSize))
                }
                continue
            }

            // Balanced probability thresholding to match standard look of the sent image
            val isDark = if (row % 3 == 0 && col % 3 == 0) {
                random.nextFloat() > 0.35f
            } else {
                random.nextFloat() > 0.50f
            }

            if (isDark) {
                drawRect(colorBlack, Offset(col * pixelSize, row * pixelSize), Size(pixelSize, pixelSize))
            }
        }
    }

    // Mid center custom CBE Birr Plus Logo card (replacing the plain purple box with the precise logo from your requested image)
    val centerPixels = pixelSize * 6.5f
    val cx = (size - centerPixels) / 2
    val cy = (size - centerPixels) / 2

    // 1. Draw outer white card container
    drawRoundRect(
        color = colorWhite,
        topLeft = Offset(cx, cy),
        size = Size(centerPixels, centerPixels),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pixelSize * 1.5f, pixelSize * 1.5f)
    )

    // 2. Draw the main purple/violet logo card body (slightly inset)
    val cardSize = centerPixels - pixelSize * 1.2f
    val ccx = cx + pixelSize * 0.6f
    val ccy = cy + pixelSize * 0.6f
    drawRoundRect(
        color = Color(0xFF6A1A78), // Sweet CBE purple/violet base color matching your requested image
        topLeft = Offset(ccx, ccy),
        size = Size(cardSize, cardSize),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pixelSize * 1.0f, pixelSize * 1.0f)
    )

    // 3. Draw thin white border inside the card
    drawRoundRect(
        color = Color.White.copy(alpha = 0.8f),
        topLeft = Offset(ccx + 0.35f * pixelSize, ccy + 0.35f * pixelSize),
        size = Size(cardSize - 0.7f * pixelSize, cardSize - 0.7f * pixelSize),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pixelSize * 0.7f, pixelSize * 0.7f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.35f * pixelSize)
    )

    // 4. Draw diagonal light-shine background accent
    val shinePath = androidx.compose.ui.graphics.Path().apply {
        moveTo(ccx, ccy)
        lineTo(ccx + cardSize * 0.45f, ccy)
        lineTo(ccx + cardSize * 0.22f, ccy + cardSize)
        lineTo(ccx, ccy + cardSize)
        close()
    }
    drawPath(shinePath, color = Color.White.copy(alpha = 0.08f))

    // 5. Draw "CBE" on top-left and "Plus" on top-right, "Birr" in center block, and wheat flame above 'i' using native Canvas text drawing
    drawContext.canvas.nativeCanvas.apply {
        // "CBE" text
        val pCbe = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = cardSize * 0.16f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }
        drawText("CBE", ccx + cardSize * 0.12f, ccy + cardSize * 0.26f, pCbe)

        // "Plus" gold-accented text
        val pPlus = android.graphics.Paint().apply {
            color = 0xFFE9C5A2.toInt() // Gold color
            textSize = cardSize * 0.14f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }
        drawText("Plus", ccx + cardSize * 0.62f, ccy + cardSize * 0.26f, pPlus)

        // "Birr" italic bold central black text
        val pBirr = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = cardSize * 0.42f
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD_ITALIC)
            isAntiAlias = true
        }
        val textWidth = pBirr.measureText("Birr")
        val bX = ccx + (cardSize - textWidth) / 2f - cardSize * 0.02f
        val bY = ccy + cardSize * 0.66f
        drawText("Birr", bX, bY, pBirr)

        // Amharic slogan (ባለበት ሁሉ አለ) centered at the bottom
        val pSlogan = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = cardSize * 0.12f
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            isAntiAlias = true
        }
        val sloganText = "ባለበት ሁሉ አለ"
        val sloganWidth = pSlogan.measureText(sloganText)
        val sX = ccx + (cardSize - sloganWidth) / 2f
        val sY = ccy + cardSize * 0.88f
        drawText(sloganText, sX, sY, pSlogan)
    }

    // 6. Natural golden wheat petal symbol above 'i' on top of the "Birr" text
    val flameSize = cardSize * 0.2f
    val fxOffset = ccx + cardSize * 0.58f
    val fyOffset = ccy + cardSize * 0.35f
    val petalPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(fxOffset + flameSize * 0.5f, fyOffset + flameSize)
        cubicTo(
            fxOffset, fyOffset + flameSize * 0.5f,
            fxOffset, fyOffset,
            fxOffset + flameSize * 0.5f, fyOffset
        )
        cubicTo(
            fxOffset + flameSize, fyOffset,
            fxOffset + flameSize, fyOffset + flameSize * 0.5f,
            fxOffset + flameSize * 0.5f, fyOffset + flameSize
        )
        close()
    }
    drawPath(petalPath, color = Color(0xFFFFD54F)) // Sweet golden yellow flame
}

// ----------------------------------------------------------------------------------
// SCREEN 6: TRANSACTION MENU SELECTOR (Image 7)
// ----------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionMenuScreen(viewModel: CbeViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Detail", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleBackPress() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CbePurple)
            )
        },
        containerColor = CbeBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Option 1
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.TRANSACTIONS_LIST) },
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = null,
                        tint = CbePurple,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recent Transactions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CbeTextDark)
                        Text("Provide the latest transactions.", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CbeTextDark)
                }
            }

            // Option 2
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.TRANSACTIONS_LIST) },
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = CbePurple,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mini Statement", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CbeTextDark)
                        Text("Provide transactions by date.", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CbeTextDark)
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// SCREEN 7: TRANSACTIONS LIST (Image 6)
// ----------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(viewModel: CbeViewModel) {
    val txs by viewModel.allTransactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recent Transactions", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleBackPress() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Lists", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CbePurple)
            )
        },
        containerColor = CbeBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(txs) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectTransactionReceipt(item) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Direction Circle indicator (Up-Red or Down-Green)
                        val isDebit = item.transactionType == "DEBIT"
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (isDebit) CbeDebitRed.copy(alpha = 0.15f) else CbeSuccessGreen.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDebit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isDebit) CbeDebitRed else CbeSuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.reason,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CbeTextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.date} ${item.time}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%.2f", item.amount),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDebit) CbeDebitRed else CbeSuccessGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// SCREEN 8: VAT RECEIPT VIEWER WITH LIVE EDITORS (Image 8)
// ----------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceReceiptViewScreen(viewModel: CbeViewModel) {
    val tx by viewModel.selectedTransaction.collectAsState()
    val context = LocalContext.current
    val pdfFile by viewModel.pdfFileState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Receipt Viewer", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleBackPress() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        tx?.let { viewModel.generatePdfReceipt(it) }
                        
                        pdfFile?.let { file ->
                            try {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(Intent.createChooser(intent, "Open Receipt PDF"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "PDF saved to Downloads folder!", Toast.LENGTH_LONG).show()
                            }
                        } ?: run {
                            Toast.makeText(context, "Preparing PDF file...", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Download/Share Receipt PDF", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CbePurple)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Render Invoice using Compose scrolling layout to mimic A4 paper
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Outer Receipt Canvas frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray)
                        .background(Color.White)
                ) {
                    Column {
                        // 1. Purple Header Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CbePurple)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CommercialBankEmblem(modifier = Modifier, sizeDp = 44)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Commercial Bank of Ethiopia", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("VAT Invoice/ Customer Receipt", color = Color.White, fontSize = 12.sp)
                                Text("CBEBirr", color = Color.White, fontSize = 10.sp)
                            }
                            Surface(modifier = Modifier.size(34.dp), shape = RoundedCornerShape(4.dp), color = Color.White) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Birr", color = CbePurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Gold line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(CbeGold)
                        )

                        // 2. Col data
                        Row(modifier = Modifier.padding(8.dp)) {
                            // Company info column
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(0.5.dp, Color.LightGray)
                                    .padding(6.dp)
                            ) {
                                Text("Company Address & Info", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CbePurple)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Country: Ethiopia\nCity: Addis Ababa\nAddress: Ras Desta Damtew St, 01, Kirkos\nPostal Code: 255\nTIN: 0000006966\nVAT Registration No: 011140\nVAT Registration Date: 01/01/2003", fontSize = 8.sp, color = Color.DarkGray, lineHeight = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // Customer info column
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(0.5.dp, Color.LightGray)
                                    .padding(6.dp)
                            ) {
                                Text("Customer Information", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CbePurple)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Customer Name: ${tx?.senderName ?: "SURAFEL ABRIHA HAILE"}\nRegion: Addis Ababa\nCity: Addis Ababa\nSub city: Kirkos\nWereda: 03\nTAX ID: -", fontSize = 8.sp, color = Color.DarkGray, lineHeight = 11.sp)
                            }
                        }

                        // 3. Transaction Details List Box with overlaid purple CBE stamp
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CbePurple)
                                    .padding(12.dp)
                            ) {
                                Text("Transaction Information", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CbePurple)
                                Divider(color = CbePurple, modifier = Modifier.padding(vertical = 4.dp))

                                val data = listOf(
                                    "Debit Account" to "251945954856 - ${tx?.senderName ?: "SURAFEL ABRIHA"}",
                                    "Credit Account" to "${tx?.receiverAccount ?: "1000****8222"}",
                                    "Receiver Name" to "${tx?.receiverName ?: "Maereg Abreha"}",
                                    "Order ID" to "${tx?.orderId ?: "FT2615217"}",
                                    "Transaction Status" to "Completed",
                                    "Reference" to "${tx?.reason ?: "Send Money"}"
                                )
                                for ((key, value) in data) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(key, fontSize = 9.sp, color = Color.DarkGray, modifier = Modifier.weight(0.4f))
                                        Text(value, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(0.6f))
                                    }
                                }
                            }

                            // VISUAL HIGH ACCURACY PURPLE CIRCULAR CBE STAMP IN BACKGROUND (Transparent overlapping) (matching 21.27.jpeg)
                            CbeOfficialDistressedStamp(modifier = Modifier.rotate(-15f))
                        }

                        // 4. Details Table
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CbePurple)
                                    .padding(6.dp)
                            ) {
                                Text("Receipt Number", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("Date & Time", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                Text("Amount (ETB)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text("${tx?.transactionId ?: "DEV01"}", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("${tx?.date ?: "31-05-2026"} ${tx?.time ?: "22:54"}", fontSize = 9.sp, modifier = Modifier.weight(1.2f))
                                Text("${tx?.amount ?: "150.00"}", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                            }
                            Divider()
                        }

                        // Breakdown and Total text word
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Total Amount in word:", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("ETB ${tx?.amount ?: "One Hundred Sixty"} Only", fontSize = 8.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Payment Reason: ${tx?.reason ?: ""}", fontSize = 8.sp, color = Color.Gray)
                            }
                            
                            // Right side breakdown
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Paid Amount:", fontSize = 8.sp)
                                    Text("${tx?.amount ?: "150.00"}", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Service Charge:", fontSize = 8.sp)
                                    Text("0.00", fontSize = 8.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("VAT Account:", fontSize = 8.sp)
                                    Text("0.00", fontSize = 8.sp)
                                }
                                Divider(color = CbePurple, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Total Paid Amount:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CbePurple)
                                    Text("${tx?.amount ?: "150.00"}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CbePurple)
                                }
                            }
                        }

                        // Slogan & fine print
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("The Bank you can always rely on!", color = CbePurpleDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("© 2026 Commercial Bank of Ethiopia All rights reserved", fontSize = 8.sp, color = Color.Gray)
                        }

                        // Custom QR Code bottom alignment
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .border(1.dp, Color.LightGray)
                                    .padding(4.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawMockQrCode(0f, 0f, size.width)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        tx?.let { viewModel.generatePdfReceipt(it) }
                        tx?.let {
                            val msg = "VAT Invoice PDF exported to cache folder! Open with Share icon above."
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CbePurple),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save / Download Receipt PDF", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// COMPONENT: QUICK SLIDEOUT SETTINGS PANEL (Image 8 editor request)
// ----------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsFab(viewModel: CbeViewModel) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = CbeGold,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.EditNote, contentDescription = "Edit Metadata Names")
        }
    }

    if (showDialog) {
        val accountHolder by viewModel.editAccountHolderName.collectAsState()
        val sender by viewModel.editSenderName.collectAsState()
        val receiverName by viewModel.editReceiverName.collectAsState()
        val amount by viewModel.editTransactionAmount.collectAsState()
        val txId by viewModel.editTransactionId.collectAsState()
        val dateString by viewModel.editDateString.collectAsState()
        val timeString by viewModel.editTimeString.collectAsState()

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Metadata Quick Editor Panel", color = CbePurple, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Text("Edit the Sender and Receiver values to dynamically recompile the billing message and PDF receipt", fontSize = 11.sp, color = Color.Gray)
                    }
                    item {
                        OutlinedTextField(
                            value = accountHolder,
                            onValueChange = { viewModel.editAccountHolderName.value = it },
                            label = { Text("Account Holder Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = sender,
                            onValueChange = { viewModel.editSenderName.value = it },
                            label = { Text("Sender Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = receiverName,
                            onValueChange = { viewModel.editReceiverName.value = it },
                            label = { Text("Receiver Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { viewModel.editTransactionAmount.value = it },
                            label = { Text("Amount (ETB)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = txId,
                            onValueChange = { viewModel.editTransactionId.value = it },
                            label = { Text("Transaction ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = dateString,
                            onValueChange = { viewModel.editDateString.value = it },
                            label = { Text("Date-time Date line") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = timeString,
                            onValueChange = { viewModel.editTimeString.value = it },
                            label = { Text("Time line") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveEditSettings()
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CbePurple)
                ) {
                    Text("Apply & Sync")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==================================================================================
// CUSTOM CBE BRAND LOGOS & STAMPS (Based on users image selections 21.17, 21.23, 21.27)
// ==================================================================================

@Composable
fun CbeBirrPlusLogo(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .width(220.dp)
            .height(115.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.8f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6A1A78)) // Violet/Purple base color from 21.17.jpeg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // White-grey transparent horizontal shine accent
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width * 0.45f, 0f)
                    lineTo(size.width * 0.28f, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color.White.copy(alpha = 0.05f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Segment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CBE",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Plus",
                        color = Color(0xFFE9C5A2), // CBE Light Gold accent
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Center Bold "Birr" with the Gold Flame wheat segment floating near "i"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Birr",
                            color = Color.Black,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }

                    // Floating Flame Wheat Petal Symbol above the letter 'i'
                    Canvas(
                        modifier = Modifier
                            .size(20.dp)
                            .offset(x = 18.dp, y = (-16).dp)
                    ) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width * 0.5f, size.height)
                            cubicTo(0f, size.height * 0.5f, 0f, 0f, size.width * 0.5f, 0f)
                            cubicTo(size.width, 0f, size.width, size.height * 0.5f, size.width * 0.5f, size.height)
                            close()
                        }
                        drawPath(path, color = Color(0xFFFFD54F)) // Elegant golden yellow
                    }
                }

                // Slogan footer (ባለበት ሁሉ አለ!)
                Text(
                    text = "ባለበት ሁሉ አለ!",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CommercialBankEmblem(modifier: Modifier = Modifier, sizeDp: Int = 36) {
    Canvas(modifier = modifier.size(sizeDp.dp)) {
        val w = size.width
        val h = size.height
        val goldColor = Color(0xFFE9C5A2) // matching 21.23.jpeg tone

        // Center medallion
        drawCircle(color = goldColor, radius = w * 0.16f, style = Stroke(w * 0.05f))
        drawCircle(color = goldColor, radius = w * 0.08f)

        // Wing 1: Upper-Right curved concentric bands
        drawArc(
            color = goldColor,
            startAngle = 330f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.7f, h * 0.7f),
            style = Stroke(w * 0.05f)
        )
        drawArc(
            color = goldColor,
            startAngle = 320f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(w * 0.22f, h * 0.22f),
            size = Size(w * 0.56f, h * 0.56f),
            style = Stroke(w * 0.05f)
        )
        drawArc(
            color = goldColor,
            startAngle = 310f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = Offset(w * 0.28f, h * 0.28f),
            size = Size(w * 0.44f, h * 0.44f),
            style = Stroke(w * 0.05f)
        )

        // Wing 2: Bottom-Left curved bands
        drawArc(
            color = goldColor,
            startAngle = 150f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.7f, h * 0.7f),
            style = Stroke(w * 0.05f)
        )
        drawArc(
            color = goldColor,
            startAngle = 140f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(w * 0.22f, h * 0.22f),
            size = Size(w * 0.56f, h * 0.56f),
            style = Stroke(w * 0.05f)
        )

        // Wing 3: Top-Left curved bands
        drawArc(
            color = goldColor,
            startAngle = 240f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.12f, h * 0.12f),
            size = Size(w * 0.76f, h * 0.76f),
            style = Stroke(w * 0.05f)
        )
        drawArc(
            color = goldColor,
            startAngle = 230f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.25f),
            size = Size(w * 0.6f, h * 0.6f),
            style = Stroke(w * 0.05f)
        )
    }
}

@Composable
fun CbeOfficialDistressedStamp(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(130.dp)
            .rotate(-12f)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        val stampColor = Color(0x9E581373) // High accuracy violet stamp ink

        // Outer Double circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Double line outer circle
            drawCircle(color = stampColor, radius = w * 0.48f, style = Stroke(width = 2.4f))
            drawCircle(color = stampColor, radius = w * 0.45f, style = Stroke(width = 1.1f))
            // Inner circle
            drawCircle(color = stampColor, radius = w * 0.24f, style = Stroke(width = 1.5f))

            // Draw schematic CBE spiral wing centered
            drawArc(
                color = stampColor,
                startAngle = 130f, sweepAngle = 100f, useCenter = false,
                topLeft = Offset(w * 0.38f, h * 0.38f), size = Size(w * 0.24f, h * 0.24f),
                style = Stroke(width = 1.5f)
            )
            drawArc(
                color = stampColor,
                startAngle = 310f, sweepAngle = 100f, useCenter = false,
                topLeft = Offset(w * 0.38f, h * 0.38f), size = Size(w * 0.24f, h * 0.24f),
                style = Stroke(width = 1.5f)
            )

            // Faded asterisk dots
            drawCircle(color = stampColor, radius = 2.2f, center = Offset(w * 0.12f, h * 0.5f))
            drawCircle(color = stampColor, radius = 2.2f, center = Offset(w * 0.88f, h * 0.5f))
        }

        // Top Amharic Text Characters (ባለበት_ሁሉ_አለ)
        val amharicCharacters = "የኢትዮጵያ ንግድ ባንክ".toList()
        amharicCharacters.forEachIndexed { idx, char ->
            val angle = -120f + (idx * 11.5f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(angle),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = char.toString(),
                    color = stampColor,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Bottom English Text Characters (COMMERCIAL BANK OF ETHIOPIA)
        val englishCharacters = "COMMERCIAL BANK OF ETHIOPIA".toList()
        englishCharacters.forEachIndexed { idx, char ->
            val angle = 180f - (englishCharacters.size * 5f / 2) + (idx * 5.2f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(angle),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = char.toString(),
                    color = stampColor,
                    fontSize = 6.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Inked Inner Date and Branch Label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .border(1.dp, stampColor, RoundedCornerShape(1.dp))
                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
                    .background(Color.White.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "31 MAY 2026",
                    color = stampColor,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }
            Text(
                text = "CBE BIRR",
                color = stampColor,
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}


// ==================================================================================
// GOOGLE MESSAGES CENTRALIZED CLIENT: INBOX & CHAT (SMS message panel request)
// ==================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsInboxScreen(viewModel: CbeViewModel) {
    val contacts by viewModel.smsContacts.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF13131A))
            )
        },
        containerColor = Color(0xFF13131A) // Premium Google messages dark mode base
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            
            // Search / Top banner simulating Google Messages
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF202128), RoundedCornerShape(26.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Search images and conversations...", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Surface(
                    modifier = Modifier.size(26.dp).clip(CircleShape),
                    color = Color(0xFFE9C5A2)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("S", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pills/Tabs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(color = Color(0xFFE9C5A2), shape = RoundedCornerShape(16.dp)) {
                    Text("Inbox 99+", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                }
                Surface(color = Color(0xFF202128), shape = RoundedCornerShape(16.dp)) {
                    Text("Transactions", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Alerts inbox items
            LazyColumn {
                items(contacts) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.activeSmsSenderId.value = item.senderId
                                viewModel.navigateTo(AppScreen.SMS_CHAT)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored Circle Head Avatar
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = Color(item.avatarColor)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(item.initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Message previews
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.displayName,
                                    color = Color.White,
                                    fontWeight = if (item.unread) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = item.relativeTime,
                                    color = if (item.unread) Color(0xFFE9C5A2) else Color.Gray,
                                    fontWeight = if (item.unread) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.lastMessageShort,
                                color = if (item.unread) Color.White.copy(alpha = 0.9f) else Color.Gray,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (item.unread) {
                            Spacer(modifier = Modifier.width(6.dp))
                            // Blue/Gold Notification Unread circle badge
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(item.notificationColor), CircleShape)
                            )
                        }
                    }
                    Divider(color = Color.White.copy(alpha = 0.08f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsChatScreen(viewModel: CbeViewModel) {
    val senderId by viewModel.activeSmsSenderId.collectAsState()
    val allSmsMap by viewModel.smsMessages.collectAsState()
    val currentMessages = allSmsMap[senderId] ?: emptyList()
    val contacts by viewModel.smsContacts.collectAsState()
    val contact = contacts.firstOrNull { it.senderId == senderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Color(contact?.avatarColor ?: 0xFF851C80)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    contact?.initials ?: "CB",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(contact?.displayName ?: "Cbe Birr", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF00BFA5), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Online", color = Color(0xFF00BFA5), fontSize = 10.sp)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.SMS_INBOX) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color.White) }
                    IconButton(onClick = {}) { Icon(Icons.Default.Call, contentDescription = null, tint = Color.White) }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1E1E24))
            )
        },
        containerColor = Color(0xFF121214) // Pure dark background mimicking screenshot 4
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chat contents
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    // Verification header row
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "End-to-end encrypted message thread\nwith verified banker shortcode \"916\"",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }

                items(currentMessages) { sms ->
                    // Chat Bubble block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            modifier = Modifier
                                .widthIn(max = 290.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF23252E) // Dark slate bubble color
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Render SMS text with clickable blue links to view receipts
                                SmsTextRenderer(body = sms.body, transactionRecord = sms.transactionRecord, viewModel = viewModel)
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sms.time,
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Locked reply bar matching standard messages block "Replying is not supported by this sender"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E24))
                    .padding(vertical = 14.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Replying is disabled for this verified shortcode.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun SmsTextRenderer(body: String, transactionRecord: TransactionRecord?, viewModel: CbeViewModel) {
    if (body.contains("https://cbepay1.cbe.com.et/aureceipt")) {
        // Parse segments to render the link in bright blue, underline, and clickable!
        val parts = body.split("https://cbepay1.cbe.com.et/aureceipt")
        val prefix = parts.getOrNull(0) ?: ""
        val suffix = if (parts.size > 1) "https://cbepay1.cbe.com.et/aureceipt" + parts[1] else ""
        
        Column {
            Text(text = prefix, color = Color.White, fontSize = 13.sp, lineHeight = 17.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .background(CbePurple.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, CbePurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        // Tapping the URL takes the user directly to the transaction PDF receipt viewer!
                        val tx = transactionRecord ?: viewModel.allTransactions.value.firstOrNull()
                        if (tx != null) {
                            viewModel.selectTransactionReceipt(tx)
                        } else {
                            viewModel.navigateTo(AppScreen.TRANSACTIONS_LIST)
                        }
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tap to View Official CBE Invoice",
                    color = Color(0xFF64B5F6), 
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                )
            }
        }
    } else {
        Text(text = body, color = Color.White, fontSize = 13.sp, lineHeight = 17.sp)
    }
}
