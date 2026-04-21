package com.example.doantotnghiep.ui.screen.language

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.ui.theme.ActiveAccent
import com.example.doantotnghiep.ui.theme.GlassBg
import com.example.doantotnghiep.ui.theme.TextDim
import com.example.doantotnghiep.ui.theme.TextWhite
import com.example.doantotnghiep.utils.appBackground

data class LanguageItem(
    val code: String,
    val nativeName: String,
    val englishName: String
)

val languages = listOf(
    LanguageItem("vi", "Tiếng Việt", "Vietnamese"),
    LanguageItem("en", "English", "English"),
    LanguageItem("fr", "Français", "French"),
    LanguageItem("ja", "日本語", "Japanese"),
    LanguageItem("ko", "한국어", "Korean"),
    LanguageItem("de", "Deutsch", "German"),
    LanguageItem("es", "Español", "Spanish"),
    LanguageItem("zh", "中文", "Chinese"),
    LanguageItem("th", "ไทย", "Thai"),
    LanguageItem("ru", "Русский", "Russian")
)

@Preview
@Composable
private fun Tets() {
    LanguageScreen {  }
}

@Composable
fun LanguageScreen(
    currentLanguageCode: String = "vi",
    onLanguageSelected: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguageCode) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header matching app style
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
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Select Language",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Language List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(languages) { language ->
                    LanguageCard(
                        language = language,
                        isSelected = language.code == selectedLanguage,
                        onClick = {
                            selectedLanguage = language.code
                            onLanguageSelected(language.code)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageCard(
    language: LanguageItem,
    isSelected: Boolean,
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
            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.nativeName,
                    color = if (isSelected) ActiveAccent else TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = language.englishName,
                    color = TextDim,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Checkbox indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.background(ActiveAccent.copy(alpha = 0.2f))
                        } else {
                            Modifier.border(1.dp, TextDim.copy(alpha = 0.5f), CircleShape)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = ActiveAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewLanguageScreen() {
    LanguageScreen()
}
