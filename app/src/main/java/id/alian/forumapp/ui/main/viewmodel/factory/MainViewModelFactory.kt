package id.alian.forumapp.ui.main.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.alian.forumapp.data.repository.MainRepository
import id.alian.forumapp.ui.main.viewmodel.MainViewModel
import java.lang.IllegalArgumentException

class MainViewModelFactory(
    private val app: Application,
    private val repository: MainRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(app, repository) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}