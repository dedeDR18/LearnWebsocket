package com.example.learnwebsocket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.learnwebsocket.model.Coin
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory

class MainActivity : AppCompatActivity() {

    companion object {
        const val WEB_SOCKET_URL = "wss://ws-feed.pro.coinbase.com"
        const val TAG = "WEBSOCKET"
    }

    private lateinit var websocketclient: WebSocketClient
    private lateinit var btcPrice: TextView
    private lateinit var ltcPrice: TextView
    private lateinit var ethPrice: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        iniViews()

    }

    private fun iniViews(){
        btcPrice = findViewById(R.id.btc_price_tv)
        ltcPrice = findViewById(R.id.ltc_price_tv)
        ethPrice = findViewById(R.id.eth_price_tv)
    }

    private fun initWebsocket(){
        val coinbaseUri: URI? = URI(WEB_SOCKET_URL)
        createWebsocketClient(coinbaseUri)
        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        websocketclient.setSocketFactory(socketFactory)
        websocketclient.connect()
    }

    private fun createWebsocketClient(coinbaseUri: URI?) {
        websocketclient = object : WebSocketClient(coinbaseUri){
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "onOpen")
                subscribe()
            }

            override fun onMessage(message: String?) {
                Log.d(TAG, "onMessage: $message")
                setUpPriceText(message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose")
                unsubscribe()
            }

            override fun onError(ex: Exception?) {
                Log.e(TAG, "onError: ${ex?.message}")
            }

        }
    }

    private fun closeWebsocket(){
        websocketclient.close()
    }

    private fun setUpPriceText(message: String?) {
        message?.let { message ->
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<Coin> = moshi.adapter(Coin::class.java)
            val coin = adapter.fromJson(message)
            runOnUiThread{
                when(coin?.product_id){
                    "BTC-USD" -> btcPrice.text = "1 BTC: ${coin?.price} $"
                    "LTC-USD" -> ltcPrice.text = "1 LTC: ${coin?.price} $"
                    "ETH-USD" -> ethPrice.text = "1 ETH: ${coin?.price} $"
                }
            }
        }
    }

    private fun subscribe() {
        websocketclient.send(
                "{\n" +
                        "    \"type\": \"subscribe\",\n" +
                        "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"BTC-USD\",\"LTC-USD\",\"ETH-USD\"] }]\n" +
                        "}"
        )
    }

    private fun unsubscribe() {
        websocketclient.send(
                "{\n" +
                        "    \"type\": \"unsubscribe\",\n" +
                        "    \"channels\": [\"ticker\"]\n" +
                        "}"
        )
    }

    override fun onResume() {
        super.onResume()
        initWebsocket()
    }

    override fun onPause() {
        super.onPause()
        closeWebsocket()
    }
}