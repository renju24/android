package com.example.android

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DialogGameFragment(private val title: String, private val message: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("ОК") { dialog, id ->
                    dialog.cancel()
                    if (title == "Конец игры") (requireActivity() as MainActivity).back()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}