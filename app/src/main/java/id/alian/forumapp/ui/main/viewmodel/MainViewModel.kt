package id.alian.forumapp.ui.main.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import id.alian.forumapp.ForumApplication
import id.alian.forumapp.data.api.response.*
import id.alian.forumapp.data.model.Question
import id.alian.forumapp.data.model.UploadRequestBody
import id.alian.forumapp.data.model.User
import id.alian.forumapp.data.repository.MainRepository
import id.alian.forumapp.utils.Constants
import id.alian.forumapp.utils.Constants.Error_Conversion_Error
import id.alian.forumapp.utils.Constants.Error_Network_Failure
import id.alian.forumapp.utils.Constants.Error_No_Internet
import id.alian.forumapp.utils.Constants.Extra_Token
import id.alian.forumapp.utils.Constants.Hint_Choice_Image
import id.alian.forumapp.utils.Constants.Hint_Empty_Field
import id.alian.forumapp.utils.Constants.Log_Main_ViewModel
import id.alian.forumapp.utils.Constants.Shared_Token_Pref
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
    private val repository: MainRepository,
) : AndroidViewModel(app) {

    // question
    val questions = MutableLiveData<Resource<QuestionResponse>>()
    val myQuestion = MutableLiveData<Resource<QuestionResponse>>()
    val imageUri = MutableLiveData<Uri>()
    val updateQuestion = MutableLiveData<Resource<UpdateQuestionResponse>>()


    // profile
    val profile = MutableLiveData<Resource<User>>()

    // answer
    val answers = MutableLiveData<Resource<AnswerResponse>>()
    val addAnswers = MutableLiveData<Resource<AddAnswerResponse>>()

    // for add question activity
    val uploadedImage = MutableLiveData<Boolean>()
    val add = MutableLiveData<Resource<AddQuestionResponse>>()
    var selectedImage: Uri? = null

    // shared preferences for get jwt token
    private val sharedPref: SharedPreferences =
        app.applicationContext.getSharedPreferences(Shared_Token_Pref, Context.MODE_PRIVATE)
    private val token = sharedPref.getString(Extra_Token, null)

    // dialog question on profile fragment
    val updateActivity = MutableLiveData<Question>()

    init {
        getQuestions()
        getProfile()
    }

    // check internet connection
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

    // handling response
    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        if (response.isSuccessful) {
            uploadedImage.postValue(true)
            response.body()?.let { result ->
                return Resource.Success(result)
            }
        }
        return Resource.Error(response.message())
    }

    // safe API Call
    private suspend fun safeGetQuestionCall() {
        questions.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.getQuestions()
                if (response.isSuccessful) {
                    questions.postValue(handleResponse(response))
                    response.body()?.data?.forEach {
                        repository.saveQuestionToLocal(it)
                    }
                } else {
                    repository.getQuestionLocal().also {
                        val oldQuestion = QuestionResponse(true, "Retrieve all question", it)
                        questions.postValue(Resource.Success(oldQuestion))
                    }
                }
            } else {
                questions.postValue(Resource.Error(Error_No_Internet))
            }
        } catch (t: Throwable) {
            Log.d(Log_Main_ViewModel, "safeGetQuestionCall: ${t.localizedMessage}")
            repository.getQuestionLocal()
            when (t) {
                is IOException -> questions.postValue(Resource.Error(Error_Network_Failure))
                else -> questions.postValue(Resource.Error(Error_Conversion_Error))
            }
        }
    }

    private suspend fun safeAddQuestionWithImageCall(
        token: String, title_: String, desc: String, file: File,
    ) {
        try {
            if (hasInternetConnection()) {
                if (selectedImage == null) {
                    add.postValue(Resource.Error(Hint_Choice_Image))
                    return
                } else {
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
                        add.postValue(Resource.Error(Hint_Empty_Field))
                    }
                }

            } else {
                add.postValue(Resource.Error(Error_No_Internet))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> profile.postValue(Resource.Error(Error_Network_Failure))
                else -> profile.postValue(Resource.Error(Error_Conversion_Error))
            }
        }
    }

    private suspend fun safeAddQuestion(
        token: String, title: String, description: String,
    ) {
        add.postValue(Resource.Loading())
        if (title.isEmpty() && description.isEmpty()) {
            add.postValue(Resource.Error("tidak boleh kosong"))
        } else {
            val response = repository.addQuestion(token, title, description)
            handleResponse(response)
            add.postValue(Resource.SuccessNoData())
        }
    }

    private fun safeDeleteQuestionCall(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteQuestionById("Bearer $token", question.questionId)
            getProfile()
        }
    }

    private suspend fun safeGetProfileCall(token: String) {
        profile.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.getUserProfile(token)
                if (response.isSuccessful) {
                    response.body()?.id?.let {
                        getQuestionByUserId(it)
                        repository.saveUserToLocal(response.body()!!)
                        profile.postValue(Resource.Success(response.body()!!))
                    }
                } else {
                    repository.getLocalUser().also {
                        profile.postValue(Resource.Success(it))
                    }
                }
            } else {
                profile.postValue(Resource.Error(Error_No_Internet))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> profile.postValue(Resource.Error(Error_Network_Failure))
                else -> profile.postValue(Resource.Error(Error_Conversion_Error))
            }
        }
    }

    private suspend fun safeGetQuestionByUserId(userId: Int) {
        myQuestion.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.getQuestionByUserId(userId)
                myQuestion.postValue(handleResponse(response))
            } else {
                myQuestion.postValue(Resource.Error(Error_No_Internet))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> myQuestion.postValue(Resource.Error(Error_Network_Failure))
                else -> myQuestion.postValue(Resource.Error(Error_Conversion_Error))
            }
        }
    }

    private suspend fun safeUpdateQuestion(
        id: Int,
        title: String,
        description: String,
    ) {
        updateQuestion.postValue(Resource.Loading())
        if (title.isEmpty() || description.isEmpty()) {
            updateQuestion.postValue(Resource.Error("tidak boleh kosong"))
        } else {
            val response =
                repository.updateQuestion("Bearer $token",
                    id,
                    title,
                    description)
            if (response.isSuccessful) {
                handleResponse(response)
            } else {
                updateQuestion.postValue(Resource.Error(response.message()))
            }
        }
    }

    private suspend fun safeAddAnswerCall(token: String, questionId: Int, description: String) {
        addAnswers.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                if (description.isNotEmpty()) {
                    val response = repository.addAnswer(token, questionId, description)
                    addAnswers.postValue(handleResponse(response))
                } else {
                    addAnswers.postValue(Resource.Error(Hint_Empty_Field))
                }
            } else {
                addAnswers.postValue(Resource.Error(Error_No_Internet))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> addAnswers.postValue(Resource.Error(Error_Network_Failure))
                else -> addAnswers.postValue(Resource.Error(Error_Conversion_Error))
            }
        }
    }

    private suspend fun safeGetAnswers(question: Int) {
        try {
            if (hasInternetConnection()) {
                answers.postValue(Resource.Loading())
                val response = repository.getAnswers(question)
                answers.postValue(handleResponse(response))

            } else {
                answers.postValue(Resource.Error(Error_No_Internet))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> profile.postValue(Resource.Error(Error_Network_Failure))
                else -> profile.postValue(Resource.Error(Error_Conversion_Error))
            }
        }
    }

    // Implement Method
    fun getQuestions() = viewModelScope.launch(Dispatchers.IO) {
        safeGetQuestionCall()
    }

    fun addQuestionWithImage(title: String, description: String) {
        if (selectedImage != null) {
            val parcelFileDescriptor =
                app.contentResolver.openFileDescriptor(selectedImage!!, "r", null) ?: return
            val file =
                File(app.cacheDir, app.contentResolver.getFileName(selectedImage!!))
            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            if (token != null) {
                viewModelScope.launch {
                    safeAddQuestionWithImageCall("Bearer $token", title, description, file)
                }
            }
        } else {
            addQuestion(title, description)
        }
    }

    private fun addQuestion(title: String, description: String) {
        if (token != null) {
            viewModelScope.launch(Dispatchers.IO) {
                safeAddQuestion("Bearer $token", title, description)
            }
        }
    }

    fun checkResultCode(resultCode: Int, requestCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.Request_Code_Image_Picker -> {
                    selectedImage = data?.data
                    if (selectedImage != null) {
                        imageUri.postValue(selectedImage!!)
                    }
                }
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            if (token != null) {
                safeGetProfileCall("Bearer $token")
            }
        }
    }

    private fun getQuestionByUserId(userId: Int) = viewModelScope.launch(Dispatchers.IO) {
        safeGetQuestionByUserId(userId)
    }

    fun updateQuestion(id: Int, title: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            safeUpdateQuestion(id, title, description)
        }
    }

    fun getAnswers(question_id: Int) = viewModelScope.launch(Dispatchers.IO) {
        safeGetAnswers(question_id)
    }

    // ADD answer
    fun addAnswer(questionId: Int, description: String) {
        if (token != null) {
            viewModelScope.launch(Dispatchers.IO) {
                safeAddAnswerCall("Bearer $token", questionId, description)
            }
        }
    }

    fun checkItemQuestionDialog(value: Int, question: Question) {
        when (value) {
            0 -> {
                // delete question
                if (token != null) {
                    safeDeleteQuestionCall(question)
                }
            }

            1 -> {
                updateActivity.postValue(question)
            }
        }
    }

    fun logout() {
        with(sharedPref.edit()) {
            remove(Extra_Token)
            apply()
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.getLocalUser().also {
                repository.deleteUser(it)
            }
        }
    }
}