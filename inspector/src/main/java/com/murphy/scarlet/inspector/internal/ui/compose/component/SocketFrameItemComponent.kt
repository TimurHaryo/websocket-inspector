package com.murphy.scarlet.inspector.internal.ui.compose.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murphy.scarlet.inspector.internal.data.entity.SocketFrame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SocketFrameItemComponent(frame: SocketFrame) {
    val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = dateFormat.format(Date(frame.timestamp)),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = frame.payload,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        }
    }
}
