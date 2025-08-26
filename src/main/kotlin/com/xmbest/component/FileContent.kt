package com.xmbest.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmbest.theme.CardShape
import com.xmbest.theme.ChipShape
import com.xmbest.theme.TextFieldShape
import com.xmbest.theme.purple

@Composable
fun FileContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 6.dp)
            .clip(CardShape)
            .background(MaterialTheme.colors.surface)
            .padding(vertical = 12.dp, horizontal = 24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier
                .clip(ChipShape)
                .background(MaterialTheme.colors.primary)
                .padding(8.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = "sdcard",
                    color = MaterialTheme.colors.onSurface,
                    style = TextStyle.Default.copy(fontSize = 18.sp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "目录",
                    color = MaterialTheme.colors.primary,
                    style = TextStyle.Default.copy(fontSize = 14.sp),
                    modifier = Modifier
                        .clip(TextFieldShape)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.3f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "drwxr-xr-x",
                    color = purple,
                    style = TextStyle.Default.copy(fontSize = 14.sp),
                    modifier = Modifier
                        .clip(TextFieldShape)
                        .background(purple.copy(alpha = 0.3f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "2019-01-01 17:35",
                    color = MaterialTheme.colors.onBackground,
                    style = TextStyle.Default.copy(fontSize = 14.sp)
                )
            }
        }
    }
}