package concertrip.sopt.com.concertrip.activities.info

import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.webkit.URLUtil
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
import concertrip.sopt.com.concertrip.interfaces.OnResponse
import concertrip.sopt.com.concertrip.list.adapter.BasicListAdapter
import concertrip.sopt.com.concertrip.model.Artist
import concertrip.sopt.com.concertrip.model.Concert
import concertrip.sopt.com.concertrip.model.Genre
import concertrip.sopt.com.concertrip.network.ApplicationController
import concertrip.sopt.com.concertrip.network.NetworkService
import concertrip.sopt.com.concertrip.network.response.*
import concertrip.sopt.com.concertrip.network.response.interfaces.BaseModel
import concertrip.sopt.com.concertrip.utillity.Constants
import concertrip.sopt.com.concertrip.utillity.Constants.Companion.INTENT_ARTIST
import concertrip.sopt.com.concertrip.utillity.Constants.Companion.INTENT_TAG_ID
import concertrip.sopt.com.concertrip.utillity.Constants.Companion.TYPE_ARTIST
import concertrip.sopt.com.concertrip.utillity.Constants.Companion.TYPE_CONCERT
import concertrip.sopt.com.concertrip.utillity.NetworkUtil
import concertrip.sopt.com.concertrip.utillity.Secret
import concertrip.sopt.com.concertrip.utillity.Secret.Companion.NETWORK_SUCCESS
import concertrip.sopt.com.concertrip.utillity.Secret.Companion.USER_TOKEN
import kotlinx.android.synthetic.main.activity_artist.*

