package com.example.android

import com.example.android.dataClasses.LoginClass
import com.example.android.dataClasses.UserClass
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class WebClient {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://renju24.com/api/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: WebClientService = retrofit.create(WebClientService::class.java)

    fun getApi() = api
}

interface WebClientService {
    @Headers("Content-Type: application/json")
    @POST("sign_up")
    fun postNewAccount(
        @Body newUser: UserClass
    ): Call<ResponseBody>

    @POST("sign_in")
    fun postLoginAccount(
        @Body loginUser: LoginClass
    ): Call<ResponseBody>
}