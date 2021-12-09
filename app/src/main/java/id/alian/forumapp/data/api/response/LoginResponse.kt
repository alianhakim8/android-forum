package id.alian.forumapp.data.api.response

import androidx.room.*

@Entity(
    tableName = "login_response",
)
data class LoginResponse(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val status: Boolean,
    val message: String,
    val data: String
)
