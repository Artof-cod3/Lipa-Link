package com.yourteam.debttracker.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RepaymentTermDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(term: RepaymentTerm)

    @Update
    suspend fun update(term: RepaymentTerm)

    @Query("SELECT * FROM repayment_terms WHERE debtId = :debtId")
    fun getTermsForDebt(debtId: Int): LiveData<RepaymentTerm?>

    @Query("DELETE FROM repayment_terms WHERE debtId = :debtId")
    suspend fun deleteTermsForDebt(debtId: Int)
}