package concertrip.sopt.com.concertrip.list.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import concertrip.sopt.com.concertrip.R
import concertrip.sopt.com.concertrip.interfaces.BasicListViewHolder
import kotlinx.android.synthetic.main.li_ticket.view.*

class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val prevTime : ImageView = itemView.findViewById(R.id.iv_time_prev)
    val nextTime : ImageView = itemView.findViewById(R.id.iv_time_next)

   // val name : TextView = itemView.findViewById(R.id.item_ticket_title)
   // val location : TextView = itemView.findViewById(R.id.item_ticket_place)
   // val date : TextView = itemView.findViewById(R.id.item_ticket_date)
    val img : ImageView = itemView.findViewById(R.id.item_iv_ticket)
}