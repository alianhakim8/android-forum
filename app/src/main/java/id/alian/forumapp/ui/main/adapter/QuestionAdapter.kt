package id.alian.forumapp.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import id.alian.forumapp.R
import id.alian.forumapp.data.model.Question
import id.alian.forumapp.databinding.MyQuestionItemLayoutBinding
import id.alian.forumapp.utils.Constants.BASE_URL

class QuestionAdapter : RecyclerView.Adapter<QuestionAdapter.DataViewHolder>() {
    inner class DataViewHolder(private val binding: MyQuestionItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question) {
            binding.textViewTitle.text = question.title
            binding.textViewUserDescription.text = question.description
        }

        val img = binding.imageView
    }

    private val differCallback = object : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding =
            MyQuestionItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val question = differ.currentList[position]
        holder.bind(differ.currentList[position])
        holder.itemView.apply {
            setOnClickListener {
                onItemClickListener?.let {
                    it(question)
                }
            }
        }
        holder.itemView.apply {
            holder.img.load(BASE_URL + "question/image/${question.image_name}") {
                placeholder(R.drawable.ic_img)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((Question) -> Unit)? = null

    fun setOnItemClickListener(listener: (Question) -> Unit) {
        onItemClickListener = listener
    }
}