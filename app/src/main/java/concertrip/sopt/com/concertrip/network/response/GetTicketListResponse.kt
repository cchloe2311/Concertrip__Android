package concertrip.sopt.com.concertrip.network.response

import com.google.gson.annotations.SerializedName
import concertrip.sopt.com.concertrip.model.Ticket
import concertrip.sopt.com.concertrip.network.response.data.SimpleTicketData
import concertrip.sopt.com.concertrip.network.response.data.TicketData
import concertrip.sopt.com.concertrip.network.response.interfaces.BaseModel

data class GetTicketListResponse(
    @SerializedName("data")
    var data: TicketListData
) : BaseModel(){

    fun toTicketList() : ArrayList<Ticket>{
        val list = ArrayList<Ticket>()
        data.tickets?.forEach {
            list.add(it.toTicket())
        }
        return list
    }
}

data class TicketListData(
    var tickets : List<SimpleTicketData>?
)