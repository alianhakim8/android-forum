package id.alian.forumapp.data.repository

import id.alian.forumapp.data.api.ApiService
import id.alian.forumapp.data.api.response.LoginResponse
import id.alian.forumapp.data.db.ForumDatabase
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MainRepository(
    private val apiService: ApiService,
    private val db: ForumDatabase
) {
    // remote data
    suspend fun getQuestions() = apiService.getQuestions()

    suspend fun addQuestionWithImage(
        token: String,
        title: RequestBody,
        description: RequestBody,
        image: MultipartBody.Part
    ) = apiService.addQuestionWithImage(token, title, description, image)

    suspend fun getUserProfile(token: String) = apiService.profile(token)

    suspend fun getQuestionByUserId(
        userId: Int
    ) = apiService.getQuestionByUserId(userId)

    suspend fun getAnswers(
        question_id: Int
    ) = apiService.getAnswerByQuestionId(question_id)

    suspend fun addAnswer(
        token: String,
        question_id: Int,
        description: String
    ) = apiService.addAnswer(token, question_id, description)

    // local db
    fun getTokenInfo() = db.forumDao().getToken()

    fun getUserInfo() = db.forumDao().getUser()

    suspend fun deleteUserInfo(data: LoginResponse) = db.forumDao().deleteToken(data)
}