package com.example.android

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.adapters.GameBoardAdapter
import com.example.android.dataClasses.*
import com.example.android.databinding.FragmentGameDeskBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.centrifugal.centrifuge.*
import kotlinx.android.synthetic.main.fragment_game_desk.*
import java.nio.charset.StandardCharsets


class GameDeskFragment : Fragment() {
    private lateinit var binding: FragmentGameDeskBinding
    val chipList = Array(225) { ChipClass() }
    lateinit var adapter: GameBoardAdapter
    private var userColor = "black"

    private var currentColor = "black"
    var gameID = 0

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

                            binding.gameBoardRecycler.post {
                                adapter.notifyItemChanged(pos)
                                game_info_step.text = chipList[pos].getColor()
                                if (chipList[pos].getColor() == "black") {
                                    game_info_step.text =
                                        (requireActivity() as MainActivity).resources.getString(R.string.white_move)
                                } else {
                                    game_info_step.text =
                                        (requireActivity() as MainActivity).resources.getString(R.string.black_move)
                                }
                            }
                        }
//                        game_info_step.post {
//                            if (currentColor == "black") {
//                                game_info_step.text =
//                                    (requireActivity() as MainActivity).resources.getString(R.string.white_move)
//                            } else {
//                                game_info_step.text =
//                                    (requireActivity() as MainActivity).resources.getString(R.string.black_move)
//                            }
//                        }
                    }

                    "user_left_game" -> {
                        val leaveObject =
                            Gson().fromJson(gameEvent, JsonObject::class.java).get("data")
                        val leave = Gson().fromJson(leaveObject, GameLeaveClass::class.java)
                        if (leave.getWinnerID() != 0) {
                            (requireActivity() as MainActivity).showDialog(
                                "Конец игры",
                                (requireActivity() as MainActivity).resources.getString(R.string.win) + " "
                                        + if (leave.getWinnerID() == (requireActivity() as MainActivity).getUserID()) game_player1_name.text else game_player2_name.text
                            )
                        } else {
                            (requireActivity() as MainActivity).showDialog(
                                "Конец игры",
                                (requireActivity() as MainActivity).resources.getString(R.string.no_win)
                            )
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
        val id = arguments?.getInt("game_id")
        if (id != 0) {
            boardRestoration(id!!)
        }
        binding = FragmentGameDeskBinding.inflate(inflater,container,false);
        return binding.root
        //return inflater.inflate(R.layout.fragment_game_desk, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Подписка на игру
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
                    gameID = game.getID()
                    game_info_step.text = if (currentColor == "black")
                        (requireActivity() as MainActivity).resources.getString(R.string.black_move)
                    else (requireActivity() as MainActivity).resources.getString(R.string.white_move)
                    if (game.getUserColor() == "white") {
                        player1.setImageResource(R.drawable.white_elem)
                        player2.setImageResource(R.drawable.black_elem)
                    }
                    game_player1_name.text = (requireActivity() as MainActivity).getUserName()
                    game_player2_name.text = game.getOpponentName()

                    userColor = game.getUserColor()
                    binding.gameBoardRecycler.layoutManager = GridLayoutManager(requireContext(), 15)
                    adapter = GameBoardAdapter(
                        (requireActivity() as MainActivity),
                        userColor,
                        game.getID().toString(),
                        chipList
                    )
                    binding.gameBoardRecycler.adapter = adapter
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

        //Отказ от игры
        binding.gameDeclineButton.setOnClickListener {
            val callLeave = "{\"game_id\": $gameID}"
            (requireActivity() as MainActivity).client.rpc(
                "leave_game", callLeave.toByteArray()
            ) { e, _ ->
                val errorMsg = if (e != null) e.message else ""
                (requireActivity() as MainActivity).makeToast(errorMsg!!)
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun boardRestoration(id: Int) {
        val callGameBoard = "{\"game_id\": $id}"
        (requireActivity() as MainActivity).client.rpc(
            "get_board_state", callGameBoard.toByteArray()
        ) { e, result ->
            val resData = result?.data
            try {
                val movesInfo = String(resData!!, StandardCharsets.UTF_8)
                val movesObject = Gson().fromJson(movesInfo, JsonObject::class.java).get("moves")
                if (movesObject != null) {
                    val movesList =
                        Gson().fromJson(movesObject, Array<MoveClass>::class.java).toList()
                    currentColor = if (movesList.size % 2 == 0) "black" else "white"
                    var color = "black"
                    for (move in movesList) {
                        val pos = move.getX() * 15 + move.getY()
                        chipList[pos].setColor(color)
                        color = if (color == "black") "white" else "black"
                    }
                }

            } catch (resultEx: NullPointerException) {
                val errorMsg = if (e != null) e.message else ""
                (requireActivity() as MainActivity).makeToast(errorMsg!!)
            }
        }
    }

    override fun onDestroy() {
        val sub = (requireActivity() as MainActivity).client.getSubscription("game_$gameID")
        sub.unsubscribe()
        (requireActivity() as MainActivity).client.removeSubscription(sub)
        super.onDestroy()
    }

}