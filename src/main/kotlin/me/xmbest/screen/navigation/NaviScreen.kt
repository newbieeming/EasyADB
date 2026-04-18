@file:Suppress("DEPRECATION")

package me.xmbest.screen.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import me.xmbest.LocalDialogState
import me.xmbest.LocalSnackbarHostState
import me.xmbest.component.GlobalDialog
import me.xmbest.model.DialogState
import me.xmbest.util.GlobalDragHandler


@Composable
fun NaviScreen(viewModel: NaviViewModule = viewModel()) {
    val uiState = viewModel.uiState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val dialogState = remember { mutableStateOf(DialogState()) }

    val dragTarget = remember(dialogState, uiState.currentRoute) {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                return GlobalDragHandler.handleDrop(
                    event = event,
                    dialogState = dialogState,
                    onInstall = { apkPath ->
                        viewModel.onEvent(
                            NaviUiEvent.DeviceManagement.Install(
                                apkPath
                            )
                        )
                    }
                )
            }
        }
    }

    CompositionLocalProvider(
        LocalSnackbarHostState provides snackbarHostState,
        LocalDialogState provides dialogState
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) {
            Row(
                modifier = Modifier.fillMaxSize().dragAndDropTarget(
                    shouldStartDragAndDrop = GlobalDragHandler::shouldStartDragAndDrop,
                    target = dragTarget
                )
            ) {
                Left(modifier = Modifier.fillMaxHeight().width(240.dp), uiState)
                Right(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    currentRoute = uiState.currentRoute
                )
            }
        }

        GlobalDialog()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Left(
    modifier: Modifier = Modifier, uiState: NaviUiState, viewModel: NaviViewModule = viewModel()
) {
    Column(
        modifier.background(MaterialTheme.colors.background).padding(start = 12.dp, end = 12.dp)
    ) {
        viewModel.destinations.forEach { item ->
            val selected = item.route == uiState.currentRoute
            Spacer(modifier = Modifier.height(8.dp))
            ListItem(
                modifier = Modifier.height(44.dp).clip(RoundedCornerShape(8.dp)).background(
                    if (selected) MaterialTheme.colors.primary
                    else MaterialTheme.colors.background
                ).clickable {
                    viewModel.onEvent(NaviUiEvent.Navigation.SelectDestination(item.route))
                }, icon = {
                    Icon(
                        item.icon, item.icon.name, tint = optionColor(selected)
                    )
                }) {
                Text(
                    text = item.name, color = optionColor(selected)
                )
            }
        }
        Row(
            modifier = Modifier.weight(1f).padding(bottom = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            ListItem(modifier = Modifier.height(44.dp).clip(RoundedCornerShape(8.dp)).clickable {
                viewModel.onEvent(NaviUiEvent.DeviceManagement.ShowDeviceList(true))
            }, icon = {
                Icon(
                    Icons.Default.Phonelink,
                    contentDescription = "refresh devices",
                    tint = MaterialTheme.colors.onBackground,
                    modifier = Modifier.clickable {
                        viewModel.onEvent(NaviUiEvent.DeviceManagement.RefreshDevice)
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
                viewModel.onEvent(NaviUiEvent.DeviceManagement.ShowDeviceList(!uiState.devicesListShow))
            }, modifier = Modifier.width(216.dp)
        ) {
            if (uiState.devices.isEmpty()) {
                DropdownMenuItem(onClick = {}) {
                    Text(text = viewModel.getString("device.empty"))
                }
            } else {
                uiState.devices.forEach {
                    DropdownMenuItem(onClick = {
                        viewModel.onEvent(NaviUiEvent.DeviceManagement.SelectDevice(it))
                    }) {
                        Text(text = it.serialNumber)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Right(modifier: Modifier, currentRoute: NaviRoute, viewModel: NaviViewModule = viewModel()) {
    val backStack = remember { mutableStateListOf(currentRoute) }
    val destinationMap = remember(viewModel.destinations) {
        viewModel.destinations.associateBy { it.route }
    }

    LaunchedEffect(currentRoute) {
        when {
           backStack.isEmpty() -> backStack.add(currentRoute)
           backStack.last() != currentRoute -> backStack[backStack.lastIndex] = currentRoute
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = {},
        modifier = modifier.background(color = MaterialTheme.colors.secondary),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator()
        ),
        entryProvider = { route ->
            val destination = destinationMap[route]
            if (destination == null) {
                NavEntry(route) {
                    Text(text = "Unknown route")
                }
            } else {
                NavEntry(route) {
                    destination.content()
                }
            }
        }
    )
}

@Composable
fun optionColor(value: Boolean) = if (value) MaterialTheme.colors.onPrimary
else MaterialTheme.colors.onBackground
