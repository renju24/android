import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.MainActivity
import com.example.android.R
import com.example.android.dataClasses.InviteClass
import io.github.centrifugal.centrifuge.Client


class InviteAdapter(
    private val context: Context,
    private val client: Client
  //  private val invites: MutableList<InviteClass>
) :
    RecyclerView.Adapter<InviteAdapter.InviteViewHolder>() {
    private var invitesList = mutableListOf<InviteClass>()

    class InviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val inviterView: TextView = itemView.findViewById(R.id.auth_welcome_eng_text)
        val acceptButtonView: Button = itemView.findViewById(R.id.accept_button)
        val refuseButtonView: Button = itemView.findViewById(R.id.refuse_button)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.invite_elem_recycle, parent, false)
        return InviteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        holder.inviterView.text = "Игрок " + invitesList[position].getUser() + " приглашает вас в игру!"
        holder.acceptButtonView.setOnClickListener {
            //Отправление сообщение о принятии приглашения
            val callAccept = "{\"game_id\": " + invitesList[position].getId() + "}"
            client.rpc(
                "accept_game_invitation", callAccept.toByteArray()
            ) { e, _ ->
                val bundle = Bundle()
                bundle.putInt("game_id", 0)
                if (e == null && context is MainActivity) context.startGameToGameDesk(bundle)
                else if (context is MainActivity) context.runOnUiThread { context.makeToast("time has run out") }
            }
            removeItem(position)
        }
        holder.refuseButtonView.setOnClickListener {
            //Отправление сообщение об отказе от приглашения
            val callRefuse = "{\"game_id\": " + invitesList[position].getId() + "}"
            client.rpc(
                "decline_game_invitation", callRefuse.toByteArray()
            ) { _, _ -> }
            removeItem(position)
        }
    }

    override fun getItemCount() = invitesList.size

    private fun removeItem(position: Int) {
        invitesList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, invitesList.size)
    }

    fun addItem(item: InviteClass) {
        invitesList.add(item)
        notifyItemInserted(invitesList.size-1)
    }

    fun getInvitesList() = invitesList

}