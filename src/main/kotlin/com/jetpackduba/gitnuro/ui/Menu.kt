@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)

package com.jetpackduba.gitnuro.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.jetpackduba.gitnuro.AppIcons
import com.jetpackduba.gitnuro.extensions.handMouseClickable
import com.jetpackduba.gitnuro.extensions.handOnHover
import com.jetpackduba.gitnuro.extensions.ignoreKeyEvents
import com.jetpackduba.gitnuro.git.remote_operations.PullType
import com.jetpackduba.gitnuro.ui.components.PrimaryButton
import com.jetpackduba.gitnuro.ui.components.tooltip.InstantTooltip
import com.jetpackduba.gitnuro.ui.context_menu.*
import com.jetpackduba.gitnuro.viewmodels.MenuViewModel

// TODO Add tooltips to all the buttons
@Composable
fun Menu(
    modifier: Modifier,
    menuViewModel: MenuViewModel,
    onCreateBranch: () -> Unit,
    onOpenAnotherRepository: (String) -> Unit,
    onOpenAnotherRepositoryFromPicker: () -> Unit,
    onStashWithMessage: () -> Unit,
    onQuickActions: () -> Unit,
    onShowSettingsDialog: () -> Unit,
    showOpenPopup: Boolean,
    onShowOpenPopupChange: (Boolean) -> Unit,
) {
    val isPullWithRebaseDefault by menuViewModel.isPullWithRebaseDefault.collectAsState()
    val lastLoadedTabs by menuViewModel.lastLoadedTabs.collectAsState()
    val (position, setPosition) = remember { mutableStateOf<LayoutCoordinates?>(null) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InstantTooltip(
            text = "Open a different repository",
            enabled = !showOpenPopup,
        ) {
            MenuButton(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .onGloballyPositioned { setPosition(it) },
                title = "Open",
                icon = painterResource(AppIcons.OPEN),
                onClick = { onShowOpenPopupChange(true) },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        val pullTooltip = if (isPullWithRebaseDefault) {
            "Pull current branch with rebase"
        } else {
            "Pull current branch"
        }


        ExtendedMenuButton(
            modifier = Modifier.padding(end = 4.dp),
            title = "Pull",
            tooltipText = pullTooltip,
            icon = painterResource(AppIcons.DOWNLOAD),
            onClick = { menuViewModel.pull(PullType.DEFAULT) },
            extendedListItems = pullContextMenuItems(
                isPullWithRebaseDefault = isPullWithRebaseDefault,
                onPullWith = {
                    // Do the reverse of the default
                    val pullType = if (isPullWithRebaseDefault) {
                        PullType.MERGE
                    } else {
                        PullType.REBASE
                    }

                    menuViewModel.pull(pullType = pullType)
                },
                onFetchAll = {
                    menuViewModel.fetchAll()
                }
            )
        )

        ExtendedMenuButton(
            title = "Push",
            tooltipText = "Push current branch changes",
            icon = painterResource(AppIcons.UPLOAD),
            onClick = { menuViewModel.push() },
            extendedListItems = pushContextMenuItems(
                onPushWithTags = {
                    menuViewModel.push(force = false, pushTags = true)
                },
                onForcePush = {
                    menuViewModel.push(force = true)
                }
            )
        )

        Spacer(modifier = Modifier.width(32.dp))

        InstantTooltip(
            text = "Create a new branch",
        ) {
            MenuButton(
                title = "Branch",
                icon = painterResource(AppIcons.BRANCH),
            ) {
                onCreateBranch()
            }
        }


        Spacer(modifier = Modifier.width(32.dp))

        ExtendedMenuButton(
            modifier = Modifier.padding(end = 4.dp),
            title = "Stash",
            tooltipText = "Stash uncommitted changes",
            icon = painterResource(AppIcons.STASH),
            onClick = { menuViewModel.stash() },
            extendedListItems = stashContextMenuItems(
                onStashWithMessage = onStashWithMessage
            )
        )

        InstantTooltip(
            text = "Pop the last stash"
        ) {
            MenuButton(
                title = "Pop",
                icon = painterResource(AppIcons.APPLY_STASH),
            ) { menuViewModel.popStash() }
        }

        Spacer(modifier = Modifier.weight(1f))

        InstantTooltip(
            text = "Open a terminal in the repository's path"
        ) {
            MenuButton(
                modifier = Modifier.padding(end = 4.dp),
                title = "Terminal",
                icon = painterResource(AppIcons.TERMINAL),
                onClick = { menuViewModel.openTerminal() },
            )
        }

        MenuButton(
            modifier = Modifier.padding(end = 4.dp),
            title = "Actions",
            icon = painterResource(AppIcons.BOLT),
            onClick = onQuickActions,
        )

        InstantTooltip(
            text = "Gitnuro's settings",
            modifier = Modifier.padding(end = 16.dp)
        ) {
            MenuButton(
                title = "Settings",
                icon = painterResource(AppIcons.SETTINGS),
                onClick = onShowSettingsDialog,
            )
        }
    }

    if (showOpenPopup && position != null) {
        val boundsInRoot = position.boundsInRoot()

        Popup(
            popupPositionProvider =
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    return IntOffset(boundsInRoot.left.toInt(), boundsInRoot.bottom.toInt())
                }
            },
            onDismissRequest = { onShowOpenPopupChange(false) },
            properties = PopupProperties(focusable = true),
        ) {
            val searchFocusRequester = remember { FocusRequester() }

            Column(
                modifier = Modifier
                    .width(600.dp)
                    .heightIn(max = 600.dp)
                    .background(MaterialTheme.colors.surface)
                    .border(2.dp, MaterialTheme.colors.onBackground.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                PrimaryButton(
                    text = "Open a repository",
                    onClick = {
                        onShowOpenPopupChange(false)
                        onOpenAnotherRepositoryFromPicker()
                    },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    RecentRepositoriesList(
                        recentlyOpenedRepositories = lastLoadedTabs,
                        canRepositoriesBeRemoved = false,
                        searchFieldFocusRequester = searchFocusRequester,
                        onRemoveRepositoryFromRecent = {},
                        onOpenKnownRepository = {
                            onShowOpenPopupChange(false)
                            onOpenAnotherRepository(it)
                        },
                    )
                }
            }

            LaunchedEffect(Unit) {
                searchFocusRequester.requestFocus()
            }
        }
    }
}

@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String,
    icon: Painter,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .ignoreKeyEvents()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.surface)
            .handMouseClickable { if (enabled) onClick() }
            .size(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = title,
            modifier = Modifier
                .size(24.dp),
            tint = MaterialTheme.colors.onBackground,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.caption,
            maxLines = 1,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onBackground,
        )
    }
}

