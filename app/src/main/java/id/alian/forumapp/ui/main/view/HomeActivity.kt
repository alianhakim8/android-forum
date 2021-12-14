package id.alian.forumapp.ui.main.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import id.alian.forumapp.R
import id.alian.forumapp.databinding.ActivityHomeBinding
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import id.alian.forumapp.ui.main.viewmodel.factory.MainViewModelFactory
import id.alian.forumapp.utils.Constants.Extra_Token
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class HomeActivity : AppCompatActivity(), KodeinAware {

    private lateinit var binding: ActivityHomeBinding

    override val kodein by kodein()

    private val factory: MainViewModelFactory by instance()

    lateinit var viewModel: MainViewModel
    lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = intent.getStringExtra(Extra_Token).toString()

        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        NavigationUI.setupWithNavController(
            binding.bottomNavigationView,
            navHostFragment!!.navController
        )
    }
}