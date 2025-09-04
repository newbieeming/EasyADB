package com.xmbest.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.xmbest.model.DialogState
import com.xmbest.model.DialogType

object InputDialogUtil {

    private val strings = DialogUtil.strings

    @Composable
    private fun showInputDialog(
        dialogState: MutableState<DialogState>,
        title: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        label: String,
        placeholder: String,
        initialValue: String = "",
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        var inputText by remember { mutableStateOf(initialValue) }
        val focusRequester = remember { FocusRequester() }

        DialogUtil.showCustom(
            dialogState = dialogState,
            title = title,
            type = DialogType.CUSTOM,
            icon = icon,
            onConfirm = {
                if (inputText.isNotBlank()) {
                    onConfirm(inputText.trim())
                    inputText = ""
                }
            },
            onCancel = {
                onCancel()
                inputText = ""
            },
            onDismiss = {
                inputText = ""
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(label) },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { focusRequester.requestFocus() }
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (inputText.isNotBlank()) {
                                onConfirm(inputText.trim())
                                inputText = ""
                                DialogUtil.dismiss(dialogState)
                            }
                        }
                    )
                )
            }
        }
    }

    @Composable
    fun showCreateFolderDialog(
        dialogState: MutableState<DialogState>,
        title: String,
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        showInputDialog(
            dialogState = dialogState,
            title = title,
            icon = Icons.Default.CreateNewFolder,
            label = strings.get("dialog.input.folder.label"),
            placeholder = strings.get("dialog.input.folder.placeholder"),
            onConfirm = onConfirm,
            onCancel = onCancel
        )
    }

    @Composable
    fun showCreateFileDialog(
        dialogState: MutableState<DialogState>,
        title: String,
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        showInputDialog(
            dialogState = dialogState,
            title = title,
            icon = Icons.Default.NoteAdd,
            label = strings.get("dialog.input.file.label"),
            placeholder = strings.get("dialog.input.file.placeholder"),
            onConfirm = onConfirm,
            onCancel = onCancel
        )
    }

    @Composable
    fun showRenameDialog(
        dialogState: MutableState<DialogState>,
        title: String,
        currentName: String,
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        showInputDialog(
            dialogState = dialogState,
            title = title,
            icon = Icons.Default.Edit,
            label = strings.get("dialog.input.rename.label"),
            placeholder = strings.get("dialog.input.rename.placeholder"),
            initialValue = currentName,
            onConfirm = onConfirm,
            onCancel = onCancel
        )
    }
}