package com.yourteam.debttracker.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.yourteam.debttracker.R
import com.yourteam.debttracker.data.AppDatabase
import com.yourteam.debttracker.data.User
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName     = view.findViewById<TextInputEditText>(R.id.etName)
        val etEmail    = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvLogin    = view.findViewById<TextView>(R.id.tvGoToLogin)

        val db = AppDatabase.getDatabase(requireContext())

        btnRegister.setOnClickListener {
            val name     = etName.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    db.userDao().register(User(name = name, email = email, password = password))
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Account created! Please login", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_register_to_login)
                    }
                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Email already registered", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }
}