package com.example.android

import InviteAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.dataClasses.EventInviteClass
import com.example.android.dataClasses.GameInfoClass
import com.example.android.dataClasses.InviteClass
import com.example.android.databinding.FragmentGameDeskBinding
import com.example.android.databinding.FragmentStartGameBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.centrifugal.centrifuge.*
import kotlinx.android.synthetic.main.fragment_game_desk.*
import kotlinx.android.synthetic.main.fragment_start_game.*
import java.nio.charset.StandardCharsets


class StartGameFragment : Fragment() {
    var invitesList: MutableList<InviteClass> = arrayListOf()
    private lateinit var binding: FragmentStartGameBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartGameBinding.inflate(inflater, container, false);
        return binding.root
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
                            start_unfinished_game_text.text.toString() + " " + game.getOpponentName()
                        start_accept_button.setOnClickListener {
                            val bundle = Bundle()
                            bundle.putInt("game_id", game.getID())
                            (requireActivity() as MainActivity).startGameToGameDesk(bundle)
                        }
                    }
                } else {
                    throw NullPointerException()
                }
            } catch (resultEx: NullPointerException) {
                val errorMsg = if (e != null) e.message else ""
                (requireActivity() as MainActivity).makeToast(errorMsg!!)
            }
        }


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
                                    val bundle = Bundle()
                                    bundle.putInt("game_id", 0)
                                    (requireActivity() as MainActivity).startGameToGameDesk(bundle)
                                }
                            }
                        }
                    }

                (requireActivity() as MainActivity).subscribeToTopic(
                    "game_" + dataObject.get("game_id").asInt,
                    subListenerGame
                )
            }
            (requireActivity() as MainActivity).hideKeyboard()
        }

        //Вывод списка прилашений
        binding.startInviteRecycler.layoutManager = LinearLayoutManager(requireContext())
        val adapter = InviteAdapter(
            (requireActivity() as MainActivity),
            (requireActivity() as MainActivity).client
            //  invitesList
        )
        binding.startInviteRecycler.adapter = adapter
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
                Log.i("centrifugalSubscription", "publication " + sub?.channel)
                if (event != null) {
                    val invite = String(event.data, StandardCharsets.UTF_8)
                    val inviteObject: EventInviteClass =
                        Gson().fromJson(invite, EventInviteClass::class.java)
                    if (inviteObject.getEventType() == "game_invitation") {

                        var isAdded = false
                        val gameId = inviteObject.getEventData().getId()
                        val inviterName = inviteObject.getEventData().getOpponent()
                        for (i in (binding.startInviteRecycler.adapter as InviteAdapter).getInvitesList()) if (i.getUser() == inviterName) isAdded =
                            true
                        if (!isAdded) {
                            //invitesList.add(InviteClass(gameId, inviterName))
                            binding.startInviteRecycler.post {
                                (binding.startInviteRecycler.adapter as InviteAdapter).addItem(
                                    InviteClass(gameId, inviterName)
                                )
                            }
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

    override fun onDestroy() {
        val sub = (requireActivity() as MainActivity).client.getSubscription("user_" + (requireActivity() as MainActivity).getUserID())
        sub.unsubscribe()
        (requireActivity() as MainActivity).client.removeSubscription(sub)
        super.onDestroy()
    }

}