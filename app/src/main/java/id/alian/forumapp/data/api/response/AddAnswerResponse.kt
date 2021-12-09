package id.alian.forumapp.data.api.response

import id.alian.forumapp.data.model.Answers

data class AddAnswerResponse(
    val status: Boolean,
    val message: String,
    val data: Answers
)