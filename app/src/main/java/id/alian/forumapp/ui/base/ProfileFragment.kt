package id.alian.forumapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import id.alian.forumapp.R
import id.alian.forumapp.databinding.FragmentProfileBinding
import id.alian.forumapp.ui.main.adapter.QuestionAdapter
import id.alian.forumapp.ui.main.view.HomeActivity
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import id.alian.forumapp.utils.Resource
import id.alian.forumapp.utils.snackBar

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel
    private lateinit var questionAdapter: QuestionAdapter
    lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = (activity as HomeActivity).viewModel
        token = (activity as HomeActivity).token
        setupRecyclerView()

        viewModel.getProfile("Bearer $token")

        binding.refresh.setOnRefreshListener {
            viewModel.getProfile("Bearer $token")
        }

        viewModel.profile.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    binding.refresh.isRefreshing = false
                    binding.tvName.text = it.data?.name
                    viewModel.getQuestionByUserId(it.data?.id!!)
                }

                is Resource.Loading -> {
                    binding.refresh.isRefreshing = true
                }

                is Resource.Error -> {
                    binding.refresh.isRefreshing = false
                    binding.root.snackBar(it.message!!)
                }
            }
        })

        viewModel.myQuestion.observe(this, {
            when (it) {
                is Resource.Success -> {
                    questionAdapter.differ.submitList(it.data?.data)
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
        })

        return view
    }

    private fun setupRecyclerView() {
        questionAdapter = QuestionAdapter()
        binding.recyclerView.apply {
            adapter = questionAdapter
            layoutManager = GridLayoutManager(activity, 2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}