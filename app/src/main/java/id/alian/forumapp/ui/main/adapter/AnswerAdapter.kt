package id.alian.forumapp.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.alian.forumapp.data.model.Answers
import id.alian.forumapp.databinding.AnswerItemLayoutBinding

class AnswerAdapter : RecyclerView.Adapter<AnswerAdapter.DataViewHolder>() {
    inner class DataViewHolder(private val binding: AnswerItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(answer: Answers) {
            binding.tvName.text = answer.user.name
            binding.tvDesc.text = answer.description
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Answers>() {
        override fun areItemsTheSame(oldItem: Answers, newItem: Answers): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Answers, newItem: Answers): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding =
            AnswerItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}