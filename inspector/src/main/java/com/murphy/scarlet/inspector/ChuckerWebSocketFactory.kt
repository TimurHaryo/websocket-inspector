package com.murphy.scarlet.inspector

import com.tinder.scarlet.WebSocket

class ChuckerWebSocketFactory(
    private val delegate: WebSocket.Factory,
    private val inspector: ScarletInspector,
    private val url: String,
    private val type: String = "DEFAULT",
    private val launchWithNewSession: Boolean = false,
    private val isDebug: Boolean = true
) : WebSocket.Factory {

    override fun create(): WebSocket {
        if (!isDebug) {
            inspector.clearAllData()
            return delegate.create()
        }
        return ChuckerScarletWebSocket(delegate.create(), inspector, url, type, launchWithNewSession)
    }
}
