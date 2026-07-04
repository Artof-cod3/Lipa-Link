package com.yourteam.debttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repayment_terms")
data class RepaymentTerm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val debtId: Int,
    val totalAmount: Double,
    val numberOfInstalments: Int,
    val instalmentFrequency: String,
    val agreedDate: Long,
    val dueDateFinal: Long,
    val notes: String = ""
)