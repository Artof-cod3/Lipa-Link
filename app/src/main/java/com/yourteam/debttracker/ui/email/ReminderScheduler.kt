package com.yourteam.debttracker.ui.email

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(
        context: Context,
        debtId: Int,
        borrowerName: String,
        borrowerEmail: String,
        amount: Double,
        delay: Long,
        timeUnit: TimeUnit = TimeUnit.DAYS
    ) {
        val data = Data.Builder()
            .putInt("debtId", debtId)
            .putString("borrowerName", borrowerName)
            .putString("borrowerEmail", borrowerEmail)
            .putDouble("amount", amount)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<EmailReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delay, timeUnit)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
