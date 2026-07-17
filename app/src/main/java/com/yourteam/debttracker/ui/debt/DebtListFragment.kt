package com.yourteam.debttracker.ui.debt

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yourteam.debttracker.DebtAdapter
import com.yourteam.debttracker.R
import com.yourteam.debttracker.data.AppDatabase
import kotlinx.coroutines.launch

class DebtListFragment : Fragment(R.layout.fragment_debt_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvDebts)
        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddDebt)
        val dao = AppDatabase.getDatabase(requireContext()).debtDao()

        val adapter = DebtAdapter(
            onEditClick = { debt ->
                findNavController().navigate(
                    R.id.action_debtList_to_addEdit,
                    bundleOf("debtId" to debt.id)
                )
            },
            onDeleteClick = { debt ->
                viewLifecycleOwner.lifecycleScope.launch {
                    dao.deleteDebt(debt)
                }
            },
            onRepaymentClick = { debt ->
                findNavController().navigate(
                    R.id.action_list_to_repayment,
                    bundleOf("debtId" to debt.id)
                )
            },
            onRemindClick = { debt ->
                findNavController().navigate(
                    R.id.action_list_to_email,
                    bundleOf("debtId" to debt.id)
                )
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            findNavController().navigate(
                R.id.action_debtList_to_addEdit,
                bundleOf("debtId" to -1)
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dao.getAllDebts().collect { debts ->
                    adapter.submitList(debts)
                }
            }
        }
    }
}