package com.example.leanmasscalculator.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.leanmasscalculator.databinding.ActivityLoginBinding
import com.example.leanmasscalculator.security.SecurityUtils
import com.example.leanmasscalculator.ui.calculator.CalculatorActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // MASVS-RESILIENCE-1 : avertir si appareil rooté
        SecurityUtils.warnIfRooted(this)

        // MASVS-AUTH-1 : session active mais non expirée → redirection directe
        if (auth.currentUser != null && !SecurityUtils.isSessionExpired(this)) {
            SecurityUtils.recordActivity(this)
            goToCalculator()
            return
        }

        // Session expirée : déconnexion forcée + effacement du timestamp
        if (auth.currentUser != null && SecurityUtils.isSessionExpired(this)) {
            auth.signOut()
            SecurityUtils.clearSession(this)
            Toast.makeText(this, "Session expirée — veuillez vous reconnecter.", Toast.LENGTH_LONG).show()
        }

        binding.btnLogin.setOnClickListener { login() }
        binding.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        // MASVS-CODE-1 : validation format e-mail avant appel réseau
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format d'e-mail invalide", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                SecurityUtils.recordActivity(this)   // MASVS-AUTH-1 : démarrage du timer
                goToCalculator()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                // MASVS-CODE-1 : message générique (ne révèle pas si e-mail existe)
                Toast.makeText(this, "Identifiants incorrects.", Toast.LENGTH_LONG).show()
            }
    }

    private fun goToCalculator() {
        startActivity(Intent(this, CalculatorActivity::class.java))
        finish()
    }
}