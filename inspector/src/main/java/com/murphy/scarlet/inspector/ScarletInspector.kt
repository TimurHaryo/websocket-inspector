package com.murphy.scarlet.inspector

import android.content.Context
import com.murphy.scarlet.inspector.internal.data.room.InspectorDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ScarletInspector(context: Context) {
    internal val database = InspectorDatabase.create(context)
    internal val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun clearAllData() {
        ioScope.launch {
            database.inspectorDao().clearAll()
        }
    }
}
