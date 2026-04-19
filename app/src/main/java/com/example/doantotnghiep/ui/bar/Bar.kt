package com.example.doantotnghiep.ui.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.NavigationItem
import com.example.doantotnghiep.data.local.enum.ScreenRoute
import com.example.doantotnghiep.ui.dialog.NotificationDiaLog
import com.example.doantotnghiep.ui.theme.SoftBgTop
import com.example.doantotnghiep.ui.theme.StatusDanger
import com.example.doantotnghiep.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloodGuardTopBar(viewmodel: HomeViewModel = hiltViewModel()) {
    val unreadCount by viewmodel.unreadCount.collectAsState()
    val notifications by viewmodel.notification.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.background(SoftBgTop)) {
        Spacer(modifier = Modifier.height(24.dp))
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_water_drop),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = stringResource(R.string.app_name).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "HỆ THỐNG CẢNH BÁO LŨ",
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.bar_menu), tint = Color.White)
                }
            },
            actions = {
                Box(Modifier.padding(end = 8.dp)) {
                    IconButton(onClick = {showDialog = true}) {
                        Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.bar_notification), tint = Color.White)
                    }
                    if(unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = 12.dp)
                            .background(StatusDanger, CircleShape)
                            .border(1.5.dp, SoftBgTop, CircleShape)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIconContentColor = Color.White,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }

    if(showDialog) {
        NotificationDiaLog(
            logs = notifications,
            onDismiss = {showDialog = false},
            onItemClick = { log ->
                viewmodel.markAsRead(log)
            },
            onMarkAllRead = {
                viewmodel.markAllAsRead()
            }
        )
    }
}

@Composable
fun FloodGuardBottomBar(
    currentRoute: ScreenRoute,
    onNavigate: (ScreenRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem(stringResource(R.string.bar_home), Icons.Rounded.Home, ScreenRoute.HOME),
        NavigationItem(stringResource(R.string.bar_map), Icons.Rounded.Map, ScreenRoute.MAP),
        NavigationItem(stringResource(R.string.bar_analytic), Icons.Rounded.Analytics, ScreenRoute.ANALYTIC),
        NavigationItem(stringResource(R.string.bar_history), Icons.Rounded.History, ScreenRoute.HISTORY)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                RoundedCornerShape(32.dp)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                val animatedBgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF0EA5E9) else Color.Transparent,
                    animationSpec = tween(300)
                )
                val animatedContentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color(0xFF94A3B8),
                    animationSpec = tween(300)
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(animatedBgColor)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onNavigate(item.route ?: ScreenRoute.HOME) }
                        .padding(horizontal = if(isSelected) 14.dp else 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon ?: Icons.Default.Refresh,
                            contentDescription = item.title,
                            tint = animatedContentColor,
                            modifier = Modifier.size(24.dp)
                        )

                        AnimatedVisibility(visible = isSelected) {
                            Text(
                                text = item.title ?: "",
                                color = animatedContentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}