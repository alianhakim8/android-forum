package id.alian.forumapp.data.model

import java.io.Serializable

data class Question(
    val id: Int,
    val user_id: Int,
    val title: String,
    val description: String,
    val image_name: String,
    val user: User
) : Serializable