package id.alian.forumapp.ui.main.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import id.alian.forumapp.ForumApplication
import id.alian.forumapp.data.api.response.LoginResponse
import id.alian.forumapp.data.api.response.RegisterResponse
import id.alian.forumapp.data.model.User
import id.alian.forumapp.data.repository.LoginRepository
import id.alian.forumapp.utils.Constants
import id.alian.forumapp.utils.Constants.Hint_Empty_Field
import id.alian.forumapp.utils.Constants.Error_Conversion_Error
import id.alian.forumapp.utils.Constants.Error_Network_Failure
import id.alian.forumapp.utils.Constants.Error_No_Internet
import id.alian.forumapp.utils.Constants.Extra_Token
import id.alian.forumapp.utils.Constants.Shared_Token_Pref
import id.alian.forumapp.utils.Resource
import id.alian.forumapp.utils.ResponseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel(
    val app: Application,
    private val repository: LoginRepository
) : AndroidViewModel(app) {

    val login = MutableLiveData<Resource<LoginResponse>>()
    val register = MutableLiveData<Resource<RegisterResponse>>()
    val isLoggedIn = MutableLiveData<Boolean>()

    private val sharedPref: SharedPreferences = app.applicationContext.getSharedPreferences(
        Shared_Token_Pref,
        Context.MODE_PRIVATE
    )

    init {
        checkLogin()
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
                        with(sharedPref.edit()) {
                            putString(Extra_Token, response.body()!!.data)
                            apply()
                        }
                                      }
                } else {
                    login.postValue(Resource.Error(Hint_Empty_Field))
                }
            } else {
                login.postValue(Resource.Error(Error_No_Internet))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> login.postValue(Resource.Error(Error_Network_Failure))
                else -> login.postValue(Resource.Error(Error_Conversion_Error))
            }
        }
    }

    private suspend fun safeRegisterCall(user: User) {
        register.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                if (user.name.isBlank() || user.password.isBlank() || user.passwordConfirmation.isBlank()) {
                    register.postValue(Resource.Error(Hint_Empty_Field))
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
                is IOException -> register.postValue(Resource.Error(Error_Network_Failure))
                else -> register.postValue(Resource.Error(Constants.Error_Conversion_Error))
            }
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        safeLoginCall(email, password)
    }

    fun register(user: User) = viewModelScope.launch(Dispatchers.IO) {
        safeRegisterCall(user)
    }

    private fun checkLogin() {
        val sharedPref =
            app.applicationContext.getSharedPreferences(
                Shared_Token_Pref,
                Context.MODE_PRIVATE
            ) ?: return
        val token = sharedPref.getString(Extra_Token, null)
        if (token != null) {
            isLoggedIn.postValue(true)
        }
    }
}