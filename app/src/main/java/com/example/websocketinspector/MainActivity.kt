package com.example.websocketinspector

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.websocketinspector.proto.Channel
import com.example.websocketinspector.proto.SubscribeRequest
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.murphy.scarlet.inspector.ChuckerWebSocketFactory
import com.murphy.scarlet.inspector.ScarletInspector
import com.murphy.scarlet.inspector.internal.InspectorNotificationManager
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : AppCompatActivity() {

    private lateinit var financeApi: WebSocketFinancialApi
    private lateinit var financeApi2: WebSocketFinancialApi
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    private lateinit var statusText: TextView
    private lateinit var connectButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var sendButton: Button
    private lateinit var messageInput: EditText
    
    // Define as property
    private lateinit var scarletInspector: ScarletInspector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        connectButton = findViewById(R.id.connectButton)
        disconnectButton = findViewById(R.id.disconnectButton)
        sendButton = findViewById(R.id.sendButton)
        messageInput = findViewById(R.id.messageInput)

        // Initialize here
        scarletInspector = ScarletInspector(this)

        setupScarlet()

        connectButton.setOnClickListener {
            setupScarlet()
        }
        
        // Actually Scarlet connects automatically when lifecycle starts. 
        // We will just let it be for now.

        sendButton.setOnClickListener {
            val text = messageInput.text.toString()
            if (text.isNotEmpty()) {
                scope.launch(Dispatchers.IO) {
                    val request = SubscribeRequest.newBuilder()
                        .setClientId("demo-client")
                        .setChannel(
                            Channel.newBuilder()
                                .addQuotes(text)
                        )
                        .build()
                    financeApi.sendPayload(request)
                    financeApi2.sendPayload(request)
                }
                messageInput.text.clear()
            }
        }

        // Show persistent notification
        InspectorNotificationManager.showNotification(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Optional: dismiss? But user requested "persistent".
        // Usually debug tools stay persistent even if app backgrounded, until process kill.
        // If sticky, we leave it. "Persistent" usually implies it stays.
    }

    private fun setupScarlet() {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()

        // WRAP THE FACTORY
        val socketUrl = "wss://echo.websocket.org"
        val baseFactory = okHttpClient.newWebSocketFactory(socketUrl)
        val factoryWrapper = ChuckerWebSocketFactory(
            delegate = baseFactory,
            inspector = scarletInspector,
            url = socketUrl,
            type = "Financial",
            launchWithNewSession = false,
            isDebug = true
        )
        val factoryWrapper2 = ChuckerWebSocketFactory(
            delegate = baseFactory,
            inspector = scarletInspector,
            url = socketUrl,
            type = "Trading",
            launchWithNewSession = false,
            isDebug = true
        )

        val scarlet = Scarlet.Builder()
            .webSocketFactory(factoryWrapper)
            .lifecycle(AndroidLifecycle.ofApplicationForeground(application))
            .addMessageAdapterFactory(com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter.Factory())
            .addStreamAdapterFactory(FlowStreamAdapter.Factory()) // Use custom Flow adapter
            .build()

        val scarlet2 = Scarlet.Builder()
            .webSocketFactory(factoryWrapper2)
            .lifecycle(AndroidLifecycle.ofApplicationForeground(application))
            .addMessageAdapterFactory(com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter.Factory())
            .addStreamAdapterFactory(FlowStreamAdapter.Factory()) // Use custom Flow adapter
            .build()

        financeApi = scarlet.create(WebSocketFinancialApi::class.java)
        financeApi2 = scarlet2.create(WebSocketFinancialApi::class.java)

        observeEvents()
    }

    private fun observeEvents() {
        scope.launch {
            financeApi.observeEvent().collect { event ->
                when(event) {
                    is WebSocket.Event.OnConnectionOpened<*> -> {
                        statusText.text = "Status: Connected"
                        enableButtons(true)
                    }
                    is WebSocket.Event.OnConnectionClosed -> {
                        statusText.text = "Status: Closed"
                        enableButtons(false)

                    }
                    is WebSocket.Event.OnConnectionFailed -> {
                        statusText.text = "Status: Failed (${event.throwable.message})"
                        android.util.Log.e("WebSocketInspector", "Connection Failed", event.throwable)
                        enableButtons(false)
                    }
                    else -> {}
                }
            }
        }
        
        scope.launch {
            financeApi.observeResponse().collect { response ->
                 Toast.makeText(this@MainActivity, "Received Protobuf Message", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun enableButtons(enabled: Boolean) {
        sendButton.isEnabled = enabled
        disconnectButton.isEnabled = enabled
        connectButton.isEnabled = !enabled
    }
}
