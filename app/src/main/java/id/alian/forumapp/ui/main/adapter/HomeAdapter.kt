package id.alian.forumapp.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.alian.forumapp.data.model.Question
import id.alian.forumapp.databinding.MainItemLayoutBinding

class HomeAdapter : RecyclerView.Adapter<HomeAdapter.DataViewHolder>() {

    inner class DataViewHolder(private val binding: MainItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question) {
            binding.textViewTitle.text = question.title
            binding.textViewUserEmail.text = question.user.email
//            binding.textViewUserDescription.text = question.description
        }

        val img = binding.imageViewAvatar
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
            MainItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
//        holder.itemView.apply {
//            holder.img.load(Base_URL + "question/image/${question.image_name}") {
////                transformations(RoundedCornersTransformation(15f))
//                placeholder(R.drawable.ic_img)
//            }
//        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((Question) -> Unit)? = null

    fun setOnItemClickListener(listener: (Question) -> Unit) {
        onItemClickListener = listener
    }
}