package me.xmbest.screen.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.xmbest.ddmlib.DeviceOperate
import me.xmbest.ddmlib.Log
import me.xmbest.ddmlib.ProcessInfo
import me.xmbest.theme.CardShape
import java.util.Locale.getDefault

@Composable
fun AppScreen(viewModel: AppViewModel = viewModel()) {
    val lazyListState = rememberLazyListState()
    val uiState = viewModel.uiState.collectAsState().value

    DisposableEffect(UInt) {
        viewModel.onEvent(AppUiEvent.Show)
        onDispose {
            viewModel.onEvent(AppUiEvent.Dispose)
        }
    }

    // 监听parentPath变化，自动滚动到顶部
    LaunchedEffect(uiState.filter, uiState.mode) {
        lazyListState.scrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        HeaderTool(viewModel, uiState)
        if (uiState.mode == AppShowMode.ProcessMode) {
            LazyColumn {
                stickyHeader {
                    ProcessHeader()
                }
                items(uiState.processList) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ProcessItem(it)
                }
            }
        }
    }
}

@Composable
fun ProcessHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).background(MaterialTheme.colors.background)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        DeviceOperate.topColumns.subList(0, DeviceOperate.topColumns.size - 1).forEach { item ->
            Text(text = item.uppercase(getDefault()), modifier = Modifier.weight(1f).align(Alignment.CenterVertically))
        }
        Text(
            DeviceOperate.topColumns.last().uppercase(getDefault()),
            modifier = Modifier.weight(4f).align(Alignment.CenterVertically)
        )
        Row(Modifier.weight(1.5f)) {
            Text("action", modifier = Modifier.align(Alignment.CenterVertically))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProcessItem(process: ProcessInfo, viewModel: AppViewModel = viewModel()) {
    val list = listOf(
        process.pid, process.user, process.cpu, process.time, process.virt, process.res, process.shr, process.mem
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier = Modifier.fillMaxWidth()
            .height(64.dp)
            .clip(CardShape)
            .background(MaterialTheme.colors.surface)
            .hoverable(interactionSource)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        list.forEach { item ->
            SelectionContainer(Modifier.weight(1f)) {
                Text(text = item, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
        SelectionContainer(Modifier.weight(4f)) {
            Text(process.name, modifier = Modifier.align(Alignment.CenterVertically))
        }

        Row(Modifier.weight(1.5f), horizontalArrangement = Arrangement.SpaceBetween) {
            if (isHovered) {
                IconButton(onClick = {
                    viewModel.onEvent(AppUiEvent.Kill(listOf(process.pid)))
                }) {
                    TooltipArea({ Text("kill") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }
                IconButton(onClick = {
                    viewModel.onEvent(AppUiEvent.ForceStop(process.name))
                }) {
                    TooltipArea({ Text("force-stop") }) {
                        Icon(
                            imageVector = Icons.Outlined.Stop,
                            contentDescription = "",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderTool(viewModel: AppViewModel, uiState: AppUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchBarDefaults.InputField(
            query = uiState.filter, onQueryChange = {
                Log.d("", "onQueryChange $it")
                viewModel.onEvent(AppUiEvent.ChangeFilter(it))
            }, onSearch = {
                Log.d("", "onSearch $it")
                viewModel.onEvent(AppUiEvent.ChangeFilter(it))
            }, expanded = false, onExpandedChange = { }, placeholder = { Text("Search") }, leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }, trailingIcon = {
                if (uiState.filter.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.onEvent(AppUiEvent.ChangeFilter("")) },
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colors.surface)
                    ) {
                        Icon(Icons.Default.Cancel, "")
                    }
                }
            }, modifier = Modifier.weight(1f).clip(CircleShape).background(MaterialTheme.colors.surface)
        )

        uiState.buttonList.forEach { button ->
            Spacer(modifier = Modifier.width(16.dp))
            AppIconButton(
                icon = button.icon,
                description = button.description,
                backgroundColor = if (button.isSelected()) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                foregroundColor = if (button.isSelected()) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
                click = button.onClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconButton(
    icon: ImageVector,
    description: String,
    backgroundColor: Color = MaterialTheme.colors.surface,
    foregroundColor: Color = MaterialTheme.colors.onSurface,
    click: () -> Unit,
) {
    TooltipArea({ Text(description) }) {
        IconButton(
            onClick = {
                click()
            }, modifier = Modifier.size(48.dp).clip(CircleShape).background(backgroundColor)
        ) {
            Icon(icon, "", tint = foregroundColor)
        }
    }

}
