package id.alian.forumapp.ui.main.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.alian.forumapp.data.repository.LoginRepository
import id.alian.forumapp.ui.main.viewmodel.LoginViewModel
import java.lang.IllegalArgumentException

class LoginViewModelFactory(
    private val app: Application,
    private val repository: LoginRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(app, repository) as T
        }
        throw IllegalArgumentException("unknown class name")
    }
}