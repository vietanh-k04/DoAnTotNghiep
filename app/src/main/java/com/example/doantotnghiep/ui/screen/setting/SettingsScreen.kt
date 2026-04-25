package com.example.doantotnghiep.ui.screen.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.doantotnghiep.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.ui.theme.ActiveAccent
import com.example.doantotnghiep.ui.theme.GlassBg
import com.example.doantotnghiep.ui.theme.TextDim
import com.example.doantotnghiep.ui.theme.TextWhite
import com.example.doantotnghiep.utils.appBackground

@Preview
@Composable
private fun Test() {
    SettingsScreen(
        onBackClick = {},
        onLanguageClick = {},
        onGuideClick = {},
        onTelegramClick = {}
    )
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onGuideClick: () -> Unit,
    onTelegramClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = stringResource(R.string.settings_title),
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 40.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingCard(
                    icon = Icons.Rounded.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = stringResource(R.string.settings_language_desc),
                    onClick = onLanguageClick
                )

                SettingCard(
                    icon = Icons.Rounded.Sensors,
                    title = stringResource(R.string.settings_guide),
                    subtitle = stringResource(R.string.settings_guide_desc),
                    onClick = onGuideClick
                )

                SettingCard(
                    icon = Icons.Rounded.ChatBubbleOutline,
                    title = stringResource(R.string.settings_telegram),
                    subtitle = stringResource(R.string.settings_telegram_desc),
                    trailingIcon = Icons.Rounded.OpenInNew,
                    onClick = onTelegramClick
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer version text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_app_version),
                    color = TextDim,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_app_author),
                    color = TextDim.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingIcon: ImageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassBg)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ActiveAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextDim,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = TextDim,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
