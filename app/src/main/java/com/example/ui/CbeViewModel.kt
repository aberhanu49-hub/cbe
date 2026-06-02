package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.PdfReceiptGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    LOGIN,               // Image 3: Login (Phone)
    PIN_ENTRY,           // Image 4: PIN Keyboard Authenticator
    DASHBOARD,           // Image 5: CBEBirr main dashboard
    SEND_MONEY_FORM,     // Image 1: Input details
    SUCCESS_SUMMARY,     // Image 2: Success Transaction message with custom QR
    TRANSACTION_MENU,    // Image 7: Recent Transactions vs Mini statement trigger
    TRANSACTIONS_LIST,   // Image 6: Recent Transactions List
    INVOICE_RECEIPT_VIEW, // Image 8: PDF Invoice / Customer Receipt & Editor
    TRAFFIC_PORTAL,      // Urban Traffic flow simulation controls
    SMS_INBOX,           // Google Messages style SMS List
    SMS_CHAT             // Dark themed Google Messages CBE Birr Chat
}

data class SmsContact(
    val senderId: String,
    val displayName: String,
    val initials: String,
    val avatarColor: Long, // color representation
    val lastMessageShort: String,
    val relativeTime: String,
    val unread: Boolean = false,
    val notificationColor: Long = 0xFF2196F3
)

data class SmsMessage(
    val senderId: String,
    val body: String,
    val time: String,
    val isSentByMe: Boolean = false,
    val transactionRecord: TransactionRecord? = null // associated transaction if any
)

class CbeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CbeRepository(db.cbeDao())

    // UI Backstack for state navigation
    val screenBackstack = Stack<AppScreen>().apply { push(AppScreen.LOGIN) }
    val currentScreen = MutableStateFlow<AppScreen>(AppScreen.LOGIN)

    // User Profile flow from Room
    val userProfile: StateFlow<UserProfile?> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Transactions flow from Room
    val allTransactions: StateFlow<List<TransactionRecord>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Form fields for Send Money Screen
    val receiverAccountNumber = MutableStateFlow("")
    val sendAmount = MutableStateFlow("")
    val sendReason = MutableStateFlow("")

    // Current Selected Transaction for displaying receipt
    val selectedTransaction = MutableStateFlow<TransactionRecord?>(null)

    // Edit settings fields
    val editAccountHolderName = MutableStateFlow("SURAFEL ABRIHA HAILE")
    val editSenderName = MutableStateFlow("SURAFEL ABRIHA HAILE")
    val editReceiverName = MutableStateFlow("Maereg Abreha Abera")
    val editReceiverAccount = MutableStateFlow("1000****8222 - CBE")
    val editTransactionAmount = MutableStateFlow("150.00")
    val editTransactionId = MutableStateFlow("DEV01EJOOM6")
    val editDateString = MutableStateFlow("2026-05-31")
    val editTimeString = MutableStateFlow("22:54")

    // Error messages and general notifications
    val pinError = MutableStateFlow<String?>(null)
    val syncStatus = MutableStateFlow("Encrypted Cloud Sync is Active")
    val pdfFileState = MutableStateFlow<File?>(null)

    // SMS Alerts and Text message panels matching Photos
    val activeSmsSenderId = MutableStateFlow("CBE_BIRR")
    val smsContacts = MutableStateFlow<List<SmsContact>>(emptyList())
    val smsMessages = MutableStateFlow<Map<String, List<SmsMessage>>>(emptyMap())
    val wrongPinCount = MutableStateFlow(0)

    // Phone / PIN state
    val loginPhone = MutableStateFlow("+251 945954856")
    val pinBuffer = MutableStateFlow("")

    init {
        // Seed default profile and transaction data if database is empty
        viewModelScope.launch {
            val existingProfile = repository.getProfileDirect()
            if (existingProfile == null) {
                repository.updateProfile(UserProfile())
            } else {
                editAccountHolderName.value = existingProfile.accountHolderName
                editSenderName.value = existingProfile.accountHolderName
            }

            // One-off check to see if database has transactions, with separate seeding
            val initialList = repository.allTransactions.first()
            if (initialList.isEmpty()) {
                seedDefaultTransactions()
            }

            // Independent observation to keep the SMS alerts feed synchronized
            repository.allTransactions.collect { list ->
                refreshSmsInbox(list)
            }
        }
    }

    private suspend fun seedDefaultTransactions() {
        val defaultList = listOf(
            TransactionRecord(
                transactionId = "DEV01EJOOM6",
                senderName = "SURAFEL ABRIHA HAILE",
                receiverName = "Maereg Abreha Abera",
                receiverAccount = "1000****8222 - CBE",
                amount = 150.00,
                date = "31 May 2026",
                time = "22:54",
                reason = "TransferfromMMToBank by Customer to Org API",
                transactionType = "DEBIT",
                orderId = "FT2615217L66"
            ),
            TransactionRecord(
                transactionId = "ATMCASH01",
                senderName = "SURAFEL ABRIHA HAILE",
                receiverName = "Self (ATM Cash Out)",
                receiverAccount = "ATM Voucher ID: 195285",
                amount = 200.00,
                date = "31 May 2026",
                time = "21:56",
                reason = "ATM Cash Out With Voucher",
                transactionType = "DEBIT",
                orderId = "ATM99EJS81H"
            ),
            TransactionRecord(
                transactionId = "DEV81EJHWNC",
                senderName = "SURAFEL ABRIHA HAILE",
                receiverName = "Maereg Abreha Abera",
                receiverAccount = "1000****8222 - CBE",
                amount = 200.00,
                date = "31 May 2026",
                time = "20:52",
                reason = "Send Money",
                transactionType = "DEBIT",
                orderId = "FT2615210K12"
            ),
            TransactionRecord(
                transactionId = "SEND100",
                senderName = "SURAFEL ABRIHA HAILE",
                receiverName = "Maereg Abreha Abera",
                receiverAccount = "1000****8222 - CBE",
                amount = 100.00,
                date = "31 May 2026",
                time = "20:32",
                reason = "Send Money",
                transactionType = "DEBIT",
                orderId = "FT2615201A39"
            ),
            TransactionRecord(
                transactionId = "MMBANK490",
                senderName = "SURAFEL ABRIHA HAILE",
                receiverName = "Maereg Abreha Abera",
                receiverAccount = "1000****8222 - CBE",
                amount = 490.00,
                date = "31 May 2026",
                time = "13:07",
                reason = "TransferfromMMToBank by Customer to Org API",
                transactionType = "DEBIT",
                orderId = "FT2615112G45"
            ),
            TransactionRecord(
                transactionId = "MMBANK1800",
                senderName = "SURAFEL ABRIHA HAILE",
                receiverName = "Maereg Abreha Abera",
                receiverAccount = "1000****8222 - CBE",
                amount = 1800.00,
                date = "31 May 2026",
                time = "11:48",
                reason = "TransferfromMMToBank by Customer to Org API",
                transactionType = "DEBIT",
                orderId = "FT2615099P18"
            ),
            TransactionRecord(
                transactionId = "EMONEY4500",
                senderName = "SYSTEM CASHIER",
                receiverName = "SURAFEL ABRIHA HAILE",
                receiverAccount = "251945954856 - CBE",
                amount = 4500.00,
                date = "30 May 2026",
                time = "23:41",
                reason = "MB E-Money Creation to Registered",
                transactionType = "CREDIT",
                orderId = "EM917281HKW7"
            )
        )
        for (item in defaultList) {
            repository.insertTransaction(item)
        }
    }

    // Dynamic Metadata Update: "edit any-time account holder name and sender name"
    fun saveEditSettings() {
        viewModelScope.launch {
            val name = editAccountHolderName.value.trim()
            val sender = editSenderName.value.trim()
            
            // 1. Update Core Room Profile
            val currentProfile = repository.getProfileDirect() ?: UserProfile()
            repository.updateProfile(currentProfile.copy(
                accountHolderName = name
            ))

            // 2. Update selected transaction record (if one is loaded)
            selectedTransaction.value?.let { tx ->
                val updatedTx = tx.copy(
                    senderName = sender,
                    receiverName = editReceiverName.value.trim(),
                    receiverAccount = editReceiverAccount.value.trim(),
                    amount = editTransactionAmount.value.toDoubleOrNull() ?: tx.amount,
                    transactionId = editTransactionId.value.trim(),
                    date = editDateString.value.trim(),
                    time = editTimeString.value.trim()
                )
                repository.insertTransaction(updatedTx) // Overwrites and triggers Flow
                selectedTransaction.value = updatedTx
                
                // Re-trigger PDF compilation if necessary to refresh downloadable cache
                generatePdfReceipt(updatedTx)
            }
            
            syncStatus.value = "Synced with device profiles & encrypted cloud cloudspot."
        }
    }

    fun selectTransactionReceipt(tx: TransactionRecord) {
        selectedTransaction.value = tx
        // Pre-populate editor fields
        editSenderName.value = tx.senderName
        editReceiverName.value = tx.receiverName
        editReceiverAccount.value = tx.receiverAccount
        editTransactionAmount.value = tx.amount.toString()
        editTransactionId.value = tx.transactionId
        editDateString.value = tx.date
        editTimeString.value = tx.time
        
        // Auto-compile PDF
        generatePdfReceipt(tx)
        navigateTo(AppScreen.INVOICE_RECEIPT_VIEW)
    }

    // Native PDF writing logic
    fun generatePdfReceipt(transaction: TransactionRecord) {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val pdfFile = PdfReceiptGenerator.generateReceiptPdf(context, transaction)
                pdfFileState.value = pdfFile
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Check PIN and navigate (Image 4 error simulation)
    fun appendPin(digit: String) {
        if (pinBuffer.value.length < 4) {
            pinBuffer.value += digit
            if (pinBuffer.value.length == 4) {
                viewModelScope.launch {
                    val profile = repository.getProfileDirect() ?: UserProfile()
                    if (pinBuffer.value == profile.pin) {
                        pinError.value = null
                        pinBuffer.value = ""
                        navigateTo(AppScreen.DASHBOARD)
                    } else {
                        // Image 3 style PIN error prompt!
                        pinError.value = "Wrong PIN Please try again!"
                        pinBuffer.value = "" // clear to retry
                        addWrongPinSms()
                    }
                }
            }
        }
    }

    fun triggerBiometricAuth() {
        // Simulated biometric scanner success
        pinError.value = null
        pinBuffer.value = ""
        navigateTo(AppScreen.DASHBOARD)
    }

    // Send Money Form Execution
    fun executeSendMoney() {
        val amountVal = sendAmount.value.toDoubleOrNull() ?: 0.0
        val receiver = receiverAccountNumber.value.ifBlank { "Maereg Abreha Abera" }
        val reasonVal = sendReason.value.ifBlank { "Send Money" }

        if (amountVal <= 0.0) return

        viewModelScope.launch {
            val profile = repository.getProfileDirect() ?: UserProfile()
            
            // Subtract balance
            val updatedBalance = (profile.balance - amountVal).coerceAtLeast(0.0)
            repository.updateProfile(profile.copy(balance = updatedBalance))
            
            // Random high fidelity transaction IDs
            val allowedChars = ('A'..'Z') + ('0'..'9')
            val randomTxId = "DEV" + (1..8).map { allowedChars.random() }.joinToString("")
            val randomOrderId = "FT26" + (1..8).map { allowedChars.random() }.joinToString("")

            // Date formatting matching CBE standard
            val today = Calendar.getInstance()
            val dfDate = SimpleDateFormat("dd MMM yyyy", Locale.US).format(today.time)
            val dfTime = SimpleDateFormat("HH:mm", Locale.US).format(today.time)

            val newRecord = TransactionRecord(
                transactionId = randomTxId,
                senderName = profile.accountHolderName,
                receiverName = receiver,
                receiverAccount = "1000****8222 - CBE", // Default receiver bank card format
                amount = amountVal,
                date = dfDate,
                time = dfTime,
                reason = reasonVal,
                transactionType = "DEBIT",
                orderId = randomOrderId
            )

            repository.insertTransaction(newRecord)
            selectedTransaction.value = newRecord
            
            // Pre-fill editable metadata
            editSenderName.value = profile.accountHolderName
            editReceiverName.value = receiver
            editTransactionAmount.value = amountVal.toString()
            editTransactionId.value = randomTxId
            editDateString.value = dfDate
            editTimeString.value = dfTime

            // Compile PDF
            generatePdfReceipt(newRecord)

            // Transition to congratulations success details
            navigateTo(AppScreen.SUCCESS_SUMMARY)
            
            // Clear inputs
            sendAmount.value = ""
            receiverAccountNumber.value = ""
            sendReason.value = ""
        }
    }

    fun navigateTo(screen: AppScreen) {
        screenBackstack.push(screen)
        currentScreen.value = screen
    }

    fun handleBackPress(): Boolean {
        if (screenBackstack.size > 1) {
            screenBackstack.pop() // remove current
            currentScreen.value = screenBackstack.peek()
            return true
        }
        return false // let OS handle
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
            seedDefaultTransactions()
        }
    }

    fun addWrongPinSms() {
        wrongPinCount.value += 1
        refreshSmsInbox()
    }

    fun refreshSmsInbox(transactions: List<TransactionRecord> = allTransactions.value) {
        val list = mutableListOf<SmsMessage>()

        // 1. Preloaded / historical generic SMS from CBE Birr to make the inbox rich and matching the screenshots
        list.add(
            SmsMessage(
                senderId = "CBE_BIRR",
                body = "Dear SURAFEL, you paid 50.00Br. to 344664 - Tamcon Software PLC on 01/06/2026 16:20. Txn ID DF101EL3YAM. Your CBE Birr account balance is 1,317.07Br. Thank you!",
                time = "01 Jun 16:20"
            )
        )

        list.add(
            SmsMessage(
                senderId = "CBE_BIRR",
                body = "Dear SURAFEL, you have withdrawn 200.00Br. from CBE ATM on 31/05/2026 21:56. Txn ID DEV81EJM322. Your CBE Birr account balance is 1,717.07Br. Thank you!",
                time = "31 May 21:56"
            )
        )

        // Wrong PIN alert SMS added on failures
        for (i in 1..wrongPinCount.value) {
            list.add(
                SmsMessage(
                    senderId = "CBE_BIRR",
                    body = "Sorry, you have entered the wrong PIN. Please check and try again. Thank you!",
                    time = "Today"
                )
            )
        }

        // 2. Map all actual database transactions into CBE Birr text messages dynamically
        var balanceAccum = 25300.50
        transactions.forEach { tx ->
            val isDebit = tx.transactionType == "DEBIT"
            val numFormat = String.format(Locale.US, "%.2f", tx.amount)
            val balFormat = String.format(Locale.US, "%.2f", balanceAccum)
            
            val messageText = if (isDebit) {
                val fName = tx.senderName.substringBefore(" ")
                "Dear $fName, you have successfully transferred ${numFormat}Br. to ${tx.receiverAccount.take(12)}-${tx.receiverName} on ${tx.date} ${tx.time}. Txn ID ${tx.transactionId},${tx.orderId}. Your CBEBirr account balance is ${balFormat}Br. Thank you for Choosing CBE Birr! For your feedback please click the link https://shorturl.at/gy3A0 For invoice https://cbepay1.cbe.com.et/aureceipt?TID=${tx.transactionId}&PH=${loginPhone.value.replace(" ", "")}"
            } else {
                val fName = tx.receiverName.substringBefore(" ")
                "Dear $fName, your account was credited with ${numFormat}Br. by ${tx.senderName} on ${tx.date} ${tx.time}. Txn ID ${tx.transactionId}. Your CBE Birr balance is ${balFormat}Br."
            }

            // Adjust balance backwards or forwards for successive timeline view
            balanceAccum = if (isDebit) balanceAccum + tx.amount else (balanceAccum - tx.amount).coerceAtLeast(0.0)

            list.add(
                SmsMessage(
                    senderId = "CBE_BIRR",
                    body = messageText,
                    time = "${tx.date} ${tx.time}",
                    transactionRecord = tx
                )
            )
        }

        // Sort messages chronologically by timestamp-like value or insert
        val sortedCbe = list.sortedBy { it.time }

        // Compile other mock contact messages for full Google Messages clone layout
        val telebirrSms = listOf(
            SmsMessage("TELEBIRR", "Dear Customer, you have successfully bought 100 ETB Airtime Package. Transaction No: 9172816B. Thank you!", "Yesterday 18:40"),
            SmsMessage("TELEBIRR", "telebirr: Dear Surafel You have paid ETB 5.00 for parking fee on 30-05-2026. Txn ID: PP192837. Thank you!", "30 May 12:45")
        )

        val ethioSms = listOf(
            SmsMessage("ETHIO_TELECOM", "Daily voice 24 Min from telebirr is successfully activated. Your balance is 24 Min. Thank you!", "Yesterday 07:15")
        )

        val lotterySms = listOf(
            SmsMessage("ET_LOTTERY", "ውድ ደንበኛችን 50 ብር በሞባይል ባንኪንግዎ በመቁረጥ ሎተሪዎን ዕጣ አግኝተዋል። መልካም ዕድል!", "28 May 10:20")
        )

        smsMessages.value = mapOf(
            "CBE_BIRR" to sortedCbe,
            "TELEBIRR" to telebirrSms,
            "ETHIO_TELECOM" to ethioSms,
            "ET_LOTTERY" to lotterySms
        )

        smsContacts.value = listOf(
            SmsContact(
                senderId = "CBE_BIRR",
                displayName = "Cbe Birr",
                initials = "CB",
                avatarColor = 0xFF851C80,
                lastMessageShort = sortedCbe.lastOrNull()?.body ?: "Dear SURAFEL, your transfer is completed...",
                relativeTime = sortedCbe.lastOrNull()?.time ?: "Now",
                unread = true,
                notificationColor = 0xFFE9C5A2
            ),
            SmsContact(
                senderId = "TELEBIRR",
                displayName = "telebirr",
                initials = "TB",
                avatarColor = 0xFF0288D1,
                lastMessageShort = "Dear Customer, you have successfully bought...",
                relativeTime = "Yesterday",
                unread = false
            ),
            SmsContact(
                senderId = "ETHIO_TELECOM",
                displayName = "Ethio Telecom",
                initials = "ET",
                avatarColor = 0xFF4CAF50,
                lastMessageShort = "Daily voice 24 Min from telebirr is successfully activated...",
                relativeTime = "Yesterday",
                unread = false
            ),
            SmsContact(
                senderId = "ET_LOTTERY",
                displayName = "Et Lottery",
                initials = "EL",
                avatarColor = 0xFFFF5722,
                lastMessageShort = "ውድ ደንበኛችን 50 ብር በሞባይል ባንኪንግዎ...",
                relativeTime = "28 May",
                unread = false
            )
        )
    }
}
