package com.example.exampleadmob

import com.google.gson.Gson
import okhttp3.*
import okio.ByteString

class MyWebSocketListener(
    private val onNewMessage: (Message) -> Unit
) : okhttp3.WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("WebSocket bağlantısı açıldı")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Yeni mesaj: $text")
        val msg = Gson().fromJson(text, Message::class.java)
        onNewMessage(msg)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        println("Binary mesaj: ${bytes.hex()}")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        println("WebSocket kapanıyor: $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("WebSocket hatası: ${t.message}")
    }
}