package id.alian.forumapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
data class Question(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    @ColumnInfo(name = "questionId")
    val questionId: Int,
    val user_id: Int,
    val title: String,
    val description: String,
    val image_name: String,
    @Embedded
    val user: User,
) : Serializable