package com.xmbest.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xmbest.LocalDialogState
import com.xmbest.model.DialogState
import com.xmbest.model.DialogType

@Composable
fun GlobalDialog() {
    val dialogState = LocalDialogState.current
    val state = dialogState.value

    if (state.isVisible) {
        Dialog(
            onDismissRequest = {
                state.onDismiss?.invoke()
                dialogState.value = DialogState()
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 图标和标题区域
                    if (state.title.isNotEmpty() || state.icon != null || state.type != DialogType.CUSTOM) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // 显示图标
                            val iconToShow = state.icon ?: when (state.type) {
                                DialogType.INFO -> Icons.Default.Info
                                DialogType.WARNING -> Icons.Default.Warning
                                DialogType.ERROR -> Icons.Default.Error
                                DialogType.SUCCESS -> Icons.Default.CheckCircle
                                DialogType.CUSTOM -> null
                            }

                            val iconColor = when (state.type) {
                                DialogType.INFO -> MaterialTheme.colors.primary
                                DialogType.WARNING -> Color(0xFFFF9800)
                                DialogType.ERROR -> MaterialTheme.colors.error
                                DialogType.SUCCESS -> Color(0xFF4CAF50)
                                DialogType.CUSTOM -> MaterialTheme.colors.primary
                            }

                            iconToShow?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            // 标题
                            if (state.title.isNotEmpty()) {
                                Text(
                                    text = state.title,
                                    style = MaterialTheme.typography.h6.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp
                                    ),
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 内容区域
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.customContent != null) {
                            state.customContent.invoke()
                        } else {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.body1.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.87f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 按钮区域
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 取消按钮
                        if (state.onCancel != null) {
                            Button(
                                onClick = {
                                    state.onCancel.invoke()
                                    dialogState.value = DialogState()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    backgroundColor = MaterialTheme.colors.error
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 4.dp
                                )
                            ) {
                                Text(
                                    text = state.cancelText,
                                    style = MaterialTheme.typography.button.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // 确认按钮
                        Button(
                            onClick = {
                                state.onConfirm?.invoke()
                                dialogState.value = DialogState()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = MaterialTheme.colors.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = state.confirmText,
                                style = MaterialTheme.typography.button.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}