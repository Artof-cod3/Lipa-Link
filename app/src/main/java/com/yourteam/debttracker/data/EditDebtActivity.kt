package com.yourteam.debttracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EditDebtActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editAmount: EditText
    private lateinit var editNotes: EditText
    private lateinit var buttonUpdate: Button

    private var currentDebt: Debt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_debt)

        editName = findViewById(R.id.editPersonName)
        editAmount = findViewById(R.id.editAmount)
        editNotes = findViewById(R.id.editNotes)
        buttonUpdate = findViewById(R.id.buttonUpdateDebt)

        val debtId = intent.getIntExtra("DEBT_ID", -1)
        if (debtId == -1) {
            Toast.makeText(this, "Debt not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadDebt(debtId)

        buttonUpdate.setOnClickListener {
            updateDebt()
        }
    }

    private fun loadDebt(debtId: Int) {
        lifecycleScope.launch {
            val db = DebtDatabase.getDatabase(applicationContext)
            val debt = db.debtDao().getDebtById(debtId)
            if (debt != null) {
                currentDebt = debt
                editName.setText(debt.personName)
                editAmount.setText(debt.amount.toString())
                editNotes.setText(debt.notes)
            } else {
                Toast.makeText(this@EditDebtActivity, "Debt not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateDebt() {
        val debt = currentDebt ?: return
        val name = editName.text.toString().trim()
        val amountText = editAmount.text.toString().trim()
        val notes = editNotes.text.toString().trim()

        if (name.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Name and amount are required", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedDebt = debt.copy(
            personName = name,
            amount = amount,
            notes = notes
        )

        lifecycleScope.launch {
            val db = DebtDatabase.getDatabase(applicationContext)
            db.debtDao().updateDebt(updatedDebt)
            Toast.makeText(this@EditDebtActivity, "Debt updated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
