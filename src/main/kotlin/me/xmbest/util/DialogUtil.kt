package me.xmbest.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector
import me.xmbest.Config
import me.xmbest.locale.PropertiesLocalization
import me.xmbest.model.DialogState
import me.xmbest.model.DialogType

object DialogUtil {

    val strings =
        PropertiesLocalization.create(Config.STRINGS_NAME)
    
    fun showAlert(
        dialogState: MutableState<DialogState>,
        title: String = "",
        message: String,
        confirmText: String = strings.get("button.confirm"),
        onConfirm: (() -> Unit)? = null
    ) {
        dialogState.value = DialogState(
            isVisible = true,
            type = DialogType.INFO,
            title = title,
            message = message,
            confirmText = confirmText,
            onConfirm = onConfirm
        )
    }
    
    fun showInfo(
        dialogState: MutableState<DialogState>,
        title: String = strings.get("dialog.title.tip"),
        message: String,
        confirmText: String = strings.get("button.confirm"),
        onConfirm: (() -> Unit)? = null
    ) {
        dialogState.value = DialogState(
            isVisible = true,
            type = DialogType.INFO,
            title = title,
            message = message,
            confirmText = confirmText,
            onConfirm = onConfirm
        )
    }
    
    fun showWarning(
        dialogState: MutableState<DialogState>,
        title: String = strings.get("dialog.title.warning"),
        message: String,
        confirmText: String = strings.get("button.confirm"),
        cancelText: String = strings.get("button.cancel"),
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        dialogState.value = DialogState(
            isVisible = true,
            type = DialogType.WARNING,
            title = title,
            message = message,
            confirmText = confirmText,
            cancelText = cancelText,
            onConfirm = onConfirm,
            onCancel = onCancel
        )
    }
    
    fun showError(
        dialogState: MutableState<DialogState>,
        title: String = strings.get("dialog.title.error"),
        message: String,
        confirmText: String = strings.get("button.confirm"),
        onConfirm: (() -> Unit)? = null
    ) {
        dialogState.value = DialogState(
            isVisible = true,
            type = DialogType.ERROR,
            title = title,
            message = message,
            confirmText = confirmText,
            onConfirm = onConfirm
        )
    }
    
    fun showSuccess(
        dialogState: MutableState<DialogState>,
        title: String = strings.get("dialog.title.success"),
        message: String,
        confirmText: String = strings.get("button.confirm"),
        onConfirm: (() -> Unit)? = null
    ) {
        dialogState.value = DialogState(
            isVisible = true,
            type = DialogType.SUCCESS,
            title = title,
            message = message,
            confirmText = confirmText,
            onConfirm = onConfirm
        )
    }
    
    fun showConfirm(
        dialogState: MutableState<DialogState>,
        title: String = strings.get("dialog.title.success"),
        message: String,
        confirmText: String = strings.get("button.confirm"),
        cancelText: String = strings.get("button.cancel"),
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        dialogState.value = DialogState(
            isVisible = true,
            type = DialogType.INFO,
            title = title,
            message = message,
            confirmText = confirmText,
            cancelText = cancelText,
            onConfirm = onConfirm,
            onCancel = onCancel
        )
    }
    
    fun showCustom(
        dialogState: MutableState<DialogState>,
        title: String = "",
        type: DialogType = DialogType.CUSTOM,
        icon: ImageVector? = null,
        confirmText: String = strings.get("button.confirm"),
        cancelText: String = strings.get("button.cancel"),
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null,
        customContent: @Composable () -> Unit
    ) {
        dialogState.value = DialogState(
            isVisible = true,
            type = type,
            title = title,
            icon = icon,
            confirmText = confirmText,
            cancelText = cancelText,
            onConfirm = onConfirm,
            onCancel = onCancel,
            onDismiss = onDismiss,
            customContent = customContent
        )
    }
    
    fun dismiss(dialogState: MutableState<DialogState>) {
        dialogState.value = DialogState()
    }
}