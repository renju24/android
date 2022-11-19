package com.example.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_user_profile.*

class UserProfileFragment : Fragment() {
    private val webClient = WebClient().getApi()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val name = (requireActivity() as MainActivity).getUserName()
        profile_username.text = name

        //TODO: Разобраться с методом getUser
        profile_play_button.setOnClickListener {
            (requireActivity() as MainActivity).profileToStartGame()
        }

        super.onViewCreated(view, savedInstanceState)
    }


}