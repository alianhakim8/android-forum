package id.alian.forumapp.data.model

data class Answers(
    val id: Int,
    val question_id: Int,
    val description: String,
    val created_at: String,
    val update_at: String,
    val user: User
)
