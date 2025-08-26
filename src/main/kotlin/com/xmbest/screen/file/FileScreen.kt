package com.xmbest.screen.file

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xmbest.component.FileContent

@Composable
fun FileScreen() {
    val scrollState = rememberScrollState()
    LazyColumn(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .scrollable(scrollState, Orientation.Vertical)
    ) {
        items(20) {
            FileContent()
        }
    }
}