package id.alian.forumapp.ui.main.view.question

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.snackbar.Snackbar
import id.alian.forumapp.data.model.Question
import id.alian.forumapp.databinding.ActivityDetailQuestionBinding
import id.alian.forumapp.ui.main.adapter.AnswerAdapter
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import id.alian.forumapp.ui.main.viewmodel.factory.MainViewModelFactory
import id.alian.forumapp.utils.Constants.BASE_URL
import id.alian.forumapp.utils.Constants.KEY_QUESTION
import id.alian.forumapp.utils.Constants.TOKEN
import id.alian.forumapp.utils.Resource
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class DetailQuestionActivity : AppCompatActivity(), KodeinAware {

    private lateinit var binding: ActivityDetailQuestionBinding
    private val factory: MainViewModelFactory by instance()
    private lateinit var answerAdapter: AnswerAdapter

    override val kodein by kodein()
    lateinit var viewModel: MainViewModel
    lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val question = intent.getSerializableExtra(KEY_QUESTION) as Question?

        binding.etTitle.text = question?.title
        binding.etDesc.text = question?.description

        token = intent.getStringExtra(TOKEN).toString()

        setupRecyclerView()

        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        viewModel.getAnswers(question_id = question!!.id)
        viewModel.answers.observe(this, {
            when (it) {
                is Resource.Success -> {
                    answerAdapter.differ.submitList(it.data?.data)
                    binding.rvAnswers.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.rvAnswers.visibility = View.INVISIBLE
                    it.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    }
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvAnswers.visibility = View.INVISIBLE
                }
            }
        })

        binding.imageViewDetail.load(BASE_URL + "question/image/${question.image_name}")

        binding.btnAddAnswer.setOnClickListener {
            viewModel.addAnswer("Bearer $token", question.id, binding.etAddAnswer.text.toString().trim())
        }

        viewModel.addAnswers.observe(this, {
            when (it) {
                is Resource.Success -> {
                    viewModel.getAnswers(question.id)
                    binding.etDesc.isEnabled = true
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
            }
        })
    }

    private fun setupRecyclerView() {
        answerAdapter = AnswerAdapter()
        binding.rvAnswers.apply {
            adapter = answerAdapter
            layoutManager = LinearLayoutManager(this@DetailQuestionActivity)
        }
    }
}