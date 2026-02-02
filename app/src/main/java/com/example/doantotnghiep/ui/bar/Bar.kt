package com.example.doantotnghiep.ui.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.model.NavigationItem
import com.example.doantotnghiep.ui.theme.HybridBadgeBlue
import com.example.doantotnghiep.ui.theme.StatusDanger
import com.example.doantotnghiep.ui.theme.SurfaceLight
import com.example.doantotnghiep.ui.theme.TextPrimaryLight
import com.example.doantotnghiep.ui.theme.TextSecondaryLight
import com.example.doantotnghiep.ui.theme.TextSelected

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloodGuardTopBar() {
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
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.bar_notification))
                }
                Surface(
                    color = StatusDanger,
                    shape = CircleShape,
                    modifier = Modifier.size(8.dp).align(Alignment.TopEnd).offset(x = (-8).dp, y = 8.dp)
                ) {  }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SurfaceLight)
    )
}


@Composable
fun FloodGuardBottomBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = SurfaceLight,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            NavigationItem(stringResource(R.string.bar_home), Icons.Default.Home, stringResource(R.string.bar_home)),
            NavigationItem(stringResource(R.string.bar_map), Icons.Default.Map, stringResource(R.string.bar_map)),
            NavigationItem(stringResource(R.string.bar_analytic), Icons.Default.Analytics, stringResource(R.string.bar_analytic))
        )

        items.forEach {
            val isSelected = currentRoute == it.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(it.route ?: "") },
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