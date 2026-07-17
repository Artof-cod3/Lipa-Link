package com.yourteam.debttracker.ui.email

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yourteam.debttracker.R
import com.yourteam.debttracker.data.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class EmailFollowUpFragment : Fragment(R.layout.fragment_email_followup) {

    private var debtId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        debtId = arguments?.getInt("debtId", -1) ?: -1

        val tvName = view.findViewById<TextView>(R.id.tvBorrowerName)
        val tvEmail = view.findViewById<TextView>(R.id.tvBorrowerEmail)
        val tvAmount = view.findViewById<TextView>(R.id.tvAmountOwed)
        val rgTiming = view.findViewById<RadioGroup>(R.id.rgTiming)
        val btnSchedule = view.findViewById<Button>(R.id.btnSchedule)

        val layoutRepayment = view.findViewById<View>(R.id.layoutRepaymentTerms)
        val tvInstalments = view.findViewById<TextView>(R.id.tvInstalments)
        val tvFinalDueDate = view.findViewById<TextView>(R.id.tvFinalDueDate)
        val tvRepaymentNotes = view.findViewById<TextView>(R.id.tvRepaymentNotes)

        val db = AppDatabase.getDatabase(requireContext())
        val dao = db.debtDao()
        val termDao = db.repaymentTermDao()

        // Load Borrower Details
        viewLifecycleOwner.lifecycleScope.launch {
            val debt = dao.getDebtById(debtId)
            if (debt != null) {
                tvName.text = "Name: ${debt.personName}"
                tvEmail.text = if (debt.personEmail.isNotEmpty()) {
                    "Email: ${debt.personEmail}"
                } else {
                    "Email: (Not provided)"
                }
                tvAmount.text = "Amount Owed: KES ${debt.amount}"

                if (debt.personEmail.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please add borrower's email before scheduling reminders.",
                        Toast.LENGTH_LONG
                    ).show()
                    btnSchedule.isEnabled = false
                } else {
                    btnSchedule.isEnabled = true
                }

                // Check Internet Access
                val hasInternet = isInternetAvailable(requireContext())
                if (!hasInternet) {
                    Toast.makeText(
                        requireContext(),
                        "Warning: No internet connection detected. The reminder will be sent once the device reconnects.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                btnSchedule.setOnClickListener {
                    val delay: Long
                    val unit: TimeUnit

                    when (rgTiming.checkedRadioButtonId) {
                        R.id.rbImmediate -> {
                            delay = 10
                            unit = TimeUnit.SECONDS
                        }
                        R.id.rbOneDay -> {
                            delay = 1
                            unit = TimeUnit.DAYS
                        }
                        R.id.rbThreeDays -> {
                            delay = 3
                            unit = TimeUnit.DAYS
                        }
                        R.id.rbSevenDays -> {
                            delay = 7
                            unit = TimeUnit.DAYS
                        }
                        else -> {
                            delay = 10
                            unit = TimeUnit.SECONDS
                        }
                    }

                    ReminderScheduler.schedule(
                        context = requireContext(),
                        debtId = debt.id,
                        borrowerName = debt.personName,
                        borrowerEmail = debt.personEmail,
                        amount = debt.amount,
                        delay = delay,
                        timeUnit = unit
                    )

                    Toast.makeText(
                        requireContext(),
                        "Email reminder scheduled successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                }
            } else {
                Toast.makeText(requireContext(), "Error: Debt not found", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        // Load Repayment Terms if they exist
        termDao.getTermsForDebt(debtId).observe(viewLifecycleOwner) { term ->
            if (term != null) {
                layoutRepayment.visibility = View.VISIBLE
                tvInstalments.text = "Instalments: ${term.numberOfInstalments} (${term.instalmentFrequency})"
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvFinalDueDate.text = "Final Due Date: ${formatter.format(Date(term.dueDateFinal))}"
                tvRepaymentNotes.text = "Notes: ${term.notes.ifEmpty { "None" }}"
            } else {
                layoutRepayment.visibility = View.GONE
            }
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}