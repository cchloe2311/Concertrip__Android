package concertrip.sopt.com.concertrip.model

import concertrip.sopt.com.concertrip.interfaces.ListData
import concertrip.sopt.com.concertrip.utillity.Constants

data class Ticket(//var _id : Int, var name : String, var location: String,
                  //var date: String?, var seat:String, var userIdx: Int,
                  //var eventId : String
    var img: String?) : ListData{

    override fun getType(): Int = Constants.TYPE_TICKET

    override fun getId(): String=""

    override fun getMainTitle(): String = ""

    override fun getSubTitle(): String =""

    override fun getImageUrl(): String  =img?:""
    override fun isSubscribe(): Boolean? =null

}