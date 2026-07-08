package com.example.leanmasscalculator.ui.history

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leanmasscalculator.data.DatabaseHelper
import com.example.leanmasscalculator.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: HistoryAdapter
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        adapter = HistoryAdapter { result ->
            db.delete(result.id)
            if (result.firestoreId.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: return@HistoryAdapter
                firestore.collection("users").document(userId)
                    .collection("calculations").document(result.firestoreId)
                    .delete()
            }
            loadHistory()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        loadHistory()
    }

    private fun loadHistory() {
        val userId = auth.currentUser?.uid ?: return
        val list = db.getByUser(userId)
        adapter.submitList(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }
}
