package id.alian.forumapp.data.db

import androidx.room.*
import id.alian.forumapp.data.model.Question
import id.alian.forumapp.data.model.User

@Dao
interface ForumDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveUser(user: User)

    @Query("SELECT * FROM user")
    fun getLocalUser(): User

    @Query("SELECT * FROM question")
    fun getLocalQuestion(): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuestionToLocal(question: Question)

    @Delete
    suspend fun deleteUser(user: User)

}