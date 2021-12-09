package id.alian.forumapp.ui.main.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import id.alian.forumapp.databinding.ActivityMainBinding
import id.alian.forumapp.ui.main.adapter.HomeAdapter
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import id.alian.forumapp.ui.main.viewmodel.factory.MainViewModelFactory
import id.alian.forumapp.utils.Resource
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware {

    private lateinit var binding: ActivityMainBinding
    lateinit var homeAdapter: HomeAdapter

    override val kodein by kodein()

    private val factory: MainViewModelFactory by instance()
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
        setupRecyclerView()

        viewModel.questions.observe(this, {
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    homeAdapter.differ.submitList(it.data?.data)
                    binding.recyclerView.visibility = View.VISIBLE
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.recyclerView.visibility = View.INVISIBLE
                    it.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    }
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.INVISIBLE
                }
            }
        })
    }

    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter()
        binding.recyclerView.apply {
            adapter = homeAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
}