package concertrip.sopt.com.concertrip.activities.info

import android.os.Bundle

import android.support.v7.widget.GridLayoutManager

import android.support.v7.widget.RecyclerView
import android.util.Log

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import concertrip.sopt.com.concertrip.R
import concertrip.sopt.com.concertrip.dialog.ColorToast
import concertrip.sopt.com.concertrip.interfaces.OnItemClick
import concertrip.sopt.com.concertrip.dialog.CustomDialog
import concertrip.sopt.com.concertrip.interfaces.OnResponse
import concertrip.sopt.com.concertrip.list.adapter.BasicListAdapter
import concertrip.sopt.com.concertrip.list.adapter.SeatListAdapter
import concertrip.sopt.com.concertrip.model.Artist
import concertrip.sopt.com.concertrip.model.Caution
import concertrip.sopt.com.concertrip.model.Concert
import concertrip.sopt.com.concertrip.model.Seat
import concertrip.sopt.com.concertrip.network.ApplicationController
import concertrip.sopt.com.concertrip.network.NetworkService
import concertrip.sopt.com.concertrip.network.response.GetConcertResponse
import concertrip.sopt.com.concertrip.network.response.MessageResponse
import concertrip.sopt.com.concertrip.network.response.interfaces.BaseModel
import concertrip.sopt.com.concertrip.utillity.Constants
import concertrip.sopt.com.concertrip.utillity.Constants.Companion.INTENT_TAG_ID
import concertrip.sopt.com.concertrip.utillity.NetworkUtil
import concertrip.sopt.com.concertrip.utillity.Secret
import concertrip.sopt.com.concertrip.utillity.Secret.Companion.USER_TOKEN
import kotlinx.android.synthetic.main.activity_concert.*
import kotlinx.android.synthetic.main.content_concert.*
import kotlinx.android.synthetic.main.content_header.*
import org.jetbrains.anko.textColor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class ConcertActivity  : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener,  OnResponse {

    private val RECOVERY_DIALOG_REQUEST = 1

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, youTubePlayer: YouTubePlayer?, b: Boolean) {
        if (!b && ::concert.isInitialized) {
//            val youtubeUrlList = concert.youtubeUrl!!.split("?v=")
//            youTubePlayer?.cueVideo(youtubeUrlList[youtubeUrlList.size-1])
            Log.d("~~~YOUTUBE URL : ", concert.youtubeUrl)
            youTubePlayer?.cueVideo(concert.youtubeUrl)
        }
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider,
        youTubeInitializationResult: YouTubeInitializationResult
    ) {
        if (youTubeInitializationResult.isUserRecoverableError) {
            youTubeInitializationResult.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show()
        } else {
            val errorMessage = String.format(
                "There was an error initializing the YouTubePlayer (%1\$s)", youTubeInitializationResult.toString()
            )
            ColorToast(this,errorMessage)
        }
    }

    private fun getYouTubePlayerProvider(): YouTubePlayer.Provider {
        return findViewById<View>(R.id.youtube) as YouTubePlayerView
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//        if (requestCode == RECOVERY_DIALOG_REQUEST) {
//            getYouTubePlayerProvider().initialize(Secret.YOUTUBE_API_KEY, this)
//        }
//    }

    override fun onResume() {
        super.onResume()

        connectRequestData(concertId)
    }

    private var concertId: String= "5c28663f3eea39d2b003f94b"
    lateinit  var concert : Concert

    var dataListMember = arrayListOf<Artist>()
    private lateinit var memberAdapter : BasicListAdapter

    var dataListCaution = arrayListOf<Caution>()
    private lateinit var cautionAdapter : BasicListAdapter

    var dataListSeat = arrayListOf<Seat>()
    private lateinit var seatAdapter : SeatListAdapter

    private val networkService: NetworkService by lazy {
        ApplicationController.instance.networkService
    }

    public var mGlideRequestManager : RequestManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concert)

        concertId = intent.getStringExtra(INTENT_TAG_ID)

        mGlideRequestManager = Glide.with(this)

        initialUI()
        connectRequestData(concertId)
    }

    private fun initialUI(){
        memberAdapter = BasicListAdapter(this, dataListMember, BasicListAdapter.MODE_THUMB)
        recycler_view.adapter = memberAdapter

        cautionAdapter = BasicListAdapter(this, dataListCaution)
        recycler_view_caution.layoutManager = GridLayoutManager(applicationContext,3)
        recycler_view_caution.adapter = cautionAdapter

        /*TODO have to implement memberList*/
        seatAdapter = SeatListAdapter(this, dataListSeat)
        recycler_view_seat.adapter = seatAdapter

        scroll_view.smoothScrollTo(0,0)
        scroll_view.viewTreeObserver.addOnScrollChangedListener {
            if(btn_ticket.isEnabled) {
                val scrollY = scroll_view.scrollY
                if (scrollY > 10 && btn_ticket.visibility == GONE) {
                    btn_ticket.visibility = VISIBLE
                } else if (scrollY <= 10 && btn_ticket.visibility == VISIBLE) {
                    btn_ticket.visibility = GONE
                }
            }
        }

        btn_ticket.setOnClickListener{
            NetworkUtil.getPayment(networkService,this,concertId)
        }

        btn_follow.setOnClickListener {
            NetworkUtil.subscribeConcert(networkService, this, concertId)
        }

        btn_back.setOnClickListener {
            finish()
        }
    }

    private fun updateArtistList(list : ArrayList<Artist>){
        dataListMember.clear()
        dataListMember.addAll(list)
        memberAdapter.notifyDataSetChanged()
    }

    private fun updateConcertData(){
        btn_follow.setImageDrawable(if (concert.subscribe)getDrawable(R.drawable.ic_header_heart_bell_selected)
                                    else getDrawable(R.drawable.ic_header_heart_bell_unselected))
        //iv_small_follow.setImageDrawable(if (artist.subscribe) getDrawable(R.drawable.ic_header_likes_selected) else getDrawable(R.drawable.ic_header_likes_unselected))

        // TODO 구독하기(종) 버튼 설정
        if(URLUtil.isValidUrl(concert.backImg))
            mGlideRequestManager?.load(concert.backImg)?.into(iv_back)
        if(URLUtil.isValidUrl(concert.profileImg))
            mGlideRequestManager?.load(concert.profileImg)?.apply(RequestOptions.circleCropTransform())?.into(iv_profile)
        if(URLUtil.isValidUrl(concert.eventInfoImg)){
            //iv_concert_info.setScaleType(ImageView.ScaleType.FIT_XY)
            mGlideRequestManager?.load(concert.eventInfoImg)?.into(iv_concert_info)
        }

        tv_title.text = concert.title
        tv_tag.text  = concert.subscribeNum.toString()
        tv_concert_location.text = concert.location

        var dateStr : String = ""

        concert.date?.forEach {
            val str = convertDate(it)
            dateStr= dateStr.plus("$str\n")
        }

        tv_concert_date.text = dateStr

        getYouTubePlayerProvider().initialize(Secret.YOUTUBE_API_KEY, this)
    }

    private fun convertDate(input: String?) : String?{
        val dayNum : List<String> = listOf("일", "월", "화", "수", "목", "금", "토")

        val convertedDate = StringBuilder()

        if(input != null){
            try {
                val dateInfoList = input.split("T")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd").parse(input.split("T")[0])
                val instance: Calendar = Calendar.getInstance()
                instance.setTime(dateFormat)
                val dayNumIdx = instance.get(Calendar.DAY_OF_WEEK)

                val splitedList = dateInfoList[0].split("-")

                convertedDate.append(splitedList[0] + "." + splitedList[1] + "." + splitedList[2] + "(" + dayNum[dayNumIdx - 1] + ")")

            }catch (e  : Exception){
                e.printStackTrace()
            }
            return convertedDate.toString()
        }
        else{
            return convertedDate.toString()
        }
    }


    private fun updateCautionData(list : ArrayList<Caution>){
        dataListCaution.clear()
        dataListCaution.addAll(list)
        cautionAdapter.notifyDataSetChanged()
    }

    private fun updateSeatData(list : ArrayList<Seat>){
        dataListSeat.clear()
        dataListSeat.addAll(list)
        seatAdapter.notifyDataSetChanged()
    }

    private var LOG_TAG = "/api/concert/detail"

    private fun connectRequestData(id : String){

        progress_bar.visibility=View.VISIBLE
        val concertResponse : Call<GetConcertResponse> = networkService.getEvent(USER_TOKEN, concertId)

        concertResponse.enqueue(object : Callback<GetConcertResponse>
        {
            override fun onFailure(call: Call<GetConcertResponse>?, t: Throwable?) {
                progress_bar.visibility=View.GONE
                Log.e(Constants.LOG_NETWORK, "$LOG_TAG $t")
            }
            override fun onResponse(call: Call<GetConcertResponse>?, response: Response<GetConcertResponse>?) {

                progress_bar.visibility=View.GONE
                response?.let { res->
                    if (res.body()?.status == Secret.NETWORK_SUCCESS) {
                        Log.d(Constants.LOG_NETWORK, "$LOG_TAG :${response.body().toString()}")
                        res.body()?.data?.let {
                            concert = it.toConcert()
                            updateArtistList(ArrayList(concert.artistList))
                            updateConcertData()
                            updateCautionData(ArrayList(concert.precaution))
                            updateSeatData(ArrayList(concert.seatList))
                        }

                    } else {
                        ColorToast(applicationContext, "해당 콘서트를 찾을 수 없습니다.")
                        Log.d(Constants.LOG_NETWORK, "$LOG_TAG: fail ${response.body()?.message}")
                        finish()
                    }
                }

            }
        })
    }

    override fun onSuccess(obj: BaseModel, position: Int?) {
        if(obj is MessageResponse){



            if(obj.message?.contains("구매")==true){
                ColorToast(this,"공연을 구매하였습니다.")
                btn_ticket.textColor=getColor(R.color.white)
                btn_ticket.isSelected=true
                btn_ticket.isPressed=true
                btn_ticket.isEnabled=false

            }else {
                concert.subscribe=(obj.message?.contains("취소")==false)
                btn_follow.setImageDrawable(
                    if (concert.subscribe) getDrawable(R.drawable.ic_header_heart_bell_selected)
                    else getDrawable(R.drawable.ic_header_heart_bell_unselected)
                )
                //iv_small_follow.setImageDrawable(if (artist.subscribe) getDrawable(R.drawable.ic_header_likes_selected) else getDrawable(R.drawable.ic_header_likes_unselected))

                if (concert.subscribe)
                    ColorToast(this, getString(R.string.txt_concert_added))
                else
                    ColorToast(this, getString(R.string.txt_concert_minus))
            }
        }
    }


    override fun onFail(status: Int) {
        ColorToast(this,getString(R.string.txt_try_again))
    }


}
