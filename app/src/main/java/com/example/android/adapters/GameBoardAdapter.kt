package com.example.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.MainActivity
import com.example.android.R
import com.example.android.dataClasses.ChipClass
import kotlinx.android.synthetic.main.fragment_game_desk.*
import org.json.JSONObject


class GameBoardAdapter(
    private val context: Context,
    private val color: String,
    private val game: String,
    private val chipList: Array<ChipClass>
) : RecyclerView.Adapter<GameBoardAdapter.GameViewHolder>() {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chipView: ImageView = itemView.findViewById(R.id.chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.game_elem, parent, false)
        return GameViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        when (chipList[position].getColor()) {
            "black" -> holder.chipView.setImageResource(R.drawable.black_elem_mini)
            "white" -> holder.chipView.setImageResource(R.drawable.white_elem_mini)
            else -> {}
        }
        holder.chipView.setOnClickListener {
            //Отправка хода на сервер (x - div, y - mod)
            val sendMove = JSONObject()
            sendMove.put("game_id", game.toInt())
            sendMove.put("x_coordinate", position / 15)
            sendMove.put("y_coordinate", position % 15)

            if (context is MainActivity) {
                context.client.rpc("make_move", sendMove.toString().toByteArray()) { e, result ->

                    context.game_info_step.post {
                        when (if (e != null) e.message else "") {
                            "first move should be made by black user" -> context.game_info_step.text =
                                context.getResources().getString(R.string.err423)
                            "first move should be in board's center" -> context.game_info_step.text =
                                context.getResources().getString(R.string.err424)
                            "coordinates outside the board" -> context.game_info_step.text =
                                context.getResources().getString(R.string.err425)
                            "field is already taken" -> context.game_info_step.text =
                                context.getResources().getString(R.string.err426)
                            "invalid turn" -> context.game_info_step.text =
                                context.getResources().getString(R.string.err427)
                            "" -> {

                                chipList[position].setColor(color)
                                if (chipList[position].getColor() == "black") {
                                    context.game_info_step.text =
                                        context.resources.getString(R.string.white_move)
                                } else {
                                    context.game_info_step.text =
                                        context.resources.getString(R.string.black_move)
                                }
                               // context.game_info_step.text = chipList[position].getColor()
                                notifyItemChanged(position)
                            }

                            else -> {
                                context.makeToast(e.message!!)
                            }
                        }
                    }

                }
            }
        }

    }

    override fun getItemCount() = 225
}