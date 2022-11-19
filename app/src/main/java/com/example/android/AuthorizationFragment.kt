package com.example.android

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.dataClasses.LoginClass
import com.example.android.dataClasses.UserClass
import kotlinx.android.synthetic.main.fragment_authorization.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthorizationFragment : Fragment() {
    private val webClient = WebClient().getApi()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_authorization, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        auth_to_profile_button.setOnClickListener {
            val login = auth_login_field.text.toString()
            val pass = auth_password_field.text.toString()

            val callAuth = webClient.postLoginAccount(LoginClass(login, pass))
            callAuth.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    (requireActivity() as MainActivity).checkUserData(response, login)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("renjuInf", "Response = $t")
                }
            })
        }

        auth_google_button.setOnClickListener{
            val callAuth = webClient.getOAuth("google")
            //TODO: Реализовать авторизацию через гугл
        }

        auth_to_choose_button.setOnClickListener {
            (requireActivity() as MainActivity).authToChooseReg()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}