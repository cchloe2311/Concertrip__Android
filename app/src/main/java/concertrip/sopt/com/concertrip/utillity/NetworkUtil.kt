package concertrip.sopt.com.concertrip.utillity

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import concertrip.sopt.com.concertrip.interfaces.OnResponse
import concertrip.sopt.com.concertrip.network.NetworkService
import concertrip.sopt.com.concertrip.network.USGS_REQUEST_URL
import concertrip.sopt.com.concertrip.network.response.GetSearchResponse
import concertrip.sopt.com.concertrip.network.response.MessageResponse
import concertrip.sopt.com.concertrip.network.response.interfaces.BaseModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetworkUtil {

    companion object {


        private const val LOG_SUBSCRIBE_ARTIST = "/api/subscribe/artist"
        fun subscribeArtist(networkService: NetworkService, listener: OnResponse?, _id: String) =
            subscribeArtist(networkService, listener, _id, null)

        fun subscribeArtist(networkService: NetworkService, listener: OnResponse?, _id: String, position: Int?) {
            val jsonObject = JSONObject()
            jsonObject.put(USGS_REQUEST_URL.JSON_ARTIST_ID, _id)
            val gsonObject = JsonParser().parse(jsonObject.toString()) as JsonObject

            Log.d(Constants.LOG_NETWORK, "$LOG_SUBSCRIBE_ARTIST, POST :$gsonObject")
            val subscribeArtist: Call<MessageResponse> =
                networkService.postSubScribeArtist(1, gsonObject)
            subscribeArtist.enqueue(object : Callback<MessageResponse> {
                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Log.e(Constants.LOG_NETWORK, t.toString())
                    listener?.onFail()
                }

                //통신 성공 시 수행되는 메소드
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    Log.d(Constants.LOG_NETWORK, response.errorBody()?.string() ?: response.message())
                    if (response.isSuccessful && response.body()?.status==200) {
                        Log.d(Constants.LOG_NETWORK, "$LOG_SUBSCRIBE_ARTIST :${response.body()}")
                        listener?.onSuccess(response.body() as BaseModel, position)
                    } else {
                        Log.d(Constants.LOG_NETWORK, "$LOG_SUBSCRIBE_ARTIST : fail ${response.body()?.message}")
                        listener?.onFail()
                    }
                }
            })
        }


        private const val LOG_SUBSCRIBE_GENRE = "/api/subscribe/genre"
        fun subscribeGenre(networkService: NetworkService, listener: OnResponse?, _id: String) =
            subscribeGenre(networkService, listener, _id, null)

        fun subscribeGenre(networkService: NetworkService, listener: OnResponse?, _id: String, position: Int?) {
            val jsonObject = JSONObject()
            jsonObject.put(USGS_REQUEST_URL.JSON_GENRE_ID, _id)
            val gsonObject = JsonParser().parse(jsonObject.toString()) as JsonObject

            Log.d(Constants.LOG_NETWORK, "$LOG_SUBSCRIBE_GENRE, POST :$gsonObject")

            val subscribeGenre: Call<MessageResponse> =
                networkService.postSubscribeGenre(1, gsonObject)
            subscribeGenre.enqueue(object : Callback<MessageResponse> {

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Log.e(Constants.LOG_NETWORK, t.toString())
                    listener?.onFail()
                }

                //통신 성공 시 수행되는 메소드
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    Log.d(Constants.LOG_NETWORK, response.errorBody()?.string() ?: response.message())

                    if (response.isSuccessful && response.body()?.status==200) {
                        Log.d(Constants.LOG_NETWORK, "$LOG_SUBSCRIBE_GENRE :${response.body()}")
                        listener?.onSuccess(response.body() as BaseModel, position)
                    } else {
                        Log.d(Constants.LOG_NETWORK, "$LOG_SUBSCRIBE_GENRE : fail  ${response.body()?.message}")
                        listener?.onFail()
                    }
                }
            })
        }


        private const val LOG_SUBSCRIBE_CONCERT = "/api/subscribe/concert"
        fun subscribeConcert(networkService: NetworkService, listener: OnResponse?, _id: String) =
            subscribeConcert(networkService, listener, _id, null)

        fun subscribeConcert(networkService: NetworkService, listener: OnResponse?, _id: String, position: Int?) {
            val jsonObject = JSONObject()
            jsonObject.put(USGS_REQUEST_URL.JSON_CONCERT_ID, _id)
            val gsonObject = JsonParser().parse(jsonObject.toString()) as JsonObject

            Log.d(Constants.LOG_NETWORK, "$LOG_SUBSCRIBE_CONCERT, POST :$gsonObject")

            val subscribeConcert: Call<MessageResponse> =
                networkService.postSubscribeConcert(1, gsonObject)
            subscribeConcert.enqueue(object : Callback<MessageResponse> {

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Log.e(Constants.LOG_NETWORK, t.toString())
                    listener?.onFail()
                }

                //통신 성공 시 수행되는 메소드
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    Log.d(Constants.LOG_NETWORK, response.errorBody()?.string() ?: response.message())

                    if (response.isSuccessful && response.body()?.status==200) {
                        Log.d(Constants.LOG_NETWORK, "$LOG_SUBSCRIBE_CONCERT :${response.body()}")
                        listener?.onSuccess(response.body() as BaseModel, position)
                    } else {
                        Log.d(Constants.LOG_NETWORK, "$LOG_SUBSCRIBE_CONCERT: fail  ${response.body()?.message}")
                        listener?.onFail()
                    }
                }
            })
        }


        private const val LOG_SEARCH = "/api/search"
        fun search(networkService: NetworkService, listener: OnResponse?, tag: String) =
            search(networkService, listener, tag, null)

        fun search(networkService: NetworkService, listener: OnResponse?, tag: String, position: Int?) {

            Log.d(Constants.LOG_NETWORK, "$LOG_SEARCH, GET ? tag=$tag")

            val search: Call<GetSearchResponse> =
                networkService.getSearch(1, tag)
            search.enqueue(object : Callback<GetSearchResponse> {

                override fun onFailure(call: Call<GetSearchResponse>, t: Throwable) {
                    Log.e(Constants.LOG_NETWORK, t.toString())
                    listener?.onFail()
                }

                //통신 성공 시 수행되는 메소드
                override fun onResponse(call: Call<GetSearchResponse>, response: Response<GetSearchResponse>) {
                    Log.d(Constants.LOG_NETWORK, response.errorBody()?.string() ?: response.message())

                    if (response.isSuccessful) {
                        Log.d(Constants.LOG_NETWORK, "$LOG_SEARCH :${response.body()?.status}")
                        response.body()?.let {
                            if (it.status == 200) {
                                Log.d(Constants.LOG_NETWORK, "$LOG_SEARCH :${response.body().toString()}")
                                listener?.onSuccess(response.body() as BaseModel, position)
                            } else{
                                Log.d(Constants.LOG_NETWORK, "$LOG_SEARCH: fail  ${response.body()?.message}")
                                listener?.onFail()
                            }
                        }

                    } else {
                        listener?.onFail()

                    }
                }
            })
        }


    }
}