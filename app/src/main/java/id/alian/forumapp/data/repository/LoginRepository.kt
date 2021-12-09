package id.alian.forumapp.data.repository

import id.alian.forumapp.data.api.ApiService
import id.alian.forumapp.data.api.response.LoginResponse
import id.alian.forumapp.data.db.ForumDatabase
import id.alian.forumapp.data.model.User

class LoginRepository(
    private val apiService: ApiService,
    private val db: ForumDatabase
) {
    suspend fun login(email: String, password: String) = apiService.login(email, password)

    suspend fun register(user: User) = apiService.register(user)

    suspend fun saveUserInfo(data: LoginResponse) = db.forumDao().saveToken(data)

    fun getUserInfo() = db.forumDao().getToken()
}