@file:Suppress("DEPRECATION")

package com.xmbest.screen.router

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RouterScreen(viewModel: RouterViewModule = viewModel()) {
    val uiState = viewModel.uiState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Left(modifier = Modifier.fillMaxHeight().width(240.dp), uiState)
            Right(modifier = Modifier.fillMaxHeight().weight(1f), uiState)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Left(
    modifier: Modifier = Modifier, uiState: RouterUiState, viewModel: RouterViewModule = viewModel()
) {
    Column(
        modifier.background(MaterialTheme.colors.background).padding(start = 12.dp, end = 12.dp)
    ) {
        viewModel.pageList.forEachIndexed { index, item ->
            Spacer(modifier = Modifier.height(8.dp))
            ListItem(
                modifier = Modifier.height(44.dp).clip(RoundedCornerShape(8.dp)).background(
                    if (index == uiState.index) MaterialTheme.colors.primary
                    else MaterialTheme.colors.background
                ).clickable {
                    viewModel.onEvent(RouterUiEvent.SelectLeftItem(index))
                }, icon = {
                    Icon(
                        item.icon, item.icon.name, tint = optionColor(index == uiState.index)
                    )
                }) {
                Text(
                    text = item.name, color = optionColor(index == uiState.index)
                )
            }
        }
        Row(
            modifier = Modifier.weight(1f).padding(bottom = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.Bottom
        ) {
            ListItem(modifier = Modifier.height(44.dp).clip(RoundedCornerShape(8.dp)).clickable {
                viewModel.onEvent(RouterUiEvent.ShowDeviceList(true))
            }, icon = {
                Icon(
                    Icons.Default.Phonelink,
                    contentDescription = "refresh devices",
                    tint = MaterialTheme.colors.onBackground,
                    modifier = Modifier.clickable {
                        viewModel.onEvent(RouterUiEvent.RefreshDevice)
                    })
            }) {
                Text(
                    uiState.device?.serialNumber ?: viewModel.getString("device.select"),
                    maxLines = 2,
                    color = MaterialTheme.colors.onBackground,
                )
            }
        }

        DropdownMenu(
            expanded = uiState.devicesListShow, onDismissRequest = {
                viewModel.onEvent(RouterUiEvent.ShowDeviceList(!uiState.devicesListShow))
            }, modifier = Modifier.width(216.dp)
        ) {
            if (uiState.devices.isEmpty()) {
                DropdownMenuItem(onClick = {}) {
                    Text(text = viewModel.getString("device.empty"))
                }
            } else {
                uiState.devices.forEach {
                    DropdownMenuItem(onClick = {
                        viewModel.onEvent(RouterUiEvent.SelectDevice(it))
                    }) {
                        Text(text = it.serialNumber)
                    }
                }
            }
        }
    }
}

@Composable
fun Right(modifier: Modifier, uiState: RouterUiState, viewModel: RouterViewModule = viewModel()) {
    Column(modifier.background(color = MaterialTheme.colors.secondary)) {
        viewModel.pageList[uiState.index].comp()
    }
}

@Composable
fun optionColor(value: Boolean) = if (value) MaterialTheme.colors.onPrimary
else MaterialTheme.colors.onBackground