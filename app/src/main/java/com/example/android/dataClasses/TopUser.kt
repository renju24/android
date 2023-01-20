package com.example.android.dataClasses

data class TopUser(
    val id: Int,
    val username: String,
    val ranking: String
) {
    fun getName():String{
        return username
    }

    fun getRank():String{
        return ranking
    }
}