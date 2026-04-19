package com.example.doantotnghiep.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.R
import com.example.doantotnghiep.ui.theme.ErrorBorder
import com.example.doantotnghiep.ui.theme.ErrorContainer
import com.example.doantotnghiep.ui.theme.ErrorIcon
import com.example.doantotnghiep.ui.theme.ErrorIconContainer
import com.example.doantotnghiep.ui.theme.ErrorMessage
import com.example.doantotnghiep.ui.theme.ErrorTitle

@Composable
fun AlertPopups(
    showRecalibrate: Boolean,
    showObstruction: Boolean,
    onDismissRecalibrate: () -> Unit,
    onDismissObstruction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = showRecalibrate,
            enter = slideInVertically(initialOffsetY = { -it - 100 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it - 100 }) + fadeOut(),
        ) {
            TopToast(
                title = stringResource(R.string.RECALIBRATE_TITLE),
                message = stringResource(R.string.RECALIBRATE_DESC),
                iconRes = R.drawable.ic_water_drop,
                onClick = onDismissRecalibrate
            )
        }

        AnimatedVisibility(
            visible = showObstruction,
            enter = slideInVertically(initialOffsetY = { -it - 100 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it - 100 }) + fadeOut(),
        ) {
            TopToast(
                title = stringResource(R.string.OBSTRUCTION_TITLE),
                message = stringResource(R.string.OBSTRUCTION_DESC),
                iconRes = R.drawable.ic_water_drop,
                containerColor = ErrorContainer,
                borderColor = ErrorBorder,
                iconContainerColor = ErrorIconContainer,
                iconColor = ErrorIcon,
                titleColor = ErrorTitle,
                messageColor = ErrorMessage,
                onClick = onDismissObstruction
            )
        }
    }
}

@Composable
fun TopToast(
    title: String,
    message: String,
    iconRes: Int,
    containerColor: Color = Color(0xFFFFF8E1),
    borderColor: Color = Color(0xFFFFA000),
    iconContainerColor: Color = Color(0xFFFFECB3),
    iconColor: Color = Color(0xFFF57C00),
    titleColor: Color = Color(0xFFE65100),
    messageColor: Color = Color(0xFFEF6C00),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconContainerColor, RoundedCornerShape(12.dp))
                    .border(1.dp, borderColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    color = messageColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}