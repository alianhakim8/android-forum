package id.alian.forumapp.data.api.response

import id.alian.forumapp.data.model.User

data class RegisterResponse(
    val status: Boolean,
    val message: String,
    val data: User
)
