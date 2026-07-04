package com.yourteam.debttracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddDebtActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editAmount: EditText
    private lateinit var editNotes: EditText
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_debt)

        editName = findViewById(R.id.editPersonName)
        editAmount = findViewById(R.id.editAmount)
        editNotes = findViewById(R.id.editNotes)
        buttonSave = findViewById(R.id.buttonSaveDebt)

        buttonSave.setOnClickListener {
            saveDebt()
        }
    }

    private fun saveDebt() {
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

        val debt = Debt(
            personName = name,
            amount = amount,
            dateIssued = System.currentTimeMillis(),
            notes = notes
        )

        lifecycleScope.launch {
            val db = DebtDatabase.getDatabase(applicationContext)
            db.debtDao().insertDebt(debt)
            Toast.makeText(this@AddDebtActivity, "Debt saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
