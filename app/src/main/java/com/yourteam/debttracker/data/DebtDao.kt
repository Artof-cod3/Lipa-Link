package com.yourteam.debttracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    @Insert
    suspend fun insertDebt(debt: Debt)

    @Update
    suspend fun updateDebt(debt: Debt)

    @Delete
    suspend fun deleteDebt(debt: Debt)

    @Query("SELECT * FROM debts ORDER BY dateIssued DESC")
    fun getAllDebts(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE id = :debtId")
    suspend fun getDebtById(debtId: Int): Debt?
}
