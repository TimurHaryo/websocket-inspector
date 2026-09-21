package com.murphy.scarlet.inspector.internal

import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks active session IDs for the current process lifecycle.
 * This ensures that when the app reconnects (but hasn't been killed),
 * we can reuse the same session ID even if "launchWithNewSession" is true.
 */
internal object SessionTracker {
    val activeSessionIds = ConcurrentHashMap<String, Long>()
}
