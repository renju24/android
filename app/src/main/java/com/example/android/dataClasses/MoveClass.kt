package com.example.android.dataClasses

data class MoveClass (
    val user_id: Int,
    val x_coordinate: Int,
    val y_coordinate: Int
){
    fun getX() = x_coordinate

    fun getY() = y_coordinate

    fun getUserID() = user_id
}