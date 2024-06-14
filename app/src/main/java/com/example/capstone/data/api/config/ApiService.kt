package com.example.capstone.data.api.config

import com.example.capstone.data.api.response.LoginResponse
import com.example.capstone.data.api.response.LogoutResponse
import com.example.capstone.data.api.response.NutriotionResponse
import com.example.capstone.data.api.response.PredictResponse
import com.example.capstone.data.api.response.ProfileResponse
import com.example.capstone.data.api.response.RegisterResponse
import com.example.capstone.data.api.response.TipsResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("fullName") name: String,
        @Field("heightCm") height: Float,
        @Field("weightKg") weight : Float,
        @Field("ageYears") age : Int,
        @Field("armCircumferenceCm") circleHand : Float
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String,
    ): LogoutResponse

    @FormUrlEncoded
    @POST("profile")
    suspend fun profile(
        @Header("Authorization") token: String,
        @Field("fullName") name: String,
        @Field("weightKg") weight : Float,
        @Field("ageYears") age : Int,
        @Field("armCircumferenceCm") circleHand : Float,
        @Field("heightCm") height: Float
    ): ProfileResponse

    @Multipart
    @POST("predict")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
    ) : PredictResponse

    @GET("food/{name}")
    suspend fun getNutrition(
        @Header("Authorization") token: String,
        @Path("name") name: String,
    ) : NutriotionResponse

    @GET("tips")
    fun getAllTips(
        @Header("Authorization") token: String
    ): Call<TipsResponse>
}