package concertrip.sopt.com.concertrip.network.response.data

import concertrip.sopt.com.concertrip.model.Alarm

data class AlarmData(
    var txt : String,
    var data : String
){
    fun toAlarm() : Alarm = Alarm("","","")


}