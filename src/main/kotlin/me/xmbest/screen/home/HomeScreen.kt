package me.xmbest.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.ddmlib.IDevice
import me.xmbest.LocalDialogState
import me.xmbest.component.Item
import me.xmbest.ddmlib.*
import me.xmbest.theme.CardShape
import me.xmbest.util.DialogUtil


@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
        val uiState = viewModel.uiState.collectAsState().value
        FirstRow(viewModel, uiState)
        Row(modifier = Modifier.fillMaxWidth()) {
            val dialogState = LocalDialogState.current
            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp).clip(CardShape)
                    .background(MaterialTheme.colors.surface).padding(10.dp)
            ) {
                Text(
                    text = viewModel.getString("router.item.commonFeatures"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {

                    // 显示查询出的activity值弹窗
                    uiState.currentActivity?.let { activity ->
                        DialogUtil.showConfirm(
                            dialogState = dialogState,
                            title = viewModel.getString("home.currentActivity.copied"),
                            message = activity
                        )
                        viewModel.onEvent(HomeUiEvent.ClearCurrentActivity)
                    }

                    // 渲染统一的功能项列表
                    uiState.actionList.forEach { actionItem ->
                        Item(
                            icon = actionItem.icon,
                            viewModel.getString(actionItem.titleKey)
                        ) {
                            if (actionItem.needsConfirmation && actionItem.confirmationMessageKey != null) {
                                DialogUtil.showWarning(
                                    dialogState,
                                    message = viewModel.getString(actionItem.confirmationMessageKey),
                                    onCancel = {},
                                    onConfirm = {
                                        viewModel.onEvent(HomeUiEvent.ExecuteAction(actionItem.action))
                                    }
                                )
                            } else {
                                viewModel.onEvent(HomeUiEvent.ExecuteAction(actionItem.action))
                            }
                        }
                    }

                    uiState.keyEventList.forEach {
                        Item(
                            icon = it.second, it.first, click = {
                                viewModel.onEvent(HomeUiEvent.InputKey(it.third))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 设备信息项组件
 * @param label 信息标签
 * @param value 信息值
 */
@Composable
private fun DeviceInfoItem(
    label: String, value: String
) {
    SelectionContainer {
        Text(
            text = "$label: $value", fontSize = 14.sp, color = MaterialTheme.colors.onSurface
        )
    }
}

/**
 * 设备信息卡片组件
 * @param device 设备信息
 * @param wmSize 屏幕尺寸
 * @param ipAddress IP地址
 * @param memory 内存信息
 */
@Composable
private fun DeviceInfoCard(
    viewModel: HomeViewModel,
    device: IDevice?,
    wmSize: String?,
    coreSize: String?,
    ipAddress: String?,
    memory: String?
) {
    Column(
        modifier = Modifier.padding(10.dp).clip(CardShape).widthIn(min = 440.dp).heightIn(min = 224.dp)
            .background(MaterialTheme.colors.surface).padding(10.dp)
    ) {
        Text(
            text = viewModel.getString("device.info"),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (device != null) {
            DeviceInfoItem(
                viewModel.getString("device.serialNumber"),
                (device.serialNumber ?: viewModel.getString("device.unknown")).plus(if (device.isRoot) "root" else "")
            )
            DeviceInfoItem(viewModel.getString("device.name"), device.name ?: viewModel.getString("device.unknown"))
            DeviceInfoItem(
                viewModel.getString("device.buildInfo"),
                "${device.buildDate ?: viewModel.getString("device.unknown")} (${
                    device.buildType ?: viewModel.getString("device.unknown")
                })"
            )
            DeviceInfoItem(
                viewModel.getString("device.cpuInfo"),
                "${device.socModel}(${device.abis.joinToString(",")}|$coreSize ${viewModel.getString("device.core")})"
            )
            DeviceInfoItem(
                viewModel.getString("device.screenInfo"),
                "${wmSize ?: viewModel.getString("device.unknown")} dpi: ${device.density}"
            )
            DeviceInfoItem(
                viewModel.getString("device.ipAddress"),
                ipAddress?.removePrefix("addr:") ?: viewModel.getString("device.unknown")
            )
            DeviceInfoItem(
                viewModel.getString("device.memoryInfo"), memory?.plus(" MB") ?: viewModel.getString("device.unknown")
            )
        } else {
            Text(
                text = viewModel.getString("device.notConnected"),
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 设备版本显示
 * @param device 设备信息
 */
@Composable
private fun DeviceVersionDisplay(
    viewModel: HomeViewModel,
    device: IDevice?,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(196.dp, 156.dp).clip(CircleShape)
                .background(MaterialTheme.colors.surface).padding(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = viewModel.getString("device.icon"),
                tint = MaterialTheme.colors.onSurface,
                modifier = Modifier.padding(top = 56.dp, start = 12.dp).size(64.dp).rotate(45F)
            )

            Text(
                text = device?.androidVersion ?: "?",
                fontSize = 52.sp,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, start = 96.dp).rotate(45F)
            )

            Text(
                text = device?.productBrand ?: "",
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Text(
                text = device?.productName ?: "",
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterEnd).rotate(90F)
            )

            Text(
                text = viewModel.getString("device.apiLevel").plus(device?.version?.apiLevel ?: "?"),
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.Center).rotate(45F)
            )

            Text(
                text = device?.batteryLevel?.toString()?.plus("%") ?: "",
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.BottomCenter).rotate(45F)
            )

            if (device?.version?.isPreview == true) {
                Text(
                    text = viewModel.getString("device.isPreview"),
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 32.dp, start = 12.dp).rotate(45F)
                )
            }
        }
    }
}

/**
 * 方向控制
 * @param onEvent 事件处理函数
 */
@Composable
private fun ControlPanel(
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.align(Alignment.Center)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.background)
            ) {
                IconButton(
                    onClick = { onEvent(HomeUiEvent.InputKey(66)) },
                    modifier = Modifier.size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.surface)
                        .align(Alignment.Center)

                ) {
                    Icon(Icons.Default.Check, "")
                }
            }

            IconButton(
                onClick = { onEvent(HomeUiEvent.InputKey(21)) },
                modifier = Modifier.size(24.dp)
                    .align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "")
            }

            IconButton(
                onClick = { onEvent(HomeUiEvent.InputKey(19)) },
                modifier = Modifier.size(24.dp)
                    .align(Alignment.TopCenter)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, "")
            }

            IconButton(
                onClick = { onEvent(HomeUiEvent.InputKey(22)) },
                modifier = Modifier.size(24.dp)
                    .align(Alignment.CenterEnd)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "")
            }

            IconButton(
                onClick = { onEvent(HomeUiEvent.InputKey(20)) },
                modifier = Modifier.size(24.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, "")
            }
        }
    }
}

@Composable
fun FirstRow(viewModel: HomeViewModel, uiState: HomeUiState) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // 设备信息卡片
        DeviceInfoCard(
            viewModel = viewModel,
            device = uiState.device,
            wmSize = uiState.wmSize,
            ipAddress = uiState.ipAddress,
            coreSize = uiState.cpuCoreSize,
            memory = uiState.memory
        )

        // 设备版本显示
        DeviceVersionDisplay(
            viewModel = viewModel,
            device = uiState.device,
            modifier = Modifier
                .padding(end = 16.dp)
                .rotate(-45F)
                .align(Alignment.CenterVertically)
        )

        // 方向控制
        ControlPanel(
            onEvent = viewModel::onEvent,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colors.surface).padding(10.dp)
                .align(Alignment.CenterVertically)
        )
    }
}
