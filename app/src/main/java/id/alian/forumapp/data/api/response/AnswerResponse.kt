package id.alian.forumapp.data.api.response

import id.alian.forumapp.data.model.Answers

data class AnswerResponse(
    val status : Boolean,
    val message : String,
    val data : List<Answers>
)
