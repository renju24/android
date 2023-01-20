package com.example.android.dataClasses

data class InviteClass(
    val gameId : Int,
    val inviter: String
){
    fun getId() = gameId

    fun getUser() = inviter
}
