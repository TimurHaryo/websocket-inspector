package com.example.websocketinspector

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.channels.ReceiveChannel

interface EchoService {
    @Receive
    fun observeWebSocketEvent(): ReceiveChannel<WebSocket.Event>

    @Send
    fun sendText(text: String)
    
    @Receive
    fun observeText(): ReceiveChannel<String>
}
