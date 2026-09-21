package com.example.websocketinspector

import com.example.websocketinspector.proto.ChannelMessage
import com.example.websocketinspector.proto.SubscribeRequest
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.flow.Flow

interface WebSocketFinancialApi {
    @Send
    fun sendPayload(payload: SubscribeRequest): Boolean

    @Receive
    fun observeEvent(): Flow<WebSocket.Event>

    @Receive
    fun observeResponse(): Flow<ChannelMessage>
}