@Composable
fun ExtendedMenuButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String,
    tooltipText: String,
    icon: Painter,
    onClick: () -> Unit,
    extendedListItems: List<ContextMenuElement>,
) {
    Row(
        modifier = modifier
            .size(width = 64.dp, height = 56.dp)
            .ignoreKeyEvents()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.surface)
            .handMouseClickable { if (enabled) onClick() }
    ) {
        InstantTooltip(
            text = tooltipText,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = icon,
                    contentDescription = title,
                    modifier = Modifier
                        .size(24.dp),
                    tint = MaterialTheme.colors.onBackground,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onBackground,
                    maxLines = 1,
                )
            }
        }

        DropDownMenu(
            items = { extendedListItems }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .ignoreKeyEvents(),
                contentAlignment = Alignment.Center,
            ) {

                Icon(
                    painterResource(AppIcons.EXPAND_MORE),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onBackground,
                    modifier = Modifier.size(16.dp)
                )

            }
        }
    }
}

@Composable
fun IconMenuButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: Painter,
    onClick: () -> Unit
) {
    val iconColor = if (enabled) {
        MaterialTheme.colors.primaryVariant
    } else {
        MaterialTheme.colors.secondaryVariant
    }

    IconButton(
        modifier = modifier
            .handOnHover(),
        enabled = enabled,
        onClick = onClick,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(24.dp),
                colorFilter = ColorFilter.tint(iconColor),
            )
        }

    }
}

