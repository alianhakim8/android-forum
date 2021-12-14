package id.alian.forumapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.alian.forumapp.R
import id.alian.forumapp.databinding.FragmentProfileBinding
import id.alian.forumapp.ui.main.adapter.QuestionAdapter
import id.alian.forumapp.ui.main.view.HomeActivity
import id.alian.forumapp.ui.main.view.auth.LoginActivity
import id.alian.forumapp.ui.main.view.question.DetailQuestionActivity
import id.alian.forumapp.ui.main.view.question.UpdateQuestionActivity
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import id.alian.forumapp.utils.Constants
import id.alian.forumapp.utils.Constants.Extra_Question
import id.alian.forumapp.utils.Resource
import id.alian.forumapp.utils.snackBar

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel
    private lateinit var questionAdapter: QuestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = (activity as HomeActivity).viewModel
        setupRecyclerView()

        // view binding
        binding.refresh.setOnRefreshListener {
            viewModel.getProfile()
        }

        binding.btnLogout.setOnClickListener {
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1)
                    .setTitle(resources.getString(R.string.label_title))
                    .setMessage("apakah anda yakin akan keluar ?")
                    .setNegativeButton("Batal") { _, _ ->
                        // Respond to neutral button press
                    }
                    .setPositiveButton("Keluar") { _, _ ->
                        // Respond to positive button press
                        viewModel.logout()
                        Intent(context, LoginActivity::class.java).also {
                            startActivity(it)
                            (activity as HomeActivity).finish()
                        }
                    }
                    .show()
            }
        }

        // viewModel
        viewModel.profile.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    binding.refresh.isRefreshing = false
                    binding.tvName.text = it.data?.name
                }

                is Resource.Loading -> {
                    binding.refresh.isRefreshing = true
                }

                is Resource.Error -> {
                    binding.refresh.isRefreshing = false
                    binding.root.snackBar(it.message!!)
                }
                else -> {}
            }
        })

        viewModel.myQuestion.observe(this, {
            when (it) {
                is Resource.Success -> {
                    questionAdapter.differ.submitList(it.data?.data)
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.refresh.isRefreshing = false
                }

                is Resource.Loading -> {
                    binding.refresh.isRefreshing = false
                    binding.recyclerView.visibility = View.INVISIBLE
                }
                else -> {}
            }
        })

        viewModel.updateActivity.observe(this, { question ->
            Intent(context, UpdateQuestionActivity::class.java).also {
                it.putExtra(Extra_Question, question)
                startActivity(it)
            }
        })

        questionAdapter.setOnLongItemClickListener { question ->
            val items = arrayOf("Hapus", "Ubah")
            context?.let { dialog1 ->
                MaterialAlertDialogBuilder(dialog1)
                    .setTitle("Aksi")
                    .setItems(items) { dialog, which ->
                        // Respond to item chosen
                        viewModel.checkItemQuestionDialog(which, question)
                        dialog.cancel()
                    }
                    .show()
            }
        }

        questionAdapter.setOnItemClickListener { question ->
            Intent(context, DetailQuestionActivity::class.java).also {
                it.putExtra(Constants.Extra_Question, question)
                startActivity(it)
            }
        }
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