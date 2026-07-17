package com.yourteam.debttracker.ui.debt

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yourteam.debttracker.Debt
import com.yourteam.debttracker.R
import com.yourteam.debttracker.data.AppDatabase
import kotlinx.coroutines.launch

class AddEditDebtFragment : Fragment(R.layout.fragment_add_edit_debt) {

    private var debtId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        debtId = arguments?.getInt("debtId", -1) ?: -1

        val etName = view.findViewById<EditText>(R.id.etPersonName)
        val etEmail = view.findViewById<EditText>(R.id.etPersonEmail)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val btnSave = view.findViewById<Button>(R.id.btnSaveDebt)

        val dao = AppDatabase.getDatabase(requireContext()).debtDao()

        // Edit mode: load existing debt and pre-fill the form
        if (debtId != -1) {
            viewLifecycleOwner.lifecycleScope.launch {
                val existing = dao.getDebtById(debtId)
                existing?.let {
                    etName.setText(it.personName)
                    etEmail.setText(it.personEmail)
                    etAmount.setText(it.amount.toString())
                    etNotes.setText(it.notes)
                }
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val notes = etNotes.text.toString().trim()

            if (name.isEmpty() || amountText.isEmpty()) {
                return@setOnClickListener // TODO: show a validation message
            }

            val amount = amountText.toDoubleOrNull() ?: return@setOnClickListener

            viewLifecycleOwner.lifecycleScope.launch {
                if (debtId == -1) {
                    dao.insertDebt(
                        Debt(
                            personName = name,
                            personEmail = email,
                            amount = amount,
                            dateIssued = System.currentTimeMillis(),
                            notes = notes
                        )
                    )
                } else {
                    dao.updateDebt(
                        Debt(
                            id = debtId,
                            personName = name,
                            personEmail = email,
                            amount = amount,
                            dateIssued = System.currentTimeMillis(),
                            notes = notes
                        )
                    )
                }
                findNavController().popBackStack() // return to DebtListFragment
            }
        }
    }
}