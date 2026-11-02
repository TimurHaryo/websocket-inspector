package com.murphy.scarlet.inspector.internal.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.murphy.scarlet.inspector.internal.data.entity.SocketFrame
import com.murphy.scarlet.inspector.internal.data.entity.SocketSession

@Dao
interface InspectorDao {
    @Insert
    suspend fun insertSession(session: SocketSession): Long

    @Insert
    suspend fun insertFrame(frame: SocketFrame)

    @Query("SELECT * FROM socket_sessions ORDER BY timestamp DESC")
    fun getSessions(): LiveData<List<SocketSession>>

    @Query("SELECT * FROM socket_sessions WHERE id IN (SELECT MAX(id) FROM socket_sessions GROUP BY type) ORDER BY timestamp DESC")
    fun getLatestSessionsByType(): LiveData<List<SocketSession>>

    @Query("SELECT * FROM socket_sessions WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSessionByType(type: String): LiveData<SocketSession?>

    @Query("SELECT * FROM socket_sessions WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSession(type: String): SocketSession?

    @Query("SELECT * FROM socket_frames WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getFrames(sessionId: Long): LiveData<List<SocketFrame>>

    @Query("SELECT COUNT(*) FROM socket_sessions WHERE id = :id")
    fun hasSession(id: Long): Int

    @Query("SELECT * FROM socket_frames WHERE sessionId = :sessionId AND direction = 'RECEIVED' ORDER BY timestamp DESC")
    fun getReceivedFrames(sessionId: Long): LiveData<List<SocketFrame>>

    @Query("SELECT * FROM socket_frames WHERE sessionId = :sessionId AND direction = 'SENT' ORDER BY timestamp DESC LIMIT 1")
    fun getLastSentFrame(sessionId: Long): LiveData<SocketFrame?>
    
    @Query("DELETE FROM socket_sessions")
    suspend fun clearAll()

    @Query("DELETE FROM socket_frames WHERE sessionId = :sessionId")
    suspend fun clearFrames(sessionId: Long)

    @Query("DELETE FROM socket_frames WHERE sessionId IN (SELECT id FROM socket_sessions WHERE type = :type)")
    suspend fun clearFramesByType(type: String)

    @Query("UPDATE socket_sessions SET timestamp = :timestamp WHERE id = :id")
    suspend fun updateSessionTimestamp(id: Long, timestamp: Long)
}
