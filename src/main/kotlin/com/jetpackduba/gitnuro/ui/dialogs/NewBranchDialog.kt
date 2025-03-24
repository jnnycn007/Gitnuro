package com.jetpackduba.gitnuro.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jetpackduba.gitnuro.generated.resources.Res
import com.jetpackduba.gitnuro.generated.resources.branch
import com.jetpackduba.gitnuro.keybindings.KeybindingOption
import com.jetpackduba.gitnuro.keybindings.matchesBinding
import com.jetpackduba.gitnuro.ui.components.AdjustableOutlinedTextField
import com.jetpackduba.gitnuro.ui.components.PrimaryButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun NewBranchDialog(
    onClose: () -> Unit,
    onAccept: (branchName: String) -> Unit,
) {
    var branchField by remember { mutableStateOf("") }
    val branchFieldFocusRequester = remember { FocusRequester() }
    val buttonFieldFocusRequester = remember { FocusRequester() }

    MaterialDialog(onCloseRequested = onClose) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painterResource(Res.drawable.branch),
                contentDescription = null,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(64.dp),
                tint = MaterialTheme.colors.onBackground,
            )

            Text(
                text = "New branch",
                modifier = Modifier
                    .padding(bottom = 8.dp),
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "Create a new branch and check it out",
                modifier = Modifier
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
            )

            AdjustableOutlinedTextField(
                modifier = Modifier
                    .focusRequester(branchFieldFocusRequester)
                    .focusProperties {
                        this.next = buttonFieldFocusRequester
                    }
                    .width(300.dp)
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.matchesBinding(KeybindingOption.SIMPLE_ACCEPT) && branchField.isNotBlank()) {
                            onAccept(branchField)
                            true
                        } else {
                            false
                        }
                    },
                value = branchField,
                maxLines = 1,
                onValueChange = {
                    branchField = it
                },
            )
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.End)
            ) {
                PrimaryButton(
                    text = "Cancel",
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = onClose,
                    backgroundColor = Color.Transparent,
                    textColor = MaterialTheme.colors.onBackground,
                )
                PrimaryButton(
                    modifier = Modifier
                        .focusRequester(buttonFieldFocusRequester)
                        .focusProperties {
                            this.previous = branchFieldFocusRequester
                            this.next = branchFieldFocusRequester
                        },
                    enabled = branchField.isNotBlank(),
                    onClick = {
                        onAccept(branchField)
                    },
                    text = "Create branch"
                )
            }
        }

        LaunchedEffect(Unit) {
            branchFieldFocusRequester.requestFocus()
        }
    }
}