package id.alian.forumapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import id.alian.forumapp.data.api.response.LoginResponse
import id.alian.forumapp.data.model.User

@Database(
    entities = [LoginResponse::class, User::class],
    version = 1,
)
abstract class ForumDatabase : RoomDatabase() {

    abstract fun forumDao(): ForumDao

    companion object {
        @Volatile
        private var instance: ForumDatabase? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ForumDatabase::class.java,
                "forum.db"
            ).build()
    }
}