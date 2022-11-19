package com.example.android.dataClasses

data class UserClass(
    val username: String,
    val email: String,
    val password: String,
    val repeated_password: String
)

data class LoginClass(
    val login: String,
    val password: String
)