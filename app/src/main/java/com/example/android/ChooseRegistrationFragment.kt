package com.example.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_choose_registration.*

class ChooseRegistrationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        choose_to_reg_button.setOnClickListener {
            (requireActivity() as MainActivity).chooseRegToReg()
        }

        choose_to_auth_button.setOnClickListener {
            (requireActivity() as MainActivity).toAuthorization()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}