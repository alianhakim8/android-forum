package id.alian.forumapp.ui.main.view.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import id.alian.forumapp.databinding.ActivityLoginBinding
import id.alian.forumapp.ui.main.view.HomeActivity
import id.alian.forumapp.ui.main.viewmodel.LoginViewModel
import id.alian.forumapp.ui.main.viewmodel.factory.LoginViewModelFactory
import id.alian.forumapp.utils.Resource
import id.alian.forumapp.utils.hideKeyboard
import id.alian.forumapp.utils.snackBar
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class LoginActivity : AppCompatActivity(), KodeinAware {

    private lateinit var binding: ActivityLoginBinding
    override val kodein by kodein()

    private val factory: LoginViewModelFactory by instance()
    lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        // view bind
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            viewModel.login(email, pass)
        }

        binding.btnToRegister.setOnClickListener {
            Intent(this, RegisterActivity::class.java).also {
                startActivity(it)
            }
        }

        // viewModel
        viewModel.login.observe(this, {
            when (it) {
                is Resource.Success -> {
                    binding.btnLogin.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                    Intent(this, HomeActivity::class.java).also {
                        startActivity(it)
                        finish()
                    }
                }

                is Resource.Error -> {
                    hideKeyboard(binding.root)
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.btnLogin.visibility = View.VISIBLE
                    binding.root.snackBar(it.message!!)
                    binding.btnToRegister.isEnabled = true
                }

                is Resource.Loading -> {
                    hideKeyboard(binding.root)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.visibility = View.INVISIBLE
                    binding.btnToRegister.isEnabled = false
                }
                else -> {}
            }
        })

        viewModel.isLoggedIn.observe(this, {
            Intent(this, HomeActivity::class.java).also { intent_ ->
                startActivity(intent_)
                finish()
            }
        })
    }
}