package id.alian.forumapp.ui.main.view.question

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import id.alian.forumapp.R
import id.alian.forumapp.data.model.Question
import id.alian.forumapp.databinding.ActivityUpdateQuestionBinding
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import id.alian.forumapp.ui.main.viewmodel.factory.MainViewModelFactory
import id.alian.forumapp.utils.Constants
import id.alian.forumapp.utils.Resource
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class UpdateQuestionActivity : AppCompatActivity(), KodeinAware {

    private lateinit var binding: ActivityUpdateQuestionBinding
    private val factory: MainViewModelFactory by instance()
    private lateinit var viewModel: MainViewModel

    override val kodein by kodein()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        val question = intent.getSerializableExtra(Constants.Extra_Question) as Question?

        binding.etDesc.setText(question?.description)
        binding.etTitle.setText(question?.title)

        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.done -> {
                    val newTitle = binding.etTitle.text.toString().trim()
                    val newDesc = binding.etDesc.text.toString().trim()
                    viewModel.updateQuestion(question?.questionId!!, newTitle, newDesc)
                    true
                }
                else -> false
            }
        }

        viewModel.updateQuestion.observe(this, {
            when (it) {
                is Resource.Success -> {
                    finish()
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.etDesc.isEnabled = false
                    binding.etTitle.isEnabled = false
                    binding.topAppBar.isEnabled = false
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.etDesc.isEnabled = true
                    binding.etTitle.isEnabled = true
                    binding.topAppBar.isEnabled = true
                    it.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}