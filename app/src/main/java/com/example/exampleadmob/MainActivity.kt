package com.example.exampleadmob

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocketListener
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


data class Message(
    val content: String,
    val nickname: String,
    val date: String? = null
)







interface MessageApiService {
    @GET("api/messages")
    fun getMessages(): Call<List<Message>>

    @POST("api/send")
    fun sendMessage(@Body message: Message): Call<Message>
}

object ApiClient {
    private const val BASE_URL = "http://192.168.0.15:8080/"

    val messageService: MessageApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MessageApiService::class.java)
    }
}






class MainActivity : AppCompatActivity() {

    private lateinit var adView: AdView

    private lateinit var webSocket: WebSocket

    private var rewardedAd: RewardedAd? = null


    private lateinit var messagesText: TextView
    private lateinit var nicknameInput: EditText
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        messagesText = findViewById(R.id.messages_text)
        nicknameInput = findViewById(R.id.nickname_input)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)
        adView = findViewById(R.id.adView)




        MobileAds.initialize(this) {}





        fun connectWebSocket() {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("ws://192.168.0.15:8080/ws")
                .build()


            val listener = MyWebSocketListener { msg ->
                runOnUiThread {
                    messagesText.append("[${msg.date}] ${msg.nickname}: ${msg.content}\n")
                }
            }


            fun onDestroy() {
                super.onDestroy()
                webSocket.close(1000, "Kapatılıyor")
            }
        }

        connectWebSocket()

        fun loadBannerAd() {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
        loadBannerAd()



        fun fetchMessages() {
            ApiClient.messageService.getMessages().enqueue(object : Callback<List<Message>> {
                override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                    if (response.isSuccessful) {
                        val builder = StringBuilder()
                        response.body()?.forEach {
                            builder.append("[${it.date}] ${it.nickname}: ${it.content}\n")
                        }
                        messagesText.text = builder.toString()
                    } else {
                        messagesText.text = "Sunucu hatası: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                    messagesText.text = "Bağlantı hatası: ${t.message}"
                }
            })
        }


        fetchMessages()


        sendButton.setOnClickListener {

            val nick = nicknameInput.text.toString()
            val msg = messageInput.text.toString()
            if (nick.isNotEmpty() && msg.isNotEmpty()) {
                val newMsg = Message(content = msg, nickname = nick)
                ApiClient.messageService.sendMessage(newMsg).enqueue(object : Callback<Message> {
                    override fun onResponse(call: Call<Message>, response: Response<Message>) {
                        if (response.isSuccessful) {
                            messageInput.text.clear()
                            fetchMessages()
                        } else {
                            Toast.makeText(this@MainActivity, "anası sikik orosbu evladı üst komu hatası: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Message>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "anası sikik orosbu evladı üst komşu hatası: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this@MainActivity, "mesaj göndermek için orosbu evladı üst komşuyu götten sikmek gerekiyor", Toast.LENGTH_SHORT).show()
            }
        }
    }
}









