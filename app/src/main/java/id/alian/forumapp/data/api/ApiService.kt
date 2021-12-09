package id.alian.forumapp.data.api

import id.alian.forumapp.data.api.response.*
import id.alian.forumapp.data.model.User
import id.alian.forumapp.utils.Constants.ANSWER
import id.alian.forumapp.utils.Constants.AUTH
import id.alian.forumapp.utils.Constants.BASE_URL
import id.alian.forumapp.utils.Constants.HEADER_AUTH
import id.alian.forumapp.utils.Constants.QUESTION
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    companion object {
        operator fun invoke(): ApiService {
            val okHttpClient = OkHttpClient.Builder()
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
                .create(ApiService::class.java)
        }
    }

    // auth route
    @FormUrlEncoded
    @POST("$AUTH/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("$AUTH/register")
    suspend fun register(
        @Body user: User
    ): Response<RegisterResponse>

    @GET("$AUTH/profile")
    @Headers("Accept: application/json")
    suspend fun profile(
        @Header(HEADER_AUTH) token: String,
    ): Response<User>

    // question route
    @GET("$QUESTION/all")
    suspend fun getQuestions(): Response<QuestionResponse>

    @POST("$QUESTION/add")
    @Multipart
    suspend fun addQuestionWithImage(
        @Header(HEADER_AUTH) token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part,
    ): Response<Unit>

    @GET("$QUESTION/{userId}")
    suspend fun getQuestionByUserId(
        @Path("userId") userId: Int
    ): Response<QuestionResponse>

    @GET("$QUESTION/image/{imageName}")
    suspend fun getQuestionImage(
        @Path("imageName") imageName: String
    ): Response<Unit>

    // answer Route
    @GET("$ANSWER/{question_id}")
    suspend fun getAnswerByQuestionId(
        @Path("question_id") questionId: Int
    ): Response<AnswerResponse>

    @FormUrlEncoded
    @POST("$ANSWER/store")
    suspend fun addAnswer(
        @Header(HEADER_AUTH) token: String,
        @Field("question_id") question_id: Int,
        @Field("description") description: String
    ): Response<AddAnswerResponse>
}