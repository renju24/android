package com.example.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_start_game.*


class StartGameFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        invite_button.setOnClickListener {
            // TODO: Реализовать начало игры
            (requireActivity() as MainActivity).startGameToGameDesk()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}