package com.example.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.R
import com.example.android.dataClasses.TopUser

class TopAdapter (private val topUsers: List<TopUser>):
    RecyclerView.Adapter<TopAdapter.TopViewHolder>(){
    class TopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val loginView: TextView = itemView.findViewById(R.id.top_login_text)
        val rankingView: TextView = itemView.findViewById(R.id.top_rating_text)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.rating_recycler_elem, parent, false)
        return TopViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TopViewHolder, position: Int) {
        holder.loginView.text = topUsers[position].getName()
        holder.rankingView.text = topUsers[position].getRank()
    }

    override fun getItemCount() = topUsers.size
}