package id.alian.forumapp.data.api.response

import id.alian.forumapp.data.model.Question

data class QuestionResponse(
    val status:Boolean,
    val message : String,
    val data : List<Question>
)