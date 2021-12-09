package id.alian.forumapp.ui.main.view.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import id.alian.forumapp.data.model.User
import id.alian.forumapp.databinding.ActivityRegisterBinding
import id.alian.forumapp.ui.main.viewmodel.LoginViewModel
import id.alian.forumapp.ui.main.viewmodel.factory.LoginViewModelFactory
import id.alian.forumapp.utils.Resource
import id.alian.forumapp.utils.snackBar
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class RegisterActivity : AppCompatActivity(), KodeinAware {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: LoginViewModel
    override val kodein by kodein()

    private val factory: LoginViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        binding.btnRegister.setOnClickListener {
            val newUser = User(
                id = 0,
                name = binding.etName.text.toString().trim(),
                email = binding.etEmail.text.toString().trim(),
                password = binding.etPassword.text.toString().trim(),
                passwordConfirmation = binding.etConfirmPassword.text.toString().trim()
            )
            viewModel.register(newUser)
        }

        viewModel.register.observe(this, {
            when (it) {
                is Resource.Success -> {
                    binding.btnRegister.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.root.snackBar(it.message!!)
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.btnRegister.visibility = View.VISIBLE
                    binding.root.snackBar(it.message!!)
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.visibility = View.INVISIBLE
                }
            }
        })
    }
}