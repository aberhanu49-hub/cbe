package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val accountHolderName: String = "SURAFEL ABRIHA HAILE",
    val phoneNumber: String = "+251945954856",
    val balance: Double = 25300.50,
    val rewardPoints: Int = 1680,
    val pin: String = "1234",
    val isBiometricEnabled: Boolean = true,
    val lastSyncTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionRecord(
    @PrimaryKey val transactionId: String, // e.g. "DEV81EJHWNC"
    val senderName: String,
    val receiverName: String,
    val receiverAccount: String,
    val amount: Double,
    val date: String,  // e.g. "31 May 2026"
    val time: String,  // e.g. "22:54"
    val reason: String,
    val transactionType: String, // "DEBIT" or "CREDIT"
    val orderId: String, // e.g. "FT2615217L66"
    val serviceCharge: Double = 0.0,
    val vat: Double = 0.0,
    val tip: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
