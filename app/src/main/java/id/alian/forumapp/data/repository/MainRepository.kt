package id.alian.forumapp.data.repository

import id.alian.forumapp.data.api.ApiService
import id.alian.forumapp.data.db.ForumDatabase
import id.alian.forumapp.data.model.Question
import id.alian.forumapp.data.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MainRepository(
    private val apiService: ApiService,
    private val db: ForumDatabase,
) {
    // REMOTE DATA

    // QUESTION
    suspend fun getQuestions() = apiService.getQuestions()
    suspend fun addQuestionWithImage(
        token: String,
        title: RequestBody,
        description: RequestBody,
        image: MultipartBody.Part,
    ) = apiService.addQuestionWithImage(token, title, description, image)

    suspend fun addQuestion(
        token: String,
        title: String,
        description: String,
    ) = apiService.addQuestion(token, title, description)

    suspend fun getQuestionByUserId(userId: Int) = apiService.getQuestionByUserId(userId)

    suspend fun deleteQuestionById(token: String, id: Int) = apiService.deleteQuestion(token, id)

    suspend fun updateQuestion(token: String, id: Int, title: String, description: String) =
        apiService.updateQuestion(token, id, title, description)

    // PROFILE
    suspend fun getUserProfile(token: String) = apiService.profile(token)

    // ANSWER
    suspend fun getAnswers(question_id: Int) = apiService.getAnswerByQuestionId(question_id)

    suspend fun addAnswer(
        token: String,
        question_id: Int,
        description: String,
    ) = apiService.addAnswer(token, question_id, description)

    // local db
    fun getQuestionLocal() = db.forumDao().getLocalQuestion()

    suspend fun saveQuestionToLocal(question: Question) =
        db.forumDao().saveQuestionToLocal(question)

    suspend fun saveUserToLocal(user: User) =
        db.forumDao().saveUser(user)

    fun getLocalUser() = db.forumDao().getLocalUser()

    suspend fun deleteUser(user: User) = db.forumDao().deleteUser(user)
}