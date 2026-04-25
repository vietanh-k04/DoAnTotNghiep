package com.example.doantotnghiep.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.doantotnghiep.R
import com.example.doantotnghiep.ui.theme.StatusDanger
import com.example.doantotnghiep.ui.theme.TextDim
import com.example.doantotnghiep.ui.theme.TextWhite
import com.example.doantotnghiep.ui.theme.VividBlue

@Composable
fun CalibrationErrorDialog(
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    Dialog(
        onDismissRequest = {  },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF242424),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Rounded.Error, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.calib_error_title), color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.calib_error_desc), color = TextDim, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(stringResource(R.string.calib_btn_cancel))
                    }
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = VividBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(stringResource(R.string.calib_btn_retry), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}