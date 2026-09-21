package com.murphy.scarlet.inspector.internal.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.murphy.scarlet.inspector.internal.data.entity.SocketFrame
import com.murphy.scarlet.inspector.internal.data.entity.SocketSession

@Database(entities = [SocketSession::class, SocketFrame::class], version = 2, exportSchema = false)
abstract class InspectorDatabase : RoomDatabase() {
    abstract fun inspectorDao(): InspectorDao

    companion object {
        private const val DB_NAME = "scarlet_inspector.db"

        @Volatile
        private var INSTANCE: InspectorDatabase? = null

        fun create(context: Context): InspectorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InspectorDatabase::class.java,
                    DB_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}