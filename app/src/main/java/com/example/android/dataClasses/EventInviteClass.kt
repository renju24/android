package com.example.android.dataClasses

data class EventInviteClass(
    val data : EventData,
    val event_type: String
){
    fun getEventType(): String{
        return event_type
    }

    fun getEventData() = data
}

data class EventData(
    val game_id : Int,
    val inviter: String,
    val invited_at: String
){
    fun getId() = game_id

    fun getOpponent() = inviter

    fun getTime() = invited_at
}