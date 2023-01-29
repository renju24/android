package com.example.android

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.example.android.dataClasses.UserClass
import kotlinx.android.synthetic.main.fragment_registration.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegistrationFragment : Fragment() {
    private val webClient = WebClient().getApi()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        reg_to_profile_button.setOnClickListener {
            val username = reg_login_field.text.toString()
            val email = reg_email_field.text.toString()
            val password = reg_password_field.text.toString()
            val repPassword = reg_repeat_password_field.text.toString()

            val callReg = webClient.postNewAccount(
                UserClass(
                    username,
                    email,
                    password,
                    repPassword
                )
            )
            callReg.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    (requireActivity() as MainActivity).checkUserData(response, username)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("renjuInf", "Response = $t")
                }
            })
            (requireActivity() as MainActivity).hideKeyboard()
        }

        reg_to_auth_button.setOnClickListener {
            (requireActivity() as MainActivity).hideKeyboard()
            (requireActivity() as MainActivity).toAuthorization()
        }

        super.onViewCreated(view, savedInstanceState)
    }

}