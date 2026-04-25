package com.example.doantotnghiep.ui.screen.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.doantotnghiep.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.data.local.GuideStepData
import com.example.doantotnghiep.ui.theme.ActiveAccent
import com.example.doantotnghiep.ui.theme.TextDim
import com.example.doantotnghiep.ui.theme.TextWhite
import com.example.doantotnghiep.utils.appBackground

@Preview
@Composable
private fun Test() {
    GuideScreen {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackground()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.guide_title),
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = TextWhite
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            val guideSteps = getGuideSteps()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp, start = 20.dp, end = 20.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.guide_header),
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 32.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = stringResource(R.string.guide_desc),
                        color = TextDim,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }

                itemsIndexed(guideSteps) { index, step ->
                    TimelineItem(
                        stepNumber = index + 1,
                        isLast = index == guideSteps.size - 1,
                        step = step
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    stepNumber: Int,
    isLast: Boolean,
    step: GuideStepData
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (!isLast) {
                    val strokeWidth = 2.dp.toPx()
                    val xOffset = 16.dp.toPx()
                    drawLine(
                        color = ActiveAccent.copy(alpha = 0.3f),
                        start = Offset(xOffset, 32.dp.toPx() + 8.dp.toPx()),
                        end = Offset(xOffset, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            }
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(ActiveAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = step.title,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = step.description,
                color = TextDim,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = if (step.icon != null) 16.dp else 0.dp)
            )

            // Image Placeholder Frame
            if (step.icon != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = "Hình minh họa",
                                tint = step.iconTint,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = stringResource(R.string.guide_image_placeholder),
                            color = TextDim.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getGuideSteps(): List<GuideStepData> {
    return listOf(
        GuideStepData(
            title = stringResource(R.string.guide_step1_title),
            description = stringResource(R.string.guide_step1_desc),
            icon = Icons.Rounded.Power,
            iconTint = Color(0xFFF59E0B)
        ),
        GuideStepData(
            title = stringResource(R.string.guide_step2_title),
            description = stringResource(R.string.guide_step2_desc),
            icon = null
        ),
        GuideStepData(
            title = stringResource(R.string.guide_step3_title),
            description = stringResource(R.string.guide_step3_desc),
            icon = Icons.Rounded.Sensors,
            iconTint = Color(0xFF0EA5E9)
        ),
        GuideStepData(
            title = stringResource(R.string.guide_step4_title),
            description = stringResource(R.string.guide_step4_desc),
            icon = null
        ),
        GuideStepData(
            title = stringResource(R.string.guide_step5_title),
            description = stringResource(R.string.guide_step5_desc),
            icon = Icons.Rounded.Analytics,
            iconTint = Color(0xFF10B981)
        )
    )
}
