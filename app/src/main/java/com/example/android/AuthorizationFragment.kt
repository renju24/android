package com.example.android

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.android.dataClasses.LoginClass
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
            (requireActivity() as MainActivity).hideKeyboard()
        }

        auth_google_button.setOnClickListener{
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://renju24.com/api/v1/oauth2/android/google"))
            startActivity(browserIntent)
        }

        auth_vk_button.setOnClickListener{
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://renju24.com/api/v1/oauth2/android/vk"))
            startActivity(browserIntent)
        }

        auth_yandex_button.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://renju24.com/api/v1/oauth2/android/yandex"))
            startActivity(browserIntent)
        }

        auth_to_choose_button.setOnClickListener {
            (requireActivity() as MainActivity).hideKeyboard()
            (requireActivity() as MainActivity).authToChooseReg()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}