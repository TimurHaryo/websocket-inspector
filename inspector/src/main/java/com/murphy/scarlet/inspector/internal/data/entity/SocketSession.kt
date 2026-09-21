package com.murphy.scarlet.inspector.internal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "socket_sessions")
data class SocketSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val method: String? = null,
    val protocol: String? = null,
    val requestHeaders: String? = null, // JSON or formatted string
    val type: String = "DEFAULT",
    val timestamp: Long = System.currentTimeMillis()
)
