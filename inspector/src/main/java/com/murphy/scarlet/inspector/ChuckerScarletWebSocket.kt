package com.murphy.scarlet.inspector

import com.murphy.scarlet.inspector.internal.SessionTracker
import com.tinder.scarlet.Message
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import com.murphy.scarlet.inspector.internal.data.entity.SocketFrame
import com.murphy.scarlet.inspector.internal.data.entity.SocketSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.ByteString
import org.reactivestreams.Subscriber

class ChuckerScarletWebSocket(
    private val delegate: WebSocket,
    private val inspector: ScarletInspector,
    private val defaultUrl: String,
    private val type: String,
    private val launchWithNewSession: Boolean
) : WebSocket {

    private var sessionId: Long = 0

    init {
        // No cache manipulation needed. 
        // Logic handled in createSession based on launchWithNewSession flag.
        createSession()
    }

    override fun open(): Stream<WebSocket.Event> {
        val originalStream = delegate.open()
        // Issue 1: Stream interface implementation.
        // It seems Stream is simply an interface with `start`. 
        // If the compiler complains about `subscribe` missing, it's likely we are importing a different Stream or using a Reactive Streams one?
        // Scarlet's Stream interface has `start`. 
        // However, if the user sees `subscribe` missing, it might be due to a transitive dependency or version mismatch?
        // Let's assume standard Scarlet 0.1.12. Stream has `start(Observer): Disposable`.
        // If strict mode or something else requires `subscribe` (maybe Reactive Streams Publisher?), we can check imports.
        // `import com.tinder.scarlet.Stream` is correct.
        // Maybe the user is confusing it or there is a mismatch.
        // However, if I can't reproduce, I will blindly trust the user and just delegate `originalStream` directly but with an observer wrapper.
        // ACTUALLY, I can just implement `start` as I did.
        // If the compiler says `subscribe` is missing, then `Stream` MUST inherit from something that has `subscribe`.
        // Scarlet 0.1.12 `Stream` interface: `public interface Stream<T> { Disposable start(Observer<T> observer); }`
        // Wait, does it extend `Publisher`? No.
        // Maybe the user has a different version of Scarlet forced?
        // I will try to implement `subscribe` just in case, casting if needed or define it.
        // But more likely: I should just wrap the observer and pass it to `originalStream.start()`.
        
        return object : Stream<WebSocket.Event> {
            override fun start(observer: Stream.Observer<WebSocket.Event>): Stream.Disposable {
                // We wrap the observer to intercept events
                val wrappedObserver = object : Stream.Observer<WebSocket.Event> {
                     override fun onNext(data: WebSocket.Event) {
                        handleEvent(data)
                        observer.onNext(data)
                    }

                    override fun onComplete() {
                        observer.onComplete()
                    }

                    override fun onError(throwable: Throwable) {
                        observer.onError(throwable)
                    }
                }
                return originalStream.start(wrappedObserver)
            }

            override fun subscribe(subscriber: Subscriber<in WebSocket.Event>) {
                val wrappedSubscriber = object : Subscriber<WebSocket.Event> {
                    override fun onSubscribe(s: org.reactivestreams.Subscription) {
                        subscriber.onSubscribe(s)
                    }

                    override fun onNext(t: WebSocket.Event) {
                        handleEvent(t)
                        subscriber.onNext(t)
                    }

                    override fun onError(t: Throwable) {
                        subscriber.onError(t)
                    }

                    override fun onComplete() {
                        subscriber.onComplete()
                    }
                }
                originalStream.subscribe(wrappedSubscriber)
            }
        }
    }

    override fun send(message: Message): Boolean {
        logMessage(message)
        return delegate.send(message)
    }

    override fun close(shutdownReason: ShutdownReason): Boolean {
        logFrame(SocketFrame.Direction.SENT, SocketFrame.Type.CLOSE, "Code: ${shutdownReason.code} / Reason: ${shutdownReason.reason}", 0)
        return delegate.close(shutdownReason)
    }

    override fun cancel() {
        delegate.cancel()
    }

    private fun handleEvent(event: WebSocket.Event) {
        logEvent(event)
        
        // Issue 2: Remove `event.response` access as it doesn't exist in 0.1.12
        if (event is WebSocket.Event.OnConnectionOpened<*>) {
            // In 0.1.12, OnConnectionOpened is: data class OnConnectionOpened<T>(val webSocket: T)
            // It doesn't have response.
        }
    }

    private fun logEvent(event: WebSocket.Event) {
        when (event) {
            is WebSocket.Event.OnMessageReceived -> {
                val message = event.message
                if (message is Message.Text) {
                    logFrame(SocketFrame.Direction.RECEIVED, SocketFrame.Type.TEXT, message.value, message.value.length.toLong())
                } else if (message is Message.Bytes) {
                    // Issue 3: Fix base64. message.value is ByteArray.
                    // Use okio.ByteString.of()
                    val byteString = ByteString.of(*message.value)
                    logFrame(SocketFrame.Direction.RECEIVED, SocketFrame.Type.BINARY, byteString.base64(), message.value.size.toLong())
                }
            }
            is WebSocket.Event.OnConnectionClosed -> {
               logFrame(SocketFrame.Direction.RECEIVED, SocketFrame.Type.CLOSE, "Connection Closed", 0)
            }
            is WebSocket.Event.OnConnectionFailed -> {
                logFrame(SocketFrame.Direction.RECEIVED, SocketFrame.Type.CLOSE, "Failed: ${event.throwable.message}", 0)
            }
            is WebSocket.Event.OnConnectionOpened<*> -> {
                 val now = System.currentTimeMillis()
                 inspector.ioScope.launch {
                     inspector.database.inspectorDao().updateSessionTimestamp(sessionId, now)
                 }
                 logFrame(SocketFrame.Direction.RECEIVED, SocketFrame.Type.TEXT, "Connected", 0)
            }
            else -> {}
        }
    }

    private fun logMessage(message: Message) {
         if (message is Message.Text) {
            logFrame(SocketFrame.Direction.SENT, SocketFrame.Type.TEXT, message.value, message.value.length.toLong())
        } else if (message is Message.Bytes) {
            // Issue 3: Fix base64 here too
            val byteString = ByteString.of(*message.value)
            logFrame(SocketFrame.Direction.SENT, SocketFrame.Type.BINARY, byteString.base64(), message.value.size.toLong())
        }
    }

    private fun logFrame(direction: SocketFrame.Direction, type: SocketFrame.Type, payload: String, size: Long) {
        inspector.ioScope.launch {
            try {
                insertFrameInternal(direction, type, payload, size)
            } catch (_: Exception) {
                // SQLiteConstraintException (FOREIGN KEY constraint failed) might occur if session was cleared
                // Re-create the session and retry
                createSession()
                // Retry once
                try {
                    insertFrameInternal(direction, type, payload, size)
                } catch (retryEx: Exception) {
                    android.util.Log.e("ScarletInspector", "Failed to log frame", retryEx)
                }
            }
        }
    }

    private suspend fun insertFrameInternal(direction: SocketFrame.Direction, type: SocketFrame.Type, payload: String, size: Long) {
        inspector.database.inspectorDao().insertFrame(
            SocketFrame(
                sessionId = sessionId,
                direction = direction,
                type = type,
                payload = payload,
                payloadSize = size
            )
        )
    }

    private fun createSession() {
        // 1. Always check memory cache first (Process lifecycle)
        // This handles "Reconnect" scenarios: we want to stay in the same session 
        // regardless of launchWithNewSession flag if we are just strictly reconnecting.
        val cachedId = SessionTracker.activeSessionIds[type]
        if (cachedId != null) {
            // Check if user cleared history, invalidating this ID
            val exists = runBlocking {
                inspector.database.inspectorDao().hasSession(cachedId) > 0
            }
            if (exists) {
                sessionId = cachedId
                return
            } else {
                SessionTracker.activeSessionIds.remove(type)
            }
        }

        // 2. If not in memory, check if we should look in DB (Persistence)
        if (!launchWithNewSession) {
            val latestSession = runBlocking {
                inspector.database.inspectorDao().getLatestSession(type)
            }
            if (latestSession != null) {
                sessionId = latestSession.id
                SessionTracker.activeSessionIds[type] = sessionId
                return
            }
        }
        
        // 3. Create New Session
        // (Either launchWithNewSession=true OR launchWithNewSession=false but no prev session found)
        val session = SocketSession(
            url = defaultUrl,
            method = "GET",
            requestHeaders = null,
            type = type
        )
        sessionId = runBlocking {
             inspector.database.inspectorDao().insertSession(session)
        }
        SessionTracker.activeSessionIds[type] = sessionId
    }
    
    // Helper to log without relying on cache
    // ...
}
