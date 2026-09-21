package com.murphy.scarlet.inspector

import android.content.Context
import android.content.Intent
import com.murphy.scarlet.inspector.internal.ui.compose.InspectorComposeActivity

object InspectorLauncher {
    fun getLaunchIntent(context: Context): Intent {
        return InspectorComposeActivity.createIntent(context)
    }
}
