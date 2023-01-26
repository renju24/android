package com.example.android

import InviteAdapter
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.adapters.GameBoardAdapter
import com.example.android.dataClasses.EventInviteClass
import com.example.android.dataClasses.GameInfoClass
import com.example.android.dataClasses.InviteClass
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.centrifugal.centrifuge.*
import kotlinx.android.synthetic.main.fragment_game_desk.*
import kotlinx.android.synthetic.main.fragment_start_game.*
import java.nio.charset.StandardCharsets


class StartGameFragment : Fragment() {
    var invitesList: MutableList<InviteClass> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Проверка наличия незавершенной игры
        val callCheckGame =
            "{\"username\": \"" + (requireActivity() as MainActivity).getUserName() + "\"}"
        (requireActivity() as MainActivity).client.rpc(
            "is_playing", callCheckGame.toByteArray()
        ) { e, result ->
            val resData = result?.data
            try {
                val gameInfo = String(resData!!, StandardCharsets.UTF_8)
                val dataObject = Gson().fromJson(gameInfo, JsonObject::class.java).get("game")
                val game = Gson().fromJson(dataObject, GameInfoClass::class.java)
                if (game != null) {
                    start_unfinished_game.post {
                        start_unfinished_game.visibility = View.VISIBLE
                        start_unfinished_game_text.text =
                            "Продолжите игру с " + game.getOpponentName() + " или завершите, чтобы начать новую:"
                    }
                } else {
                    throw NullPointerException()
                }
            } catch (resultEx: NullPointerException) {
                val errorMsg = if (e != null) e.message else ""
                (requireActivity() as MainActivity).makeToast(errorMsg!!)
            }
        }
        //TODO: Дописать обработку нажатий для отказа/восстановления игры


        //Приглашение игрока в игру
        invite_button.setOnClickListener {
            val opponentName = start_login_field.text
            val callForGame = "{\"username\": \"$opponentName\"}"
            (requireActivity() as MainActivity).client.rpc(
                "call_for_game", callForGame.toByteArray()
            ) { e, result ->
                val resData = result?.data
                val gameNum = String(resData!!, StandardCharsets.UTF_8)
                val dataObject: JsonObject = Gson().fromJson(gameNum, JsonObject::class.java)
                Log.i("game_id", dataObject.get("game_id").asString)
                val bundle = Bundle()
                bundle.putString("gameID", dataObject.get("game_id").asString)

                val subListenerGame: SubscriptionEventListener =
                    object : SubscriptionEventListener() {
                        override fun onSubscribed(sub: Subscription, event: SubscribedEvent?) {
                            Log.i("centrifugalSubscription", "SG" + "subscribed to " + sub.channel)
                        }

                        override fun onSubscribing(sub: Subscription, event: SubscribingEvent?) {
                            Log.i("centrifugalSubscription", "SG" + "subscribing " + sub.channel)
                        }

                        override fun onUnsubscribed(sub: Subscription, event: UnsubscribedEvent?) {
                            Log.i("centrifugalSubscription", "SG" + "unsubscribed " + sub.channel)
                        }

                        override fun onPublication(sub: Subscription?, event: PublicationEvent?) {
                            Log.i("centrifugalSubscription", "SG" + "publication " + sub?.channel)
                            if (event != null) {
                                val playerAnswer = String(event.data, StandardCharsets.UTF_8)
                                val playerAnswerObject: JsonObject =
                                    Gson().fromJson(playerAnswer, JsonObject::class.java)
                                if (playerAnswerObject.get("event_type").asString == "decline_game_invitation") {
                                    sub!!.unsubscribe()
                                    (requireActivity() as MainActivity).client.removeSubscription(
                                        sub
                                    )
                                    val msgText: String =
                                        opponentName.toString() + " " + (requireActivity() as MainActivity).resources.getString(
                                            R.string.decline
                                        )
                                    (requireActivity() as MainActivity).showDialog(
                                        "Отказ от игры",
                                        msgText
                                    )
                                } else if (playerAnswerObject.get("event_type").asString == "game_started") {
                                    sub!!.unsubscribe()
                                    (requireActivity() as MainActivity).client.removeSubscription(
                                        sub
                                    )
                                    (requireActivity() as MainActivity).startGameToGameDesk()
                                }
                            }
                        }
                    }

                (requireActivity() as MainActivity).subscribeToTopic(
                    "game_" + dataObject.get("game_id").asInt,
                    subListenerGame
                )
            }
        }

        //Вывод списка прилашений
        start_invite_recycler.layoutManager = LinearLayoutManager(requireContext())
        val adapter = InviteAdapter(
            (requireActivity() as MainActivity),
            (requireActivity() as MainActivity).client,
            invitesList
        )
        start_invite_recycler.adapter = adapter

        var subListenerUser: SubscriptionEventListener = object : SubscriptionEventListener() {
            override fun onSubscribed(sub: Subscription, event: SubscribedEvent?) {
                Log.i("centrifugalSubscription", "subscribed to " + sub.channel)
            }

            override fun onSubscribing(sub: Subscription, event: SubscribingEvent?) {
                Log.i("centrifugalSubscription", "subscribing " + sub.channel)
            }

            override fun onUnsubscribed(sub: Subscription, event: UnsubscribedEvent?) {
                Log.i("centrifugalSubscription", "unsubscribed " + sub.channel)
            }

            override fun onPublication(sub: Subscription?, event: PublicationEvent?) {
                Log.i("centrifugalSubscription", "publication")
                if (event != null) {
                    val invite = String(event.data, StandardCharsets.UTF_8)
                    val inviteObject: EventInviteClass =
                        Gson().fromJson(invite, EventInviteClass::class.java)
                    if (inviteObject.getEventType() == "game_invitation") {
                        var isAdded = false
                        val gameId = inviteObject.getEventData().getId()
                        val inviterName = inviteObject.getEventData().getOpponent()
                        for (i in invitesList) if (i.getUser() == inviterName) isAdded = true
                        if (!isAdded) {
                            invitesList.add(InviteClass(gameId, inviterName))
                            start_invite_recycler.post { adapter.notifyItemInserted(invitesList.size - 1) }
                        }
                    }
                }
            }
        }

        (requireActivity() as MainActivity).subscribeToTopic(
            "user_" + (requireActivity() as MainActivity).getUserID(),
            subListenerUser
        )
        super.onViewCreated(view, savedInstanceState)
    }


}