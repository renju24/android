package com.example.android.dataClasses

data class GameClass(
    val id: Int,
    val black_username: String,
    val white_username: String,
    val winner_username: String
) {
    fun getBlackUser():String{
        return black_username
    }

    fun getWhiteUser():String{
        return white_username
    }

    fun getWinnerUser(): String{
        return winner_username
    }
}

data class GameInfoClass(
    val game_id: Int,
    val color: String,
    val opponent: String
) {
    fun getID():Int{
        return game_id
    }

    fun getUserColor():String{
        return color
    }

    fun getOpponentName(): String{
        return opponent
    }
}
