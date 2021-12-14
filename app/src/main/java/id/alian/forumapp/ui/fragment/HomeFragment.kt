package id.alian.forumapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import id.alian.forumapp.R
import id.alian.forumapp.databinding.FragmentHomeBinding
import id.alian.forumapp.ui.main.adapter.HomeAdapter
import id.alian.forumapp.ui.main.view.HomeActivity
import id.alian.forumapp.ui.main.view.question.AddQuestionActivity
import id.alian.forumapp.ui.main.view.question.DetailQuestionActivity
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import id.alian.forumapp.utils.Constants.Extra_Question
import id.alian.forumapp.utils.Constants.Extra_Token
import id.alian.forumapp.utils.Constants.Log_Home_Fragment
import id.alian.forumapp.utils.Resource

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel
    private lateinit var questionAdapter: HomeAdapter
    lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.swipeUpRefresh.setOnRefreshListener {
            viewModel.getQuestions();
        }

        viewModel = (activity as HomeActivity).viewModel
        token = (activity as HomeActivity).token
        setupRecyclerView()
        questionUI()

        questionAdapter.setOnItemClickListener { question ->
            Intent(context, DetailQuestionActivity::class.java).also {
                it.putExtra(Extra_Token, token)
                it.putExtra(Extra_Question, question)
                startActivity(it)
            }
        }
        return view
    }

    private fun setupRecyclerView() {
        questionAdapter = HomeAdapter()
        binding.recyclerView.apply {
            adapter = questionAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun questionUI() {
        viewModel.questions.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Log.d(Log_Home_Fragment, "questionUI: $it")
                    questionAdapter.differ.submitList(it.data?.data)
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.swipeUpRefresh.isRefreshing = false
                }

                is Resource.Error -> {
                    binding.swipeUpRefresh.isRefreshing = false
                    binding.recyclerView.visibility = View.INVISIBLE
                    it.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    }
                }

                is Resource.Loading -> {
                    binding.swipeUpRefresh.isRefreshing = true
                    binding.recyclerView.visibility = View.INVISIBLE
                }
            }
        })
        binding.fab.setOnClickListener {
            Intent(context, AddQuestionActivity::class.java).also {
                it.putExtra(Extra_Token, token)
                startActivity(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}