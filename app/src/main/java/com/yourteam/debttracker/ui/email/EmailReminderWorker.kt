package com.yourteam.debttracker.ui.email

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourteam.debttracker.BuildConfig
import com.yourteam.debttracker.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val debtId = inputData.getInt("debtId", -1)
        val borrowerName = inputData.getString("borrowerName") ?: return Result.failure()
        val borrowerEmail = inputData.getString("borrowerEmail") ?: return Result.failure()
        val amount = inputData.getDouble("amount", 0.0)

        val username = BuildConfig.GMAIL_USERNAME
        val password = BuildConfig.GMAIL_APP_PASSWORD

        if (username.isEmpty() || password.isEmpty()) {
            Log.e("EmailReminderWorker", "SMTP credentials are empty. Please check local.properties config.")
            return Result.failure()
        }

        val db = AppDatabase.getDatabase(applicationContext)
        val lender = db.userDao().getFirstUser()
        val lenderName = lender?.name ?: "the lender"

        val term = if (debtId != -1) {
            db.repaymentTermDao().getTermsForDebtOnce(debtId)
        } else {
            null
        }

        val termsText = if (term != null) {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            "\nHere are the repayment terms that were agreed upon:\n" +
            "- Total Instalments: ${term.numberOfInstalments} (${term.instalmentFrequency})\n" +
            "- Final Due Date: ${formatter.format(Date(term.dueDateFinal))}\n" +
            (if (term.notes.isNotEmpty()) "- Notes: ${term.notes}\n" else "")
        } else {
            ""
        }

        val emailBody = "Hello $borrowerName,\n\n" +
            "This is a friendly reminder that you have an outstanding debt of KES $amount " +
            "owed to $lenderName. Please arrange for repayment as soon as possible.\n" +
            termsText +
            "\nThank you!"

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        return try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(borrowerEmail))
                subject = "Debt Repayment Reminder"
                setText(emailBody)
            }

            Transport.send(message)
            Log.d("EmailReminderWorker", "Email reminder sent successfully to $borrowerEmail")
            Result.success()
        } catch (e: Exception) {
            Log.e("EmailReminderWorker", "Error sending email to $borrowerEmail", e)
            Result.retry()
        }
    }
}
