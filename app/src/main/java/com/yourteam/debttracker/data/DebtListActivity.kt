package com.yourteam.debttracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class DebtListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAddDebt: Button
    private lateinit var adapter: DebtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_list)

        recyclerView = findViewById(R.id.recyclerViewDebts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DebtAdapter(
            onEditClick = { debt -> /* navigate to edit */ },
            onDeleteClick = { debt -> /* delete from DB */ }
        )
        buttonAddDebt = findViewById(R.id.buttonAddDebt)

        adapter = DebtAdapter(
            onEditClick = { debt ->
                val intent = Intent(this, EditDebtActivity::class.java)
                intent.putExtra("DEBT_ID", debt.id)
                startActivity(intent)
            },
            onDeleteClick = { debt ->
                deleteDebt(debt)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = DebtAdapter(
            onEditClick = { debt ->
                // Navigate to edit screen
                val intent = Intent(this, EditDebtActivity::class.java)
                intent.putExtra("debt_id", debt.id)
                startActivity(intent)
            },
            onDeleteClick = { debt ->
                // Delete the debt
                deleteDebt(debt)
            }
        )
        recyclerView.adapter = adapter

        buttonAddDebt.setOnClickListener {
            startActivity(Intent(this, AddDebtActivity::class.java))
        }

        observeDebts()
    }

    private fun observeDebts() {
        val db = DebtDatabase.getDatabase(applicationContext)
        lifecycleScope.launch {
            db.debtDao().getAllDebts().collect { debts ->
                adapter.submitList(debts)
            }
        }
    }

    private fun deleteDebt(debt: Debt) {
        lifecycleScope.launch {
            val db = DebtDatabase.getDatabase(applicationContext)
            db.debtDao().deleteDebt(debt)
        }
    }
}
