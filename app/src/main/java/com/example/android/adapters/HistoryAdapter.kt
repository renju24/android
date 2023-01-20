package com.example.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.R
import com.example.android.dataClasses.GameClass

class HistoryAdapter (private val games: List<GameClass>):
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>(){
    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val blackUserView: TextView = itemView.findViewById(R.id.black_user_text)
        val whiteUserView: TextView = itemView.findViewById(R.id.white_user_text)
        val winnerView: TextView = itemView.findViewById(R.id.winner_text)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.history_recycler_elem, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.blackUserView.text = games[position].getBlackUser()
        holder.whiteUserView.text = games[position].getWhiteUser()
        holder.winnerView.text = games[position].getWinnerUser()
    }

    override fun getItemCount() = games.size
}