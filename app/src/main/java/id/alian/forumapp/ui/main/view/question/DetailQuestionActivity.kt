package id.alian.forumapp.ui.main.view.question

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import id.alian.forumapp.data.model.Question
import id.alian.forumapp.databinding.ActivityDetailQuestionBinding
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import id.alian.forumapp.ui.main.viewmodel.factory.MainViewModelFactory
import id.alian.forumapp.utils.Constants.Extra_Question
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class DetailQuestionActivity : AppCompatActivity(), KodeinAware {

    private lateinit var binding: ActivityDetailQuestionBinding
    private val factory: MainViewModelFactory by instance()

    override val kodein by kodein()
    lateinit var viewModel: MainViewModel
    var question: Question? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        question = intent.getSerializableExtra(Extra_Question) as Question?
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }
}



