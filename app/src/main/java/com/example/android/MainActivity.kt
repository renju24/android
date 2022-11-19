package com.example.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController
    }

    //Реализация навигации по макетам приложения
    fun toAuthorization() {
        navController.navigate(R.id.authorizationFragment)
    }

    fun authToChooseReg() {
        navController.navigate(R.id.action_auth_to_choose)
    }

    fun chooseRegToReg() {
        navController.navigate(R.id.action_choose_to_reg)
    }

    fun toProfile() {
        navController.navigate(R.id.userProfileFragment)
    }

    fun profileToStartGame() {
        navController.navigate(R.id.action_profile_to_start)
    }

    fun startGameToGameDesk() {
        navController.navigate(R.id.action_start_to_game)
    }

    fun back() {
        navController.popBackStack()
    }

    //Показ информационных сообщений
    fun makeToast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    //Сохранение токена
    fun saveUserInfo(userToken: String, name: String) {
        val sp = this.getSharedPreferences("settings", MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString("userToken", userToken)
        editor.putString("username", name)
        editor.apply()
    }

    //Получение сохранённого токена
    fun getToken(): String {
        val sp = this.getSharedPreferences("settings", MODE_PRIVATE)
        return sp.getString("userToken", "") ?: ""
    }

    //Получение сохранённого имени пользователя
    fun getUserName(): String {
        val sp = this.getSharedPreferences("settings", MODE_PRIVATE)
        return sp.getString("username", "") ?: ""
    }

    //Отправка запроса на регистрацию/авторизацию пользователя
    fun checkUserData(response : Response<ResponseBody>, name: String){
        val code = response.code()
        if (code != 200) {
            val jObjError = JSONObject(response.errorBody()!!.string())
            val errMsg = jObjError.getJSONObject("error").getString("message")
            makeToast(errMsg)
        } else {
            val jObj = JSONObject(response.body()!!.string())
            val userToken = jObj.get("token").toString()
            saveUserInfo(userToken, name)
            toProfile()
        }
    }

}