package com.example.doantotnghiep.ui.dashboard

import androidx.compose.foundation.background
import com.example.doantotnghiep.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.doantotnghiep.data.remote.NotificationLog
import com.example.doantotnghiep.ui.theme.*
import com.example.doantotnghiep.utils.formatTimeAgo

@Composable
fun NotificationDiaLog(
    logs: List<NotificationLog>,
    onDismiss: () -> Unit,
    onItemClick: (NotificationLog) -> Unit,
    onMarkAllRead: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .heightIn(max = 650.dp),
            shape = RoundedCornerShape(28.dp),
            color = GhostWhite
        ) {
            Column(Modifier.padding(top = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.bar_notification),
                        color = EerieBlack,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    TextButton(onClick = onMarkAllRead) {
                        Text(stringResource(R.string.notification_mark_all_read), fontSize = 13.sp, color = AreaContent)
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(logs) { log ->
                        NotificaionItem(log = log, onClick = {onItemClick(log)})
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EerieBlack)
                ) {
                    Text(stringResource(R.string.notification_close), color = Color.White)
                }
            }
        }
    }
}

@Composable
fun NotificaionItem(log: NotificationLog, onClick: () -> Unit) {
    val contentAlpha = if(log.isRead) 0.5f else 1f
    val statusColor = if(log.type == 2) SystemRed else SystemOrange
    val statusText = if(log.type == 2) "DANGER" else "WARNING"

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().alpha(contentAlpha),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AreaBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.fillMaxHeight().width(4.dp).background(statusColor))

            Column(modifier = Modifier.padding(16.dp).weight(1f)) {

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).background(statusColor, CircleShape))

                        Spacer(Modifier.width(8.dp))

                        Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor, letterSpacing = 0.5.sp)
                    }
                    Text(formatTimeAgo(log.timestamp), fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))

                Text(log.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EerieBlack)

                Text(text = log.message, fontSize = 13.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterVertically).padding(end = 12.dp).size(20.dp)
            )
        }
    }
}

