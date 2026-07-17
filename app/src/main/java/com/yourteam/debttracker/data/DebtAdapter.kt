package com.yourteam.debttracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class DebtAdapter(
    private val onEditClick: (Debt) -> Unit,
    private val onDeleteClick: (Debt) -> Unit,
    private val onRepaymentClick: (Debt) -> Unit,
    private val onRemindClick: (Debt) -> Unit
) : ListAdapter<Debt, DebtAdapter.DebtViewHolder>(DebtDiffCallback()) {

    class DebtViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.textPersonName)
        val amountText: TextView = view.findViewById(R.id.textAmount)
        val notesText: TextView = view.findViewById(R.id.textNotes)
        val editButton: View = view.findViewById(R.id.buttonEdit)
        val deleteButton: View = view.findViewById(R.id.buttonDelete)
        val repaymentButton: View = view.findViewById(R.id.buttonRepayment)
        val remindButton: View = view.findViewById(R.id.buttonRemind)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_debt, parent, false)
        return DebtViewHolder(view)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        val debt = getItem(position)
        holder.nameText.text = debt.personName
        holder.amountText.text = "KES ${debt.amount}"
        holder.notesText.text = debt.notes

        holder.editButton.setOnClickListener { onEditClick(debt) }
        holder.deleteButton.setOnClickListener { onDeleteClick(debt) }
        holder.repaymentButton.setOnClickListener { onRepaymentClick(debt) }
        holder.remindButton.setOnClickListener { onRemindClick(debt) }
    }
}

class DebtDiffCallback : DiffUtil.ItemCallback<Debt>() {
    override fun areItemsTheSame(oldItem: Debt, newItem: Debt) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Debt, newItem: Debt) = oldItem == newItem
}