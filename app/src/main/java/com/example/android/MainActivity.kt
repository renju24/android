package com.example.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import io.github.centrifugal.centrifuge.*
import io.github.centrifugal.centrifuge.EventListener
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var client: Client
    private lateinit var navController: NavController
    private var userID = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController

        help_button.setOnClickListener{
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ru.wikipedia.org/wiki/%D0%A0%D1%8D%D0%BD%D0%B4%D0%B7%D1%8E"))
            startActivity(browserIntent)
        }
    }


    val listener: EventListener = object : EventListener() {
        override fun onConnected(client: Client?, event: ConnectedEvent?) {
            Log.i("centrifugalConnection", "connected")
        }

        override fun onConnecting(client: Client?, event: ConnectingEvent) {
            Log.i("centrifugalConnection", "connecting")
        }

        override fun onDisconnected(client: Client?, event: DisconnectedEvent) {
            Log.i("centrifugalConnection", "disconnected")
        }
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
        connectToWebSocketServer()
        navController.navigate(R.id.userProfileFragment)
    }

    fun profileToStartGame() {
        navController.navigate(R.id.action_profile_to_start)
    }

    fun startGameToGameDesk() {
        navController.navigate(R.id.action_start_to_game)
    }

    fun authToGame(){
        navController.navigate(R.id.action_auth_to_game)
    }

    fun back() {
        navController.popBackStack()
    }

    fun connectToWebSocketServer(){
        val opts = Options()
        opts.token = getToken()
        client = Client("wss://renju24.com/connection/websocket", opts, listener)
        client.connect()
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

    fun subscribeToTopic(topic:String, subListener: SubscriptionEventListener){
        var sub: Subscription? = null
        try {
            sub = client.newSubscription(topic, subListener)
            sub.subscribe()
        } catch (e: DuplicateSubscriptionException) {
            e.printStackTrace()
        }
    }

    fun setUserID(id: Int){
        userID = id
    }

    fun getUserID(): Int{
        return userID
    }
}