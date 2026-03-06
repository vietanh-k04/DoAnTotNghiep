package com.example.doantotnghiep.ui.bar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.NavigationItem
import com.example.doantotnghiep.data.local.ScreenRoute
import com.example.doantotnghiep.ui.dashboard.NotificationDiaLog
import com.example.doantotnghiep.ui.theme.HybridBadgeBlue
import com.example.doantotnghiep.ui.theme.StatusDanger
import com.example.doantotnghiep.ui.theme.SurfaceLight
import com.example.doantotnghiep.ui.theme.TextPrimaryLight
import com.example.doantotnghiep.ui.theme.TextSecondaryLight
import com.example.doantotnghiep.ui.theme.TextSelected
import com.example.doantotnghiep.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloodGuardTopBar(viewmodel: HomeViewModel = hiltViewModel()) {
    val unreadCount by viewmodel.unreadCount.collectAsState()
    val notifications by viewmodel.notification.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_water_drop),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    color = TextPrimaryLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.bar_menu))
            }

        },
        actions = {
            Box(Modifier.padding(end = 8.dp)) {
                IconButton(onClick = {showDialog = true}) {
                    Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.bar_notification))
                }
                if(unreadCount > 0) {
                    Surface(
                        color = StatusDanger,
                        shape = CircleShape,
                        modifier = Modifier.size(8.dp).align(Alignment.TopEnd).offset(x = (-8).dp, y = 8.dp).border(1.5.dp, SurfaceLight, CircleShape)
                    ) { }
                }

            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceLight,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = Color.Unspecified,
            actionIconContentColor = Color.Unspecified
        )
    )

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
fun FloodGuardBottomBar(currentRoute: ScreenRoute, onNavigate: (ScreenRoute) -> Unit) {
    NavigationBar(
        containerColor = SurfaceLight,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            NavigationItem(stringResource(R.string.bar_home), Icons.Default.Home, ScreenRoute.HOME),
            NavigationItem(stringResource(R.string.bar_map), Icons.Default.Map, ScreenRoute.MAP),
            //NavigationItem(stringResource(R.string.bar_analytic), Icons.Default.Analytics, ScreenRoute.)
        )

        items.forEach {
            val isSelected = currentRoute == it.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(it.route ?: ScreenRoute.HOME) },
                label = { Text(it.title ?: "", fontSize = 10.sp)},
                icon = {
                    Icon(
                        imageVector = it.icon ?: Icons.Default.Refresh,
                        contentDescription = it.title,
                        tint = if (isSelected) TextSelected else TextSecondaryLight
                    )
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = HybridBadgeBlue)
            )
        }
    }
}

