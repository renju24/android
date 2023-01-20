package com.example.android

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.adapters.HistoryAdapter
import com.example.android.adapters.TopAdapter
import com.example.android.dataClasses.GameClass
import com.example.android.dataClasses.TopUser
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.fragment_user_profile.*
import java.nio.charset.StandardCharsets
import java.util.*


class UserProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val callUser =
            "{\"username\": \"" + (requireActivity() as MainActivity).getUserName() + "\"}"

        //Информация о пользователе
        (requireActivity() as MainActivity).client.rpc(
            "get_user", callUser.toByteArray()
        ) { e, result ->
            val resData = result?.data
            profile_username.post {
                try {
                    val userInfo = String(resData!!, StandardCharsets.UTF_8)
                    val dataObject: JsonObject =
                        Gson().fromJson(userInfo, JsonObject::class.java)
                    profile_username.text = dataObject.get("username").asString
                    profile_rating.text = dataObject.get("ranking").asString
                    Log.i("userID", dataObject.get("id").asString)
                    (requireActivity() as MainActivity).setUserID(dataObject.get("id").asInt)
                } catch (resultEx: NullPointerException) {
                    val errorMsg = if (e != null) e.message else ""
                    (requireActivity() as MainActivity).makeToast(errorMsg!!)
                }
            }
        }


        //Информация о истории игр
        (requireActivity() as MainActivity).client.rpc(
            "game_history", callUser.toByteArray()
        ) { e, result ->
            val resData = result?.data
            profile_history_recycler.post {
                try {
                    val history = String(resData!!, StandardCharsets.UTF_8)
                    val dataObject = Gson().fromJson(history, JsonObject::class.java).get("games")
                    if (dataObject != null) {
                        val adapterList =
                            Gson().fromJson(dataObject, Array<GameClass>::class.java).toList()
                        profile_history_recycler.layoutManager =
                            LinearLayoutManager(requireContext())
                        profile_history_recycler.adapter = HistoryAdapter(adapterList)
                    }

                } catch (resultEx: NullPointerException) {
                    val errorMsg = if (e != null) e.message else ""
                    if (!errorMsg.equals("")) (requireActivity() as MainActivity).makeToast(errorMsg!!)
                }
            }
        }

        //Информация о топе игроков
        (requireActivity() as MainActivity).client.rpc(
            "top_10", callUser.toByteArray()
        ) { e, result ->
            val resData = result?.data
            profile_players_rating_recycler.post {
                try {
                    val top = String(resData!!, StandardCharsets.UTF_8)
                    val dataObject = Gson().fromJson(top, JsonObject::class.java).get("users")
                    if (dataObject != null) {
                        val adapterList =
                            Gson().fromJson(dataObject, Array<TopUser>::class.java).toList()
                        profile_players_rating_recycler.layoutManager =
                            LinearLayoutManager(requireContext())
                        profile_players_rating_recycler.adapter = TopAdapter(adapterList)
                    }

                } catch (resultEx: NullPointerException) {
                    val errorMsg = if (e != null) e.message else ""
                    if (!errorMsg.equals("")) (requireActivity() as MainActivity).makeToast(errorMsg!!)
                }
            }
        }


        profile_play_button.setOnClickListener {
            (requireActivity() as MainActivity).profileToStartGame()
        }

        super.onViewCreated(view, savedInstanceState)
    }


}