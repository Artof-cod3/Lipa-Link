package com.yourteam.debttracker.ui.repayment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yourteam.debttracker.R
import com.yourteam.debttracker.data.AppDatabase
import com.yourteam.debttracker.data.RepaymentTerm
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RepaymentTermsFragment : Fragment(R.layout.fragment_repayment_terms) {

    private var selectedDateMillis: Long? = null
    private var debtId: Int = -1
    private var existingTermId: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        debtId = arguments?.getInt("debtId", -1) ?: -1

        val etTotalAmount = view.findViewById<EditText>(R.id.etTotalAmount)
        val etInstalments = view.findViewById<EditText>(R.id.etInstalments)
        val spinnerFrequency = view.findViewById<Spinner>(R.id.spinnerFrequency)
        val btnPickDate = view.findViewById<Button>(R.id.btnPickDate)
        val tvSelectedDate = view.findViewById<TextView>(R.id.tvSelectedDate)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val btnSaveTerms = view.findViewById<Button>(R.id.btnSaveTerms)

        val frequencies = listOf("Weekly", "Bi-weekly", "Monthly", "Once (lump sum)", "Custom")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            frequencies
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrequency.adapter = spinnerAdapter

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dao = AppDatabase.getDatabase(requireContext()).repaymentTermDao()

        // Load existing terms if they exist for this debt
        if (debtId != -1) {
            dao.getTermsForDebt(debtId).observe(viewLifecycleOwner) { term ->
                term?.let {
                    existingTermId = it.id
                    etTotalAmount.setText(it.totalAmount.toString())
                    etInstalments.setText(it.numberOfInstalments.toString())
                    etNotes.setText(it.notes)
                    selectedDateMillis = it.dueDateFinal
                    tvSelectedDate.text = formatter.format(Date(it.dueDateFinal))

                    val freqIndex = frequencies.indexOf(it.instalmentFrequency)
                    if (freqIndex >= 0) spinnerFrequency.setSelection(freqIndex)
                }
            }
        }

        // Date picker
        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDateMillis = calendar.timeInMillis
                    tvSelectedDate.text = formatter.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save button
        btnSaveTerms.setOnClickListener {
            val amountText = etTotalAmount.text.toString().trim()
            val instalmentsText = etInstalments.text.toString().trim()
            val notes = etNotes.text.toString().trim()
            val frequency = spinnerFrequency.selectedItem.toString()
            val dueDate = selectedDateMillis

            if (amountText.isEmpty() || instalmentsText.isEmpty() || dueDate == null) {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all fields and pick a due date",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull() ?: return@setOnClickListener
            val instalments = instalmentsText.toIntOrNull() ?: return@setOnClickListener

            val term = RepaymentTerm(
                id = existingTermId,
                debtId = debtId,
                totalAmount = amount,
                numberOfInstalments = instalments,
                instalmentFrequency = frequency,
                agreedDate = System.currentTimeMillis(),
                dueDateFinal = dueDate,
                notes = notes
            )

            viewLifecycleOwner.lifecycleScope.launch {
                dao.insert(term)
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Repayment terms saved",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }
}