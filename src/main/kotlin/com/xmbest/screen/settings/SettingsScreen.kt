package com.xmbest.screen.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xmbest.component.TitleContent

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {

    val uiState = viewModel.uiState.collectAsState().value

    TitleContent(
        title = viewModel.getString("theme.setting"),
        modifier = Modifier.fillMaxWidth().padding(start = 6.dp, top = 6.dp)
    ) {
        SingleChoiceSegmentedButtonRow {
            viewModel.themeList.forEachIndexed { index, item ->
                SegmentedButton(
                    selected = item.second == uiState.theme,
                    onClick = { viewModel.onEvent(SettingsUiEvent.UpdateTheme(item.second)) },
                    label = {
                        Text(
                            item.first,
                            color = if (item.second == uiState.theme)
                                MaterialTheme.colors.onPrimary
                            else
                                MaterialTheme.colors.onSurface
                        )
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = viewModel.themeList.size
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colors.primary,
                        activeContentColor = MaterialTheme.colors.onPrimary,
                        inactiveContainerColor = MaterialTheme.colors.surface,
                        inactiveContentColor = MaterialTheme.colors.onSurface
                    )
                )
            }
        }
    }

    TitleContent(
        title = viewModel.getString("adb.config"),
        modifier = Modifier.fillMaxWidth().padding(start = 6.dp, top = 6.dp)
    ) {
        SingleChoiceSegmentedButtonRow {
            viewModel.envList.forEachIndexed { index, item ->
                SegmentedButton(
                    selected = item.second.path == uiState.adbPAth,
                    onClick = { viewModel.onEvent(SettingsUiEvent.UpdateAdbEnv(item.second)) },
                    label = {
                        Text(
                            item.first,
                            color = if (item.second.path == uiState.adbPAth)
                                MaterialTheme.colors.onPrimary
                            else
                                MaterialTheme.colors.onSurface
                        )
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = viewModel.themeList.size
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colors.primary,
                        activeContentColor = MaterialTheme.colors.onPrimary,
                        inactiveContainerColor = MaterialTheme.colors.surface,
                        inactiveContentColor = MaterialTheme.colors.onSurface,
                    )
                )
            }
        }
    }
}

