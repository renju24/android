package com.example.android.dataClasses

class ChipClass{
    private var chipColor = "no"

    fun getColor() = chipColor

    fun setColor(newColor: String) {
        chipColor = newColor
    }
}