package id.alian.forumapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import id.alian.forumapp.databinding.FragmentDetailBottomSheetBinding
import id.alian.forumapp.ui.main.adapter.AnswerAdapter
import id.alian.forumapp.ui.main.view.question.DetailQuestionActivity
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import id.alian.forumapp.utils.Resource

class DetailBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDetailBottomSheetBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel
    private lateinit var answerAdapter: AnswerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailBottomSheetBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = (activity as DetailQuestionActivity).viewModel
        val question = (activity as DetailQuestionActivity).question
        setupRecyclerView()

        binding.btnAddAnswer.setOnClickListener {
            viewModel.addAnswer(question?.questionId!!, binding.etAddAnswer.text.toString().trim())
        }

        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
        }

        viewModel.getAnswers(question?.questionId!!)
        getAnswer()
        viewModel.addAnswers.observe(this, {
            when (it) {
                is Resource.Success -> {
                    viewModel.getAnswers(question.questionId)
                    binding.etAddAnswer.isEnabled = true
                    binding.btnAddAnswer.isEnabled = true
                }

                is Resource.Loading -> {
                    binding.etAddAnswer.isEnabled = false
                    binding.btnAddAnswer.isEnabled = false
                }

                is Resource.Error -> {
                    it.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    }
                    binding.etAddAnswer.isEnabled = true
                    binding.btnAddAnswer.isEnabled = true
                }
                else -> {}
            }
        })

        return view
    }

    private fun setupRecyclerView() {
        answerAdapter = AnswerAdapter()
        binding.recyclerView.apply {
            isNestedScrollingEnabled = true
            adapter = answerAdapter
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration =
                DividerItemDecoration(this.context,
                    (layoutManager as LinearLayoutManager).orientation)
            this.addItemDecoration(dividerItemDecoration)
        }
    }

    private fun getAnswer() {
        viewModel.answers.observe(this, {
            when (it) {
                is Resource.Success -> {
                    answerAdapter.differ.submitList(it.data?.data)
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.etAddAnswer.text.clear()
                    binding.etAddAnswer.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    it.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    }
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
                else -> {
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
