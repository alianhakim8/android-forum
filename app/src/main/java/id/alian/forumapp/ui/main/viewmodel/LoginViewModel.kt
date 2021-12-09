package id.alian.forumapp.ui.main.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import id.alian.forumapp.ForumApplication
import id.alian.forumapp.data.api.response.LoginResponse
import id.alian.forumapp.data.api.response.RegisterResponse
import id.alian.forumapp.data.model.User
import id.alian.forumapp.data.repository.LoginRepository
import id.alian.forumapp.utils.Constants
import id.alian.forumapp.utils.Constants.CANNOT_BE_EMPTY
import id.alian.forumapp.utils.Constants.CONVERSION_ERROR
import id.alian.forumapp.utils.Constants.LOGIN_VIEW_MODEL
import id.alian.forumapp.utils.Constants.NETWORK_FAILURE
import id.alian.forumapp.utils.Constants.NO_INTERNET
import id.alian.forumapp.utils.Constants.TOKEN
import id.alian.forumapp.utils.Constants.TOKEN_PREF_KEY
import id.alian.forumapp.utils.Resource
import id.alian.forumapp.utils.ResponseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel(
    val app: Application,
    private val repository: LoginRepository
) : AndroidViewModel(app) {

    val login: MutableLiveData<Resource<LoginResponse>> = MutableLiveData()
    val register: MutableLiveData<Resource<RegisterResponse>> = MutableLiveData()
    val isLoggedIn: MutableLiveData<Boolean> = MutableLiveData()

    init {
        getTokenInfo()
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<ForumApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private suspend fun safeLoginCall(email: String, password: String) {
        login.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                if (email.isNotEmpty() || password.isNotEmpty()) {
                    val response = repository.login(email, password)
                    login.postValue(ResponseHelper().handleResponse(response))
                    response.body()?.let {
                        val sharedPref =
                            app.applicationContext.getSharedPreferences(
                                TOKEN_PREF_KEY,
                                Context.MODE_PRIVATE
                            ) ?: return
                        with(sharedPref.edit()) {
                            putString(TOKEN, response.body()!!.data)
                            apply()
                        }
                        saveUserInfo(it)
                    }
                } else {
                    login.postValue(Resource.Error(CANNOT_BE_EMPTY))
                }
            } else {
                login.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> login.postValue(Resource.Error(NETWORK_FAILURE))
                else -> login.postValue(Resource.Error(CONVERSION_ERROR))
            }
        }
    }

    private suspend fun safeRegisterCall(user: User) {
        register.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                if (user.name.isBlank() || user.password.isBlank() || user.passwordConfirmation.isBlank()) {
                    register.postValue(Resource.Error(CANNOT_BE_EMPTY))
                } else if (user.passwordConfirmation != user.password) {
                    register.postValue(Resource.Error("password harus sama"))
                } else if (user.password.length < 8) {
                    register.postValue(Resource.Error("password sangat lemah"))
                } else {
                    val response = repository.register(user)
                    register.postValue(ResponseHelper().handleResponse(response))
                }
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> register.postValue(Resource.Error(NETWORK_FAILURE))
                else -> register.postValue(Resource.Error(Constants.CONVERSION_ERROR))
            }
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        safeLoginCall(email, password)
    }

    fun register(user: User) = viewModelScope.launch(Dispatchers.IO) {
        safeRegisterCall(user)
    }

    private fun saveUserInfo(data: LoginResponse) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveUserInfo(data)
    }

    private fun getTokenInfo() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val data = repository.getUserInfo()
            if (data.data.isNotEmpty()) {
                isLoggedIn.postValue(true)
            }
        } catch (t: Throwable) {
            Log.d(LOGIN_VIEW_MODEL, "getTokenInfo: ${t.message}")
        }
    }

    fun checkLogin() {
        val sharedPref =
            app.applicationContext.getSharedPreferences(
                TOKEN_PREF_KEY,
                Context.MODE_PRIVATE
            ) ?: return
        val token = sharedPref.getString(TOKEN, "")
        Log.d(LOGIN_VIEW_MODEL, "checkLogin: $token")
    }
}