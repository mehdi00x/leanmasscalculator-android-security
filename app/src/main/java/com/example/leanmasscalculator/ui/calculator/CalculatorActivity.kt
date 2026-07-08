package com.example.leanmasscalculator.ui.calculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.leanmasscalculator.R
import com.example.leanmasscalculator.data.DatabaseHelper
import com.example.leanmasscalculator.databinding.ActivityCalculatorBinding
import com.example.leanmasscalculator.model.CalculationResult
import com.example.leanmasscalculator.security.SecurityUtils
import com.example.leanmasscalculator.ui.auth.LoginActivity
import com.example.leanmasscalculator.ui.history.HistoryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseHelper
    private val firestore = FirebaseFirestore.getInstance()

    private val normMale   = 38.0
    private val normFemale = 24.0

    // MASVS-CODE-1 : bornes physiologiques pour la validation des entrées
    private val WEIGHT_MIN = 1.0
    private val WEIGHT_MAX = 500.0
    private val HEIGHT_MIN = 50.0
    private val HEIGHT_MAX = 300.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db   = DatabaseHelper(this)

        // MASVS-AUTH-1 : vérification du timeout de session à chaque reprise
        if (SecurityUtils.isSessionExpired(this)) {
            Toast.makeText(this, "Session expirée — reconnexion requise.", Toast.LENGTH_LONG).show()
            logout()
            return
        }

        // MASVS-RESILIENCE-1 : avertissement root
        SecurityUtils.warnIfRooted(this)

        binding.btnCalculate.setOnClickListener { calculate() }
        binding.btnHistory.setOnClickListener {
            SecurityUtils.recordActivity(this)
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnLogout.setOnClickListener { logout() }
    }

    override fun onResume() {
        super.onResume()
        // MASVS-AUTH-1 : rafraîchir le timestamp à chaque retour sur l'écran
        SecurityUtils.recordActivity(this)
    }

    private fun calculate() {
        val weightStr = binding.etWeight.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()

        if (weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toDoubleOrNull()
        val height = heightStr.toDoubleOrNull()

        if (weight == null || height == null) {
            Toast.makeText(this, "Valeurs numériques requises", Toast.LENGTH_SHORT).show()
            return
        }

        // MASVS-CODE-1 : validation des bornes physiologiques
        if (weight < WEIGHT_MIN || weight > WEIGHT_MAX) {
            Toast.makeText(this, "Poids invalide (entre 1 et 500 kg)", Toast.LENGTH_SHORT).show()
            return
        }
        if (height < HEIGHT_MIN || height > HEIGHT_MAX) {
            Toast.makeText(this, "Taille invalide (entre 50 et 300 cm)", Toast.LENGTH_SHORT).show()
            return
        }

        val isMale = binding.radioMale.isChecked
        val sex    = if (isMale) "H" else "F"

        // Formule de Boer
        val lbm = if (isMale)
            0.407 * weight + 0.267 * height - 19.2
        else
            0.252 * weight + 0.473 * height - 48.3

        val isSatisfactory = lbm >= (if (isMale) normMale else normFemale)

        showResult(lbm, isSatisfactory)
        saveResult(weight, height, sex, lbm, isSatisfactory)
        SecurityUtils.recordActivity(this)   // MASVS-AUTH-1 : activité enregistrée
    }

    private fun showResult(lbm: Double, isSatisfactory: Boolean) {
        binding.cardResult.visibility = View.VISIBLE
        binding.tvLbmValue.text = "${"%.1f".format(lbm)} kg"

        if (isSatisfactory) {
            binding.cardResult.setCardBackgroundColor(getColor(R.color.status_good_bg))
            binding.imgStatus.setImageResource(R.drawable.ic_satisfied)
            binding.tvStatus.text = "Résultat satisfaisant"
            binding.tvStatus.setTextColor(getColor(R.color.status_good))
        } else {
            binding.cardResult.setCardBackgroundColor(getColor(R.color.status_warning_bg))
            binding.imgStatus.setImageResource(R.drawable.ic_unsatisfied)
            binding.tvStatus.text = "Résultat à surveiller"
            binding.tvStatus.setTextColor(getColor(R.color.status_warning))
        }
    }

    private fun saveResult(
        weight: Double, height: Double, sex: String,
        lbm: Double, isSatisfactory: Boolean
    ) {
        val userId = auth.currentUser?.uid ?: return
        val now    = System.currentTimeMillis()

        val result = CalculationResult(
            userId = userId, weight = weight, height = height,
            sex = sex, lbm = lbm, date = now, isSatisfactory = isSatisfactory
        )
        val localId = db.insert(result)

        val data = hashMapOf(
            "userId" to userId, "weight" to weight, "height" to height,
            "sex" to sex, "lbm" to lbm, "date" to now,
            "isSatisfactory" to isSatisfactory
        )

        firestore.collection("users").document(userId)
            .collection("calculations")
            .add(data)
            .addOnSuccessListener { docRef -> db.updateFirestoreId(localId, docRef.id) }
    }

    private fun logout() {
        auth.signOut()
        SecurityUtils.clearSession(this)   // MASVS-AUTH-1 : effacer le timer de session
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
