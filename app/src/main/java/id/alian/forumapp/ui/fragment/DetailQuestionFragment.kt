package id.alian.forumapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import id.alian.forumapp.R
import id.alian.forumapp.databinding.FragmentDetailQuestionBinding
import id.alian.forumapp.ui.main.view.question.DetailQuestionActivity
import id.alian.forumapp.utils.Constants.Base_URL

class DetailQuestionFragment : Fragment(R.layout.fragment_detail_question) {

    private var _binding: FragmentDetailQuestionBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailQuestionBinding.inflate(inflater, container, false)
        val view = binding.root

        val question = (activity as DetailQuestionActivity).question

        binding.etTitle.text = question?.title
        binding.etDesc.text = question?.description
        binding.photoView.load(Base_URL + "question/image/${question?.image_name}") {
            placeholder(R.drawable.ic_img)
        }

        binding.appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.answerFragment -> {
                    findNavController().navigate(R.id.action_detailQuestionFragment_to_detailBottomSheetFragment2)
                    true
                }
                else -> false
            }
        }

        binding.appBar.setOnClickListener {
            findNavController().navigate(R.id.action_detailQuestionFragment_to_detailBottomSheetFragment2)
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}