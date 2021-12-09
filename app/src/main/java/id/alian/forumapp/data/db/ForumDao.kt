package id.alian.forumapp.data.db

import androidx.room.*
import id.alian.forumapp.data.api.response.LoginResponse
import id.alian.forumapp.data.model.User

@Dao
interface ForumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(data: LoginResponse)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveUser(user: User)

    @Query("SELECT * FROM login_response")
    fun getToken(): LoginResponse

    @Query("SELECT * FROM user")
    fun getUser(): User

    @Delete
    suspend fun deleteToken(data: LoginResponse)

}