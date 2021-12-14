package id.alian.forumapp.data.api

import id.alian.forumapp.data.api.response.*
import id.alian.forumapp.data.model.User
import id.alian.forumapp.utils.Constants.Base_URL
import id.alian.forumapp.utils.Constants.Route_Answer
import id.alian.forumapp.utils.Constants.Route_Auth
import id.alian.forumapp.utils.Constants.Route_Header_Auth
import id.alian.forumapp.utils.Constants.Route_Question
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
                .baseUrl(Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
                .create(ApiService::class.java)
        }
    }

    // AUTH ENDPOINT
    @FormUrlEncoded
    @POST("$Route_Auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Response<LoginResponse>

    @POST("$Route_Auth/register")
    suspend fun register(
        @Body user: User,
    ): Response<RegisterResponse>

    @GET("$Route_Auth/profile")
    @Headers("Accept: application/json")
    suspend fun profile(
        @Header(Route_Header_Auth) token: String,
    ): Response<User>

    // QUESTION ENDPOINT
    @GET("$Route_Question/all")
    suspend fun getQuestions(): Response<QuestionResponse>

    @POST("$Route_Question/add")
    @Multipart
    suspend fun addQuestionWithImage(
        @Header(Route_Header_Auth) token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part,
    ): Response<Unit>

    @FormUrlEncoded
    @POST("$Route_Question/add")
    suspend fun addQuestion(
        @Header(Route_Header_Auth) token: String,
        @Field("title") title: String,
        @Field("description") description: String,
    ): Response<Unit>

    @GET("$Route_Question/{userId}")
    suspend fun getQuestionByUserId(
        @Path("userId") userId: Int,
    ): Response<QuestionResponse>

    @GET("$Route_Question/image/{imageName}")
    suspend fun getQuestionImage(
        @Path("imageName") imageName: String,
    ): Response<Unit>

    @DELETE("$Route_Question/delete/{id}")
    suspend fun deleteQuestion(
        @Header(Route_Header_Auth) token: String,
        @Path("id") id: Int,
    ): Response<Unit>

    @FormUrlEncoded
    @PUT("$Route_Question/update/{id}")
    suspend fun updateQuestion(
        @Header(Route_Header_Auth) token: String,
        @Path("id") id: Int,
        @Field("title") title: String,
        @Field("description") description: String,
    ): Response<UpdateQuestionResponse>

    // ANSWER ENDPOINT
    @GET("$Route_Answer/{question_id}")
    suspend fun getAnswerByQuestionId(
        @Path("question_id") questionId: Int,
    ): Response<AnswerResponse>

    @FormUrlEncoded
    @POST("$Route_Answer/store")
    suspend fun addAnswer(
        @Header(Route_Header_Auth) token: String,
        @Field("question_id") question_id: Int,
        @Field("description") description: String,
    ): Response<AddAnswerResponse>
}