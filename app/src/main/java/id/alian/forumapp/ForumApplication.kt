package id.alian.forumapp

import android.app.Application
import id.alian.forumapp.data.api.ApiService
import id.alian.forumapp.data.db.ForumDatabase
import id.alian.forumapp.data.repository.LoginRepository
import id.alian.forumapp.data.repository.MainRepository
import id.alian.forumapp.ui.main.viewmodel.factory.LoginViewModelFactory
import id.alian.forumapp.ui.main.viewmodel.factory.MainViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.*

class ForumApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@ForumApplication))
        bind() from singleton { ApiService() }
        bind() from singleton { ForumDatabase(instance()) }
        bind() from singleton { MainRepository(instance(), instance()) }
        bind() from singleton { LoginRepository(instance(), instance()) }
        bind() from provider { MainViewModelFactory(instance(), instance()) }
        bind() from provider { LoginViewModelFactory(instance(), instance()) }
    }
}