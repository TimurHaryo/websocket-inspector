package com.murphy.scarlet.inspector.internal.ui.compose.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murphy.scarlet.inspector.internal.data.entity.SocketSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SocketTypeItemComponent(session: SocketSession, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.type,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Last active: ${dateFormat.format(Date(session.timestamp))}",
                fontSize = 12.sp
            )
        }
    }
}
