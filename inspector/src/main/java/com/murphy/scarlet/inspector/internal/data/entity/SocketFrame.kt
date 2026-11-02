package com.murphy.scarlet.inspector.internal.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "socket_frames",
    foreignKeys = [
        ForeignKey(
            entity = SocketSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class SocketFrame(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val direction: Direction, // Sent or Received
    val type: Type, // Text, Binary, Ping, Pong, Close
    val payload: String, // Text content or Base64 of binary
    val payloadSize: Long
) {
    enum class Direction { SENT, RECEIVED }
    enum class Type { TEXT, BINARY, PING, PONG, CLOSE }
}
