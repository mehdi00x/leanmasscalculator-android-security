package com.example.leanmasscalculator.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.leanmasscalculator.R
import com.example.leanmasscalculator.databinding.ItemHistoryBinding
import com.example.leanmasscalculator.model.CalculationResult
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onDelete: (CalculationResult) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var items = listOf<CalculationResult>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun submitList(list: List<CalculationResult>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: CalculationResult) {
            val sexLabel = if (result.sex == "H") "Homme" else "Femme"
            binding.tvDate.text = dateFormat.format(Date(result.date))
            binding.tvLbm.text = "${"%.1f".format(result.lbm)} kg"
            binding.tvDetails.text = "$sexLabel • ${result.weight} kg • ${result.height} cm"

            if (result.isSatisfactory) {
                binding.imgStatus.setImageResource(R.drawable.ic_satisfied)
                binding.tvStatus.text = "Satisfaisant"
                binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.status_good))
            } else {
                binding.imgStatus.setImageResource(R.drawable.ic_unsatisfied)
                binding.tvStatus.text = "À surveiller"
                binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.status_warning))
            }

            binding.btnDelete.setOnClickListener { onDelete(result) }
        }
    }
}
