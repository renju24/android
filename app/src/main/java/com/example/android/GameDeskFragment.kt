package com.example.android

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.adapters.GameBoardAdapter
import com.example.android.dataClasses.ChipClass
import com.example.android.dataClasses.GameInfoClass
import com.example.android.dataClasses.MoveClass
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.centrifugal.centrifuge.*
import kotlinx.android.synthetic.main.fragment_game_desk.*
import java.nio.charset.StandardCharsets


class GameDeskFragment : Fragment() {
    val chipList = Array(225) { ChipClass() }
    lateinit var adapter: GameBoardAdapter
    private var userColor = "black"

    private val subListenerGame: SubscriptionEventListener = object : SubscriptionEventListener() {
        override fun onSubscribed(sub: Subscription, event: SubscribedEvent?) {
            Log.i("centrifugalSubscription", "GD" + "subscribed to " + sub.channel)
        }

        override fun onSubscribing(sub: Subscription, event: SubscribingEvent?) {
            Log.i("centrifugalSubscription", "GD" + "subscribing " + sub.channel)
        }

        override fun onUnsubscribed(sub: Subscription, event: UnsubscribedEvent?) {
            Log.i("centrifugalSubscription", "GD" + "unsubscribed " + sub.channel)
        }

        override fun onPublication(sub: Subscription?, event: PublicationEvent?) {
            Log.i("centrifugalSubscription", "GD" + "publication " + sub?.channel)
            if (event != null) {
                val gameEvent = String(event.data, StandardCharsets.UTF_8)
                val dataObject =
                    Gson().fromJson(gameEvent, JsonObject::class.java).get("event_type")
                when (dataObject.asString) {
                    "game_ended_in_draw" -> {
                        (requireActivity() as MainActivity).showDialog(
                            "Конец игры",
                            (requireActivity() as MainActivity).resources.getString(R.string.draw)
                        )
                    }

                    "game_ended_with_winner" -> {
                        val winnerObject =
                            Gson().fromJson(gameEvent, JsonObject::class.java).get("data")
                        val winnerID = Gson().fromJson(winnerObject, JsonObject::class.java)
                            .get("winner_id").asInt
                        (requireActivity() as MainActivity).showDialog(
                            "Конец игры",
                            (requireActivity() as MainActivity).resources.getString(R.string.win) + " "
                                    + if (winnerID == (requireActivity() as MainActivity).getUserID()) game_player1_name.text else game_player2_name.text
                        )
                    }

                    "move" -> {
                        val moveObject =
                            Gson().fromJson(gameEvent, JsonObject::class.java).get("data")
                        val move = Gson().fromJson(moveObject, MoveClass::class.java)
                        val pos = move.getX() * 15 + move.getY()
                        if (chipList[pos].getColor() == "no") {
                            if (userColor == "black") chipList[pos].setColor("white")
                            else chipList[pos].setColor("black")
                            game_board_recycler.post { adapter.notifyItemChanged(pos) }
                        }

                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_desk, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Подписка на игру
        game_info_step.text = ""
        val callGame =
            "{\"username\": \"" + (requireActivity() as MainActivity).getUserName() + "\"}"
        (requireActivity() as MainActivity).client.rpc(
            "is_playing", callGame.toByteArray()
        ) { e, result ->
            val resData = result?.data
            try {
                val gameInfo = String(resData!!, StandardCharsets.UTF_8)
                val dataObject = Gson().fromJson(gameInfo, JsonObject::class.java).get("game")
                val game = Gson().fromJson(dataObject, GameInfoClass::class.java)
                player1.post {
                    if (game.getUserColor() == "white") {
                        player1.setImageResource(R.drawable.white_elem)
                        player2.setImageResource(R.drawable.black_elem)
                    }
                    game_player1_name.text = (requireActivity() as MainActivity).getUserName()
                    game_player2_name.text = game.getOpponentName()

                    userColor = game.getUserColor()
                    game_board_recycler.layoutManager = GridLayoutManager(requireContext(), 15)
                    adapter = GameBoardAdapter(
                        (requireActivity() as MainActivity),
                        userColor,
                        game.getID().toString(),
                        chipList
                    )
                    game_board_recycler.adapter = adapter
                    (requireActivity() as MainActivity).subscribeToTopic(
                        "game_" + game.getID(),
                        subListenerGame
                    )
                }
            } catch (resultEx: NullPointerException) {
                val errorMsg = if (e != null) e.message else ""
                (requireActivity() as MainActivity).makeToast(errorMsg!!)
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }
}