import kotlinx.android.synthetic.main.content_artist.*
import kotlinx.android.synthetic.main.content_header.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArtistActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener, OnResponse {

    private var isGenre: Boolean = true
    private var artistId: String = "5c298b2a3eea39d2b00ca7d4"

//    private var isGenre: Boolean = false
//    private var artistId: String ="5c287b713eea39d2b0049f3f"

    lateinit var artist: Artist
    lateinit var genre: Genre
    var dataList = arrayListOf<Concert>()
    private lateinit var adapter: BasicListAdapter

    var dataListMember = arrayListOf<Artist>()
    lateinit var memberListAdapter: BasicListAdapter

    private val networkService: NetworkService by lazy {
        ApplicationController.instance.networkService
    }

     var mGlideRequestManager : RequestManager? = null



    private val RECOVERY_DIALOG_REQUEST = 1

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, youTubePlayer: YouTubePlayer?, b: Boolean) {
        if (!b && ::artist.isInitialized) {
//            val youtubeUrlList = artist.youtubeUrl!!.split("?v=")
//            youTubePlayer?.cueVideo(youtubeUrlList[youtubeUrlList.size - 1])  //http://www.youtube.com/watch?v=IA1hox-v0jQ

            youTubePlayer?.cueVideo(artist.youtubeUrl)
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


    override fun onSuccess(obj: BaseModel, position: Int?) {

        progress_bar.visibility=View.GONE

        if(obj is MessageResponse) {

            if ( ::artist.isInitialized) {
                artist.subscribe = !artist.subscribe
                toggleFollowBtn(artist.subscribe)


                if(artist.subscribe)
                    ColorToast(this,getString(R.string.txt_calendar_added))

                else
                    ColorToast(this,getString(R.string.txt_calendar_minus))

            }
        }

    }

    override fun onFail(status: Int) {

        progress_bar.visibility=View.GONE
        ColorToast(this,getString(R.string.txt_try_again))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist)


        isGenre = intent.getIntExtra(INTENT_ARTIST, TYPE_ARTIST) != TYPE_ARTIST


        if (intent.hasExtra(INTENT_TAG_ID))
            artistId = intent.getStringExtra(INTENT_TAG_ID)

        mGlideRequestManager = Glide.with(this)

// 장르인지 아티스트인지 intent에서 받아오기

        initialUI()
        connectRequestData(artistId)
    }

    override fun onResume() {
        super.onResume()

        connectRequestData(artistId)
    }

    fun initialUI() {
        adapter = BasicListAdapter(this, dataList, TYPE_CONCERT)
        recycler_view.adapter = adapter

        memberListAdapter = BasicListAdapter(this, dataListMember, BasicListAdapter.MODE_THUMB)
        recycler_view_member.adapter = memberListAdapter

        scroll_view.smoothScrollTo(0, 0)


        btn_follow.setOnClickListener {
            if (isGenre)
                NetworkUtil.subscribeGenre(networkService, this, artistId)
            else
                NetworkUtil.subscribeArtist(networkService, this, artistId)

        }

        btn_back.setOnClickListener {
            finish()
        }

    }

    private fun toggleFollowBtn(b: Boolean) {
        if (b) {
            btn_follow.setImageDrawable(getDrawable(R.drawable.ic_header_likes_selected))
            //iv_small_follow.setImageDrawable(getDrawable(R.drawable.ic_header_likes_selected))

        } else {
            btn_follow.setImageDrawable(getDrawable(R.drawable.ic_header_likes_unselected))
            //iv_small_follow.setImageDrawable(getDrawable(R.drawable.ic_header_likes_selected)) // 다른 이미지로
        }
    }



    private fun updateUI() {
        if (dataListMember.size == 0)
            li_member.visibility = View.GONE
        else
            li_member.visibility = View.VISIBLE
    }


    private fun updateConcertList(list: ArrayList<Concert>) {
        dataList.clear()
        dataList.addAll(list)
        adapter.notifyDataSetChanged()
    }

    private fun updateArtistData(artist: Artist) {


        btn_follow.setImageDrawable(if (artist.subscribe) getDrawable(R.drawable.ic_header_likes_selected) else getDrawable(R.drawable.ic_header_likes_unselected))
        //iv_small_follow.setImageDrawable(if (artist.subscribe) getDrawable(R.drawable.ic_header_likes_selected) else getDrawable(R.drawable.ic_header_likes_unselected))


        if (URLUtil.isValidUrl(artist.backImg))
            mGlideRequestManager?.load(artist.backImg)?.into(iv_back)
//        else
//            // 기본이미지로 설정
        if (URLUtil.isValidUrl(artist.profileImg))
            mGlideRequestManager?.load(artist.profileImg)?.apply(RequestOptions.circleCropTransform())?.into(iv_profile)
//        else
//            // 기본이미지로 설정

        tv_title.text = artist.name
        tv_tag.text = artist.subscribeNum.toString()

        getYouTubePlayerProvider().initialize(Secret.YOUTUBE_API_KEY, this)
    }

    private fun updateMemberList(list: ArrayList<Artist>) {
        dataListMember.clear()
        dataListMember.addAll(list)
        memberListAdapter.notifyDataSetChanged()
    }


    private var LOG_TAG = ""

    private fun connectRequestData(id: String) {

        progress_bar.visibility=View.VISIBLE

        // 서버에서 넘어오는 데이터 구조가 달라서 따로 구현할 수 밖에 없음ㅠ
        if (isGenre) {
            LOG_TAG = "/api/genre/detail"
            val getGenreResponse: Call<GetGenreResponse> = networkService.getGenre(USER_TOKEN, artistId)
            getGenreResponse.enqueue(object : Callback<GetGenreResponse> {

                override fun onFailure(call: Call<GetGenreResponse>?, t: Throwable?) {

                    progress_bar.visibility=View.GONE

                    Log.e(Constants.LOG_NETWORK, "$LOG_TAG $t")
                }

                override fun onResponse(call: Call<GetGenreResponse>?, response: Response<GetGenreResponse>?) {

                    progress_bar.visibility=View.GONE


                    response?.let { res ->
                        if (res.body()?.status == NETWORK_SUCCESS) {
                            res.body()?.data?.let {
                                Log.d(Constants.LOG_NETWORK, "$LOG_TAG :${response.body().toString()}")
                                genre = it.toGenre()
                                artist=genre
                                artistId=genre._id

                                updateConcertList(ArrayList(genre.concertList))
                                updateArtistData(genre)
                                updateUI()
                            }

                        } else {
                            ColorToast(applicationContext, "해당 테마를 찾을 수 없습니다.")
                            Log.d(Constants.LOG_NETWORK, "$LOG_TAG: fail ${response.body()?.message}")
                            finish()
                        }
                    }

                }
            })
        } else {
            LOG_TAG = "/api/artist/detail"

            val getArtistResponse: Call<GetArtistResponse> = networkService.getArtist(USER_TOKEN, artistId)
            getArtistResponse.enqueue(object : Callback<GetArtistResponse> {
                override fun onFailure(call: Call<GetArtistResponse>?, t: Throwable?) {
                    progress_bar.visibility=View.GONE

                    Log.e(Constants.LOG_NETWORK, "$LOG_TAG $t")
                }

                override fun onResponse(call: Call<GetArtistResponse>?, response: Response<GetArtistResponse>?) {


                    progress_bar.visibility=View.GONE

                    response?.let { res ->

                        if (response.body()?.status == NETWORK_SUCCESS) {
                            Log.d(Constants.LOG_NETWORK, "$LOG_TAG :${response.body().toString()}")
                            res.body()?.data?.let {
                                artist = it.toArtist()
                                updateConcertList(ArrayList(artist.concertList)) // 굳이 param으로 안넘겨줘도됨!
                                updateMemberList(ArrayList(artist.memberList))
                                updateArtistData(artist)
                                updateUI()
                            }
                        } else {
                            ColorToast(applicationContext, "해당 아티스트를 찾을 수 없습니다.")
                            Log.d(Constants.LOG_NETWORK, "$LOG_TAG: fail ${response.body()?.message}")
                            finish()
                        }

                    }


                }
            })
        }
    }

}