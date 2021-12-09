package id.alian.forumapp.ui.main.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import id.alian.forumapp.ForumApplication
import id.alian.forumapp.data.api.response.AddAnswerResponse
import id.alian.forumapp.data.api.response.AddQuestionResponse
import id.alian.forumapp.data.api.response.AnswerResponse
import id.alian.forumapp.data.api.response.QuestionResponse
import id.alian.forumapp.data.model.UploadRequestBody
import id.alian.forumapp.data.model.User
import id.alian.forumapp.data.repository.MainRepository
import id.alian.forumapp.utils.Constants
import id.alian.forumapp.utils.Constants.CANNOT_BE_EMPTY
import id.alian.forumapp.utils.Constants.CHOICE_IMAGE
import id.alian.forumapp.utils.Constants.CONVERSION_ERROR
import id.alian.forumapp.utils.Constants.NETWORK_FAILURE
import id.alian.forumapp.utils.Constants.NO_INTERNET
import id.alian.forumapp.utils.Resource
import id.alian.forumapp.utils.getFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class MainViewModel(
    val app: Application,
    private val repository: MainRepository
) : AndroidViewModel(app) {

    // question
    val questions: MutableLiveData<Resource<QuestionResponse>> = MutableLiveData()
    val myQuestion: MutableLiveData<Resource<QuestionResponse>> = MutableLiveData()
    val imageUri: MutableLiveData<Uri> = MutableLiveData()

    // profile
    val profile: MutableLiveData<Resource<User>> = MutableLiveData()

    // answer
    val answers: MutableLiveData<Resource<AnswerResponse>> = MutableLiveData()
    val addAnswers: MutableLiveData<Resource<AddAnswerResponse>> = MutableLiveData()

    // for add question activity
    val uploadedImage: MutableLiveData<Boolean> = MutableLiveData()
    val add: MutableLiveData<Resource<AddQuestionResponse>> = MutableLiveData()
    var selectedImage: Uri? = null

    init {
        getQuestions()
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<ForumApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        if (response.isSuccessful) {
            uploadedImage.postValue(true)
            response.body()?.let { result ->
                return Resource.Success(result)
            }
        }
        return Resource.Error(response.message())
    }

    // question
    private suspend fun safeGetQuestionCall() {
        questions.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.getQuestions()
                questions.postValue(handleResponse(response))
            } else {
                questions.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> questions.postValue(Resource.Error(NETWORK_FAILURE))
                else -> questions.postValue(Resource.Error(CONVERSION_ERROR))
            }
        }
    }

    fun getQuestions() = viewModelScope.launch(Dispatchers.IO) {
        safeGetQuestionCall()
    }

    private suspend fun safeAddQuestionWithImageCall(
        token: String,
        title_: String,
        desc: String,
        file: File
    ) {
        try {
            if (hasInternetConnection()) {
                if (selectedImage == null) {
                    add.postValue(Resource.Error(CHOICE_IMAGE))
                    return
                }
                if (title_.isNotEmpty() && desc.isNotEmpty()) {
                    add.postValue(Resource.Loading())
                    val body = UploadRequestBody(file, "image")

                    val response = repository.addQuestionWithImage(
                        token,
                        image = MultipartBody.Part.createFormData(
                            "image_name",
                            file.name,
                            body
                        ),
                        title = title_.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                        description = desc.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                    )
                    if (response.isSuccessful) {
                        add.postValue(Resource.SuccessNoData())
                        runBlocking {
                            delay(1000L)
                            uploadedImage.postValue(true)
                        }
                    }
                } else {
                    add.postValue(Resource.Error(CANNOT_BE_EMPTY))
                }
            } else {
                add.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> profile.postValue(Resource.Error(NETWORK_FAILURE))
                else -> profile.postValue(Resource.Error(CONVERSION_ERROR))
            }
        }
    }

    fun addQuestionWithImage(token: String, title: String, description: String) {
        val parcelFileDescriptor =
            app.contentResolver.openFileDescriptor(selectedImage!!, "r", null) ?: return
        val file =
            File(app.cacheDir, app.contentResolver.getFileName(selectedImage!!))
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        viewModelScope.launch {
            safeAddQuestionWithImageCall(token, title, description, file)
        }
    }

    fun checkResultCode(resultCode: Int, requestCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.REQUEST_CODE_IMAGE_PICKER -> {
                    selectedImage = data?.data
                    if (selectedImage != null) {
                        imageUri.postValue(selectedImage!!)
                    }
                }
            }
        }
    }

    // profile
    private suspend fun safeGetProfileCall(token: String) {
        profile.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.getUserProfile(token = token)
                profile.postValue(handleResponse(response))
            } else {
                profile.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> profile.postValue(Resource.Error(NETWORK_FAILURE))
                else -> profile.postValue(Resource.Error(CONVERSION_ERROR))
            }
        }
    }

    fun getProfile(token: String) = viewModelScope.launch(Dispatchers.IO) {
        safeGetProfileCall(token)
    }

    // answer
    private suspend fun safeGetQuestionByUserId(userId: Int) {
        myQuestion.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.getQuestionByUserId(userId)
                myQuestion.postValue(handleResponse(response))
            } else {
                myQuestion.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> myQuestion.postValue(Resource.Error(NETWORK_FAILURE))
                else -> myQuestion.postValue(Resource.Error(CONVERSION_ERROR))
            }
        }
    }

    fun getQuestionByUserId(userId: Int) = viewModelScope.launch(Dispatchers.IO) {
        safeGetQuestionByUserId(userId)
    }

    private suspend fun safeGetAnswers(question: Int) {
        answers.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.getAnswers(question)
                answers.postValue(handleResponse(response))
            } else {
                answers.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> profile.postValue(Resource.Error(NETWORK_FAILURE))
                else -> profile.postValue(Resource.Error(CONVERSION_ERROR))
            }
        }
    }

    fun getAnswers(question_id: Int) = viewModelScope.launch(Dispatchers.IO) {
        safeGetAnswers(question_id)
    }

    private suspend fun safeAddAnswerCall(token: String, questionId: Int, description: String) {
        addAnswers.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                if (description.isNotEmpty()) {
                    val response = repository.addAnswer(token, questionId, description)
                    addAnswers.postValue(handleResponse(response))
                } else {
                    addAnswers.postValue(Resource.Error(CANNOT_BE_EMPTY))
                }
            } else {
                addAnswers.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> addAnswers.postValue(Resource.Error(NETWORK_FAILURE))
                else -> addAnswers.postValue(Resource.Error(CONVERSION_ERROR))
            }
        }
    }

    fun addAnswer(token: String, questionId: Int, description: String) =
        viewModelScope.launch(Dispatchers.IO) {
            safeAddAnswerCall(token, questionId, description)
        }
}