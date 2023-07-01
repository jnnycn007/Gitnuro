package com.jetpackduba.gitnuro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetpackduba.gitnuro.AppIcons
import com.jetpackduba.gitnuro.ui.components.AdjustableOutlinedTextField
import com.jetpackduba.gitnuro.ui.components.PrimaryButton
import com.jetpackduba.gitnuro.ui.components.ScrollableLazyColumn
import com.jetpackduba.gitnuro.ui.components.gitnuroDynamicViewModel
import com.jetpackduba.gitnuro.viewmodels.RebaseAction
import com.jetpackduba.gitnuro.viewmodels.RebaseInteractiveViewState
import com.jetpackduba.gitnuro.viewmodels.RebaseInteractiveViewModel
import com.jetpackduba.gitnuro.viewmodels.RebaseLine

@Composable
fun RebaseInteractive(
    rebaseInteractiveViewModel: RebaseInteractiveViewModel = gitnuroDynamicViewModel(),
) {
    val rebaseState = rebaseInteractiveViewModel.rebaseState.collectAsState()
    val rebaseStateValue = rebaseState.value

    Box(
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .fillMaxSize(),
    ) {
        when (rebaseStateValue) {
            is RebaseInteractiveViewState.Failed -> {}
            is RebaseInteractiveViewState.Loaded -> {
                RebaseStateLoaded(
                    rebaseInteractiveViewModel,
                    rebaseStateValue,
                    onCancel = {
                        rebaseInteractiveViewModel.cancel()
                    },
                )
            }

            RebaseInteractiveViewState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun RebaseStateLoaded(
    rebaseInteractiveViewModel: RebaseInteractiveViewModel,
    rebaseState: RebaseInteractiveViewState.Loaded,
    onCancel: () -> Unit,
) {
    val stepsList = rebaseState.stepsList

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Rebase interactive",
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            fontSize = 20.sp,
        )

        ScrollableLazyColumn(modifier = Modifier.weight(1f)) {
            items(stepsList) { rebaseTodoLine ->
                RebaseCommit(
                    rebaseLine = rebaseTodoLine,
                    message = rebaseState.messages[rebaseTodoLine.commit.name()],
                    isFirst = stepsList.first() == rebaseTodoLine,
                    onActionChanged = { newAction ->
                        rebaseInteractiveViewModel.onCommitActionChanged(rebaseTodoLine.commit, newAction)
                    },
                    onMessageChanged = { newMessage ->
                        rebaseInteractiveViewModel.onCommitMessageChanged(rebaseTodoLine.commit, newMessage)
                    },
                )
            }
        }

        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            PrimaryButton(
                text = "Cancel",
                modifier = Modifier.padding(end = 8.dp),
                onClick = onCancel,
                backgroundColor = Color.Transparent,
                textColor = MaterialTheme.colors.onBackground,
            )
            PrimaryButton(
                modifier = Modifier.padding(end = 16.dp),
                enabled = stepsList.any { it.rebaseAction != RebaseAction.PICK },
                onClick = {
                    rebaseInteractiveViewModel.continueRebaseInteractive()
                },
                text = "Complete rebase"
            )
        }
    }
}

@Composable
fun RebaseCommit(
    rebaseLine: RebaseLine,
    isFirst: Boolean,
    message: String?,
    onActionChanged: (RebaseAction) -> Unit,
    onMessageChanged: (String) -> Unit,
) {
    val action = rebaseLine.rebaseAction
    var newMessage by remember(rebaseLine.commit.name(), action) {
        if (action == RebaseAction.REWORD) {
            mutableStateOf(message ?: rebaseLine.shortMessage) /* if reword, use the value from the map (if possible)*/
        } else
            mutableStateOf(rebaseLine.shortMessage) // If it's not reword, use the original shortMessage
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
    ) {
        ActionDropdown(
            action,
            isFirst = isFirst,
            onActionChanged = onActionChanged,
        )

        AdjustableOutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp),
            enabled = action == RebaseAction.REWORD,
            value = newMessage,
            onValueChange = {
                newMessage = it
                onMessageChanged(it)
            },
            textStyle = MaterialTheme.typography.body2,
            backgroundColor = if (action == RebaseAction.REWORD) {
                MaterialTheme.colors.background
            } else
                MaterialTheme.colors.surface
        )

    }
}


@Composable
fun ActionDropdown(
    action: RebaseAction,
    isFirst: Boolean,
    onActionChanged: (RebaseAction) -> Unit,
) {
    var showDropDownMenu by remember { mutableStateOf(false) }
    Box {
        TextButton(
            onClick = { showDropDownMenu = true },
            modifier = Modifier
                .width(120.dp)
                .height(40.dp)
                .padding(end = 8.dp),
        ) {
            Text(
                action.displayName,
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painterResource(AppIcons.EXPAND_MORE),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colors.onBackground,
            )
        }

        DropdownMenu(
            expanded = showDropDownMenu,
            onDismissRequest = { showDropDownMenu = false },
        ) {
            val dropDownItems = if (isFirst) {
                firstItemActions
            } else {
                actions
            }

            for (dropDownOption in dropDownItems) {
                DropdownMenuItem(
                    onClick = {
                        showDropDownMenu = false
                        onActionChanged(dropDownOption)
                    }
                ) {
                    Text(
                        text = dropDownOption.displayName,
                        style = MaterialTheme.typography.body1,
                    )
                }
            }
        }
    }
}

val firstItemActions = listOf(
    RebaseAction.PICK,
    RebaseAction.REWORD,
    RebaseAction.DROP,
)

val actions = listOf(
    RebaseAction.PICK,
    RebaseAction.REWORD,
    RebaseAction.SQUASH,
    RebaseAction.FIXUP,
    RebaseAction.EDIT,
    RebaseAction.DROP,
)