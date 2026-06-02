package com.example.data

import kotlinx.coroutines.flow.Flow

class CbeRepository(private val dao: CbeDao) {
    val userProfile: Flow<UserProfile?> = dao.getUserProfileFlow()
    val allTransactions: Flow<List<TransactionRecord>> = dao.getAllTransactionsFlow()

    suspend fun getProfileDirect(): UserProfile? = dao.getUserProfile()

    suspend fun updateProfile(profile: UserProfile) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun insertTransaction(record: TransactionRecord) {
        dao.insertTransaction(record)
    }

    suspend fun getTransactionById(txId: String): TransactionRecord? {
        return dao.getTransactionById(txId)
    }

    suspend fun deleteTransaction(txId: String) {
        dao.deleteTransaction(txId)
    }

    suspend fun clearAll() {
        dao.clearAllTransactions()
    }
}
