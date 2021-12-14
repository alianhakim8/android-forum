package id.alian.forumapp.ui.main.view.question

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import id.alian.forumapp.data.model.UploadRequestBody.UploadCallback
import id.alian.forumapp.databinding.ActivityAddQuestionBinding
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import id.alian.forumapp.ui.main.viewmodel.factory.MainViewModelFactory
import id.alian.forumapp.utils.Constants.Request_Code_Image_Picker
import id.alian.forumapp.utils.Resource
import id.alian.forumapp.utils.hideKeyboard
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class AddQuestionActivity : AppCompatActivity(), KodeinAware, UploadCallback {

    private lateinit var binding: ActivityAddQuestionBinding

    override val kodein by kodein()

    private val factory: MainViewModelFactory by instance()

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        binding.btnAddQuestion.setOnClickListener {
            viewModel.addQuestionWithImage(
                title = binding.etTitle.text.toString().trim(),
                description = binding.etDesc.text.toString().trim()
            )
        }

        binding.imageViewQuestion.setOnClickListener {
            Intent(Intent.ACTION_PICK).also {
                it.type = "image/*"
                val mimeTypes = arrayOf("image/jpg", "image/png")
                it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                startActivityForResult(it, Request_Code_Image_Picker)
            }
        }

        viewModel.add.observe(this, {
            when (it) {
                is Resource.SuccessNoData -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    viewModel.uploadedImage.observe(this, {
                        finish()
                    })
                }

                is Resource.Error -> {
                    hideKeyboard(binding.root)
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.btnAddQuestion.visibility = View.VISIBLE
                    it.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    }
                    binding.etDesc.isEnabled = true
                    binding.etTitle.isEnabled = true
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnAddQuestion.visibility = View.INVISIBLE
                    binding.etDesc.isEnabled = false
                    binding.etTitle.isEnabled = false
                }
                else -> {}
            }
        })

        viewModel.imageUri.observe(this, {
            binding.imageViewQuestion.setImageURI(it)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.checkResultCode(resultCode, requestCode, data)
    }

    override fun onProgressUpdate(percentage: Int) {
        binding.progressBar.progress = percentage
    }
}