package com.univpm.unirun.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.univpm.unirun.R

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private val auth = FirebaseAuth.getInstance()

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvAuthError: TextView
    private lateinit var progressAuth: ProgressBar

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_auth_to_home)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvAuthError = view.findViewById(R.id.tvAuthError)
        progressAuth = view.findViewById(R.id.progressAuth)

        btnLogin.setOnClickListener { attemptLogin() }
        btnRegister.setOnClickListener { attemptRegister() }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        if (!validateInput(email, password)) return

        showLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                showLoading(false)
                findNavController().navigate(R.id.action_auth_to_home)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                tvAuthError.text = e.localizedMessage ?: "Errore di accesso"
                tvAuthError.visibility = View.VISIBLE
            }
    }

    private fun attemptRegister() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        if (!validateInput(email, password)) return

        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                showLoading(false)
                findNavController().navigate(R.id.action_auth_to_home)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                tvAuthError.text = e.localizedMessage ?: "Errore di registrazione"
                tvAuthError.visibility = View.VISIBLE
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvAuthError.text = "Email non valida"
            tvAuthError.visibility = View.VISIBLE
            return false
        }
        if (password.length < 6) {
            tvAuthError.text = "Password: minimo 6 caratteri"
            tvAuthError.visibility = View.VISIBLE
            return false
        }
        tvAuthError.visibility = View.GONE
        return true
    }

    private fun showLoading(show: Boolean) {
        progressAuth.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnRegister.isEnabled = !show
    }
}
