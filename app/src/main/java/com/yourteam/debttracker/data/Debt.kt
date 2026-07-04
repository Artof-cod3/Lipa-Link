package com.yourteam.debttracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val personName: String,
    val amount: Double,
    val dateIssued: Long,   // stored as System.currentTimeMillis()
    val notes: String,
    val isPaid: Boolean = false
)
