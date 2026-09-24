package com.rifsxd.ksunext.ui.screen

import androidx.core.net.toUri
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.PowerManager
import android.system.Os
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.LabelItemDefaults
import com.dergoogler.mmrl.ui.component.text.TextRow
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.InstallScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ModuleScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SuperUserScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.rifsxd.ksunext.*
import com.rifsxd.ksunext.R
import com.rifsxd.ksunext.ui.component.rememberConfirmDialog
import com.rifsxd.ksunext.ui.theme.ORANGE
import com.rifsxd.ksunext.ui.theme.blend
import com.rifsxd.ksunext.ui.component.AnimatedCount
import com.rifsxd.ksunext.ui.component.StatusChip
import com.rifsxd.ksunext.ui.component.TonalIcon
import com.rifsxd.ksunext.ui.component.heroBrush
import com.rifsxd.ksunext.ui.component.pressScale
import androidx.compose.foundation.LocalIndication
import com.rifsxd.ksunext.ui.util.*
import com.rifsxd.ksunext.ui.webui.WebUIActivity
import com.rifsxd.ksunext.ui.util.restartActivity
import com.rifsxd.ksunext.ui.util.module.LatestVersionInfo
import com.rifsxd.ksunext.ui.viewmodel.ModuleViewModel
import com.rifsxd.ksunext.ui.LocalNavBarEnabled
import com.rifsxd.ksunext.ui.LocalScrollState 
import com.rifsxd.ksunext.ui.screen.BottomBarDestination
import com.rifsxd.ksunext.ui.trackScroll 
import com.rifsxd.ksunext.ui.rememberScrollConnection
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    val kernelVersion = getKernelVersion()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val isManager = Natives.isManager
    val fullFeatured = Natives.isFullFeatured()
    val ksuVersion = if (isManager) Natives.version else null
    val ksuVersionTag = if (isManager) Natives.getVersionTag() else null
    val kernelUAPIVersion = if (isManager) Natives.kernelUAPIVersion else null
    val managerUAPIVersion = Natives.managerUAPIVersion

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val developerOptionsEnabled = prefs.getBoolean("enable_developer_options", false)
    
    // Get scroll state for bottom bar tracking
    val bottomBarScrollState = LocalScrollState.current

    val scrollState = LocalScrollState.current
    val navBarEnabled = LocalNavBarEnabled.current
    val isNavBarHidden = (scrollState?.isScrollingDown?.value ?: false) || (navBarEnabled?.value == false)
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + if (isNavBarHidden) 0.dp else 112.dp
    
    // Create scroll connection for bottom bar
    val bottomBarScrollConnection = if (bottomBarScrollState != null) {
        rememberScrollConnection(
            isScrollingDown = bottomBarScrollState.isScrollingDown,
            scrollOffset = bottomBarScrollState.scrollOffset,
            previousScrollOffset = bottomBarScrollState.previousScrollOffset,
            threshold = 30f
        )
    } else null

    Scaffold(
        topBar = {
            TopBar(
                kernelVersion,
                ksuVersion,
                onInstallClick = {
                    navigator.navigate(InstallScreenDestination)
                },
                onSettingsClick = {
                    navigator.navigate(SettingScreenDestination) {
                        popUpTo(NavGraphs.root.startRoute) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                navBarEnabled = LocalNavBarEnabled.current?.value ?: true,
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                // Chain scroll connections - bottom bar tracking first, then topbar behavior
                .let { modifier ->
                    if (bottomBarScrollConnection != null) {
                        modifier
                            .nestedScroll(bottomBarScrollConnection)
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    }
                }
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp)
                .padding(bottom = navBarPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val lkmMode = ksuVersion?.let {
                Natives.isLkmMode
            }

            StatusCard(
                kernelVersion,
                ksuVersion,
                kernelUAPIVersion,
                lkmMode,
                ksuVersionTagParam = ksuVersionTag
            ) {
                navigator.navigate(InstallScreenDestination)
            }

            val homeDestination = BottomBarDestination.entries.firstOrNull()
            val startRoute = homeDestination?.direction?.route

            if (fullFeatured) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SuperuserCard(
                            onClick = {
                                navigator.navigate(SuperUserScreenDestination) {
                                    popUpTo(NavGraphs.root.startRoute) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        ModuleCard(
                            onClick = {
                                navigator.navigate(ModuleScreenDestination) {
                                    popUpTo(NavGraphs.root.startRoute) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }

            val currentVersionCode = getManagerVersion(context).second
            val requiresNewKernel = isManager && kernelUAPIVersion != null && managerUAPIVersion > kernelUAPIVersion
            val requiresNewManager = isManager && kernelUAPIVersion != null && managerUAPIVersion < kernelUAPIVersion

            if (requiresNewKernel) {
                WarningCard(
                    stringResource(
                        id = if (lkmMode == true) R.string.require_kernel_version else R.string.require_kernel_version_gki,
                        kernelUAPIVersion!!,
                        managerUAPIVersion
                    ),
                    onClick = if (lkmMode == true) {
                        { navigator.navigate(InstallScreenDestination) }
                    } else null
                )
            }

            if (requiresNewManager) {
                WarningCard(
                    stringResource(
                        id = R.string.require_manager_version,
                        managerUAPIVersion,
                        kernelUAPIVersion!!
                    )
                )
            }

            val showLkmUpdate = isManager && lkmMode == true && Natives.isLkmBundled && ksuVersion != BuildConfig.UPSTREAM_VERSION_CODE && !requiresNewKernel && !requiresNewManager

            if (showLkmUpdate) {
                WarningCard(
                    message = stringResource(R.string.home_lkm_update_available),
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = { navigator.navigate(InstallScreenDestination) }
                )
            }

            if (ksuVersion != null && !rootAvailable()) {
                WarningCard(
                    stringResource(id = R.string.grant_root_failed),
                    onClick = {
                        restartActivity(context)
                    }
                )
            }

            val checkUpdate =
                LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
                    .getBoolean("check_update", true)
            if (checkUpdate) {
                UpdateCard()
            }

            InfoCard(autoExpand = developerOptionsEnabled)
            IssueReportCard()
            ContributorsCard()
            Spacer(Modifier)
        }
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    label: String,
    count: Int,
    badge: String? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scheme = MaterialTheme.colorScheme

    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = scheme.secondaryContainer,
            contentColor = scheme.onSecondaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 118.dp)
            .pressScale(interactionSource)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current
                    ) { onClick() } else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TonalIcon(
                    imageVector = icon,
                    containerColor = scheme.onSecondaryContainer.copy(alpha = 0.12f),
                    contentColor = scheme.onSecondaryContainer,
                    size = 34.dp,
                    iconSize = 19.dp
                )
                Spacer(Modifier.weight(1f))
                AnimatedVisibility(
                    visible = badge != null,
                    enter = fadeIn() + scaleIn(initialScale = 0.7f),
                    exit = fadeOut() + scaleOut(targetScale = 0.7f)
                ) {
                    StatusChip(
                        text = badge.orEmpty(),
                        containerColor = scheme.onSecondaryContainer.copy(alpha = 0.16f),
                        contentColor = scheme.onSecondaryContainer
                    )
                }
            }

            Column {
                AnimatedCount(
                    count = count,
                    style = MaterialTheme.typography.headlineMedium,
                    color = scheme.onSecondaryContainer
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSecondaryContainer.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun SuperuserCard(onClick: (() -> Unit)? = null) {
    val count = getSuperuserCount()
    StatTile(
        icon = Icons.Filled.AdminPanelSettings,
        label = if (count <= 1) {
            stringResource(R.string.home_superuser_count_singular)
        } else {
            stringResource(R.string.home_superuser_count_plural)
        },
        count = count,
        onClick = onClick
    )
}

@Composable
private fun ModuleCard(onClick: (() -> Unit)? = null) {
    val count = getModuleCount()
    val moduleViewModel: ModuleViewModel = viewModel()

    val moduleUpdateCount = remember(moduleViewModel.moduleList) {
        moduleViewModel.moduleList.count { module ->
            module.enabled && (moduleViewModel.checkUpdate(module).first.isNotEmpty())
        }
    }

    var showCount by remember { mutableStateOf(false) }
    LaunchedEffect(moduleUpdateCount) {
        showCount = false
        if (moduleUpdateCount > 0) {
            delay(1600)
            showCount = true
        }
    }

    val updateLabel = stringResource(id = R.string.home_module_update_available)

    StatTile(
        icon = Icons.Filled.Layers,
        label = if (count <= 1) {
            stringResource(R.string.home_module_count_singular)
        } else {
            stringResource(R.string.home_module_count_plural)
        },
        count = count,
        badge = when {
            moduleUpdateCount <= 0 -> null
            showCount -> "$moduleUpdateCount"
            else -> updateLabel
        },
        onClick = onClick
    )
}

@Composable
fun UpdateCard() {
    val context = LocalContext.current
    val latestVersionInfo = LatestVersionInfo()

    var preferSpoofed by remember { mutableStateOf(false) }

    val newVersion by produceState(initialValue = latestVersionInfo, key1 = preferSpoofed) {
        value = withContext(Dispatchers.IO) {
            checkNewVersion(preferSpoofed)
        }
    }

    val currentVersionCode = getManagerVersion(context).second
    val newVersionCode = newVersion.versionCode
    val newVersionUrl = newVersion.downloadUrl
    val changelog = newVersion.changelog
    val newVersionTag = newVersion.versionTag

    val uriHandler = LocalUriHandler.current
    val title = stringResource(id = R.string.module_changelog)
    val updateText = stringResource(id = R.string.module_update)

    AnimatedVisibility(
        visible = newVersionCode > currentVersionCode,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val scheme = MaterialTheme.colorScheme
        val interactionSource = remember { MutableInteractionSource() }
        val updateDialog = rememberConfirmDialog(onConfirm = { uriHandler.openUri(newVersionUrl) })

        ElevatedCard(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = scheme.primary,
                contentColor = scheme.onPrimary
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .pressScale(interactionSource)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroBrush(scheme.primary, scheme.tertiary.blend(scheme.primary, 0.45f)))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current
                    ) {
                        if (changelog.isEmpty()) {
                            uriHandler.openUri(newVersionUrl)
                        } else {
                            updateDialog.showConfirm(
                                title = title,
                                content = changelog,
                                markdown = true,
                                confirm = updateText
                            )
                        }
                    }
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TonalIcon(
                        imageVector = Icons.Filled.Update,
                        containerColor = scheme.onPrimary.copy(alpha = 0.16f),
                        contentColor = scheme.onPrimary,
                        size = 40.dp
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = if (!newVersionTag.isNullOrEmpty()) {
                            stringResource(id = R.string.new_version_available, newVersionTag, newVersionCode)
                        } else {
                            stringResource(id = R.string.new_version_available, "", newVersionCode)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = scheme.onPrimary
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.select_build_type),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .background(
                                color = scheme.onPrimary.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BuildTypeSegment(
                            selected = !preferSpoofed,
                            icon = Icons.Filled.Verified,
                            text = stringResource(id = R.string.main),
                            modifier = Modifier.weight(1f)
                        ) { preferSpoofed = false }

                        BuildTypeSegment(
                            selected = preferSpoofed,
                            icon = Icons.Filled.VisibilityOff,
                            text = stringResource(id = R.string.spoofed),
                            modifier = Modifier.weight(1f)
                        ) { preferSpoofed = true }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildTypeSegment(
    selected: Boolean,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue = if (selected) scheme.onPrimary else Color.Transparent,
        animationSpec = tween(220),
        label = "segmentContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) scheme.primary else scheme.onPrimary,
        animationSpec = tween(220),
        label = "segmentContent"
    )

    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(50))
            .background(container)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
    }
}

@Composable
fun RebootDropdownItem(@StringRes id: Int, reason: String = "") {
    DropdownMenuItem(text = {
        Text(stringResource(id))
    }, onClick = {
        reboot(reason)
    })
}

// @Composable
// fun getSeasonalIcon(): ImageVector {
//     val month = Calendar.getInstance().get(Calendar.MONTH) // 0-11 for January-December
//     return when (month) {
//         Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY -> Icons.Filled.AcUnit // Winter
//         Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> Icons.Filled.Spa // Spring
//         Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> Icons.Filled.WbSunny // Summer
//         Calendar.SEPTEMBER, Calendar.OCTOBER, Calendar.NOVEMBER -> Icons.Filled.Forest // Fall
//         else -> Icons.Filled.Whatshot // Fallback icon
//     }
// }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    kernelVersion: KernelVersion,
    ksuVersion: Int?,
    onInstallClick: () -> Unit,
    onSettingsClick: () -> Unit,
    navBarEnabled: Boolean,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var isSpinning by remember { mutableStateOf(false) }
    var rotationTarget by remember { mutableStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(
            durationMillis = 1400,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        finishedListener = {
            isSpinning = false
        }
    )

    val moduleViewModel: ModuleViewModel = viewModel()
    
    val webUILauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    val context = LocalContext.current
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (!isSpinning) {
                        isSpinning = true
                        rotationTarget += 360f * 6
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ksu_next),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .graphicsLayer {
                            rotationZ = rotation
                        }
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            }
        },
        actions = {
            if (!navBarEnabled) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.settings)
                    )
                }
            }

            if (ksuVersion != null) {
                IconButton(onClick = onInstallClick) {
                    Icon(
                        imageVector = Icons.Filled.Archive,
                        contentDescription = stringResource(id = R.string.install)
                    )
                }
            }

            if (ksuVersion != null) {
                var showDropdown by remember { mutableStateOf(false) }
                IconButton(onClick = {
                    showDropdown = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = stringResource(id = R.string.reboot)
                    )

                    DropdownMenu(expanded = showDropdown, onDismissRequest = {
                        showDropdown = false
                    }) {
                        RebootDropdownItem(id = R.string.reboot)
                        RebootDropdownItem(id = R.string.reboot_userspace, reason = "soft-reboot")

                        val pm =
                            LocalContext.current.getSystemService(Context.POWER_SERVICE) as PowerManager?
                        @Suppress("DEPRECATION")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && pm?.isRebootingUserspaceSupported == true) {
                            RebootDropdownItem(id = R.string.reboot_userspace, reason = "userspace")
                        }
                        RebootDropdownItem(id = R.string.reboot_recovery, reason = "recovery")
                        RebootDropdownItem(id = R.string.reboot_bootloader, reason = "bootloader")
                        RebootDropdownItem(id = R.string.reboot_download, reason = "download")
                        RebootDropdownItem(id = R.string.reboot_edl, reason = "edl")
                    }
                }
            }
        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusCard(
    kernelVersionParam: KernelVersion,
    ksuVersionParam: Int?,
    uapiVerParam: Int? = null,
    lkmModeParam: Boolean?,
    moduleUpdateCount: Int = 0,
    ksuVersionTagParam: String? = null,
    onClickInstall: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val installed = ksuVersionParam != null
    val gkiCapable = kernelVersionParam.isGKI()

    val container = when {
        installed -> scheme.primary
        gkiCapable -> scheme.secondaryContainer
        else -> scheme.errorContainer
    }
    val onContainer = when {
        installed -> scheme.onPrimary
        gkiCapable -> scheme.onSecondaryContainer
        else -> scheme.onErrorContainer
    }
    val accent = when {
        installed -> scheme.tertiary.blend(scheme.primary, 0.4f)
        gkiCapable -> scheme.tertiaryContainer
        else -> scheme.error.blend(scheme.errorContainer, 0.55f)
    }

    val icon = when {
        installed -> Icons.Filled.Verified
        gkiCapable -> Icons.Filled.AutoFixHigh
        else -> Icons.Filled.ReportProblem
    }

    val headline = when {
        installed -> stringResource(R.string.home_working)
        gkiCapable -> stringResource(R.string.home_not_installed)
        else -> stringResource(R.string.home_failure)
    }

    val subtitle = when {
        installed -> {
            val ksuVer = ksuVersionParam ?: 0
            val uapiVer = uapiVerParam ?: 0
            val tag = if (!ksuVersionTagParam.isNullOrEmpty()) ksuVersionTagParam else "v0.0.0"
            stringResource(R.string.home_working_version, tag, "$ksuVer-$uapiVer")
        }
        gkiCapable -> stringResource(R.string.home_click_to_install)
        else -> stringResource(R.string.home_failure_tip)
    }

    val chipContainer = onContainer.copy(alpha = 0.16f)
    val interactionSource = remember { MutableInteractionSource() }

    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = container,
            contentColor = onContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroBrush(container, accent))
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    enabled = !installed
                ) { onClickInstall() }
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(onContainer.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = onContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.headlineSmall,
                        color = onContainer
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainer.copy(alpha = 0.8f)
                    )
                }

                if (!installed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = onContainer
                    )
                }
            }

            if (installed) {
                val workingMode = if (lkmModeParam == true || lkmModeParam == false) {
                    val mode = if (lkmModeParam == true) "LKM" else "BUILT-IN"
                    "$mode (" + kernelVersionParam.getKernelType() + ")"
                } else kernelVersionParam.getKernelType()

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(
                        text = workingMode,
                        icon = if (Natives.isSafeMode) Icons.Filled.Security else Icons.Filled.VerifiedUser,
                        containerColor = chipContainer,
                        contentColor = onContainer
                    )

                    if (lkmModeParam == true && !Natives.isLkmBundled) {
                        StatusChip(
                            text = stringResource(R.string.home_lkm_custom),
                            icon = Icons.Filled.Extension,
                            containerColor = chipContainer,
                            contentColor = onContainer
                        )
                    }

                    if (isSuCompatDisabled()) {
                        StatusChip(
                            text = stringResource(R.string.sucompat_disabled),
                            icon = Icons.Filled.Warning,
                            containerColor = scheme.tertiaryContainer,
                            contentColor = scheme.onTertiaryContainer
                        )
                    }

                    if (Natives.isLateLoadMode) {
                        StatusChip(
                            text = stringResource(R.string.jailbreak_mode),
                            icon = Icons.Filled.Warning,
                            containerColor = scheme.errorContainer,
                            contentColor = scheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WarningCard(
    message: String, color: Color = MaterialTheme.colorScheme.error, onClick: (() -> Unit)? = null
) {
    val resolvedContent = contentColorFor(color)
    val contentColor = if (resolvedContent == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        resolvedContent
    }
    val interactionSource = remember { MutableInteractionSource() }

    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = color,
            contentColor = contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    onClick?.let {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current
                        ) { it() }
                    } ?: Modifier
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TonalIcon(
                imageVector = Icons.Filled.SentimentDissatisfied,
                containerColor = contentColor.copy(alpha = 0.16f),
                contentColor = contentColor
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (onClick != null) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
    }
}

@Composable
private fun InfoCardRow(
    label: String,
    content: String,
    icon: ImageVector? = null,
    painter: Painter? = null
) {
    val scheme = MaterialTheme.colorScheme
    val plate = scheme.primary.copy(alpha = 0.12f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        when {
            icon != null -> TonalIcon(
                imageVector = icon,
                containerColor = plate,
                contentColor = scheme.primary
            )
            painter != null -> TonalIcon(
                painter = painter,
                containerColor = plate,
                contentColor = scheme.primary
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.onSurface
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

private data class HomeInfoSnapshot(
    val managerVersion: Pair<String, Long> = "" to 0L,
    val managerUAPIVersion: Int = 0,
    val managerAppId: Int = 0,
    val hookMode: String? = null,
    val metaModule: String? = null,
    val metaInfo: ModuleViewModel.ModuleInfo? = null,
    val suSFS: String? = null,
    val suSFSVersion: String? = null,
    val suSFSVariant: String? = null,
    val zygiskEnabled: Boolean = false,
    val zygiskInfo: ModuleViewModel.ModuleInfo? = null,
    val unameRelease: String = "",
    val unameMachine: String = "",
    val seccompStatus: String = "Unavailable",
)

private fun buildHomeInfoSnapshot(
    context: Context,
    ksuVersion: Int?,
    moduleList: List<ModuleViewModel.ModuleInfo>,
): HomeInfoSnapshot {
    val managerVersion = getManagerVersion(context)
    val managerUAPIVersion = Natives.managerUAPIVersion
    val managerAppId = Natives.getManagerAppid()
    val hookMode = if (ksuVersion == null) null else {
        Natives.getHookMode().takeUnless { it.isNullOrBlank() } ?: "Unavailable"
    }
    val metaModule = if (ksuVersion == null) null else getMetaModule()
    val metaInfo = if (metaModule == null) null else moduleList.firstOrNull { it.isMetaModule }
    val suSFS = if (ksuVersion == null) null else getSuSFS()
    val suSFSVersion = if (suSFS == "Supported" && ksuVersion != null) getSuSFSVersion() else null
    val suSFSVariant = if (suSFS == "Supported" && ksuVersion != null) getSuSFSVariant() else null
    val zygiskEnabled = if (ksuVersion == null) false else Natives.isZygiskEnabled()
    val zygiskInfo = if (!zygiskEnabled) null else moduleList.firstOrNull { it.isZygisk && it.enabled }
    val uname = kotlin.runCatching { Os.uname() }.getOrNull()
    val statusInt = kotlin.runCatching { Os.prctl(21, 0, 0, 0, 0) }.getOrDefault(-1)
    val seccompStatus = when (statusInt) {
        -1 -> "Unavailable"
        0 -> "Disabled"
        1 -> "Strict"
        2 -> "Filter"
        else -> "Unknown"
    }

    return HomeInfoSnapshot(
        managerVersion = managerVersion,
        managerUAPIVersion = managerUAPIVersion,
        managerAppId = managerAppId,
        hookMode = hookMode,
        metaModule = metaModule,
        metaInfo = metaInfo,
        suSFS = suSFS,
        suSFSVersion = suSFSVersion,
        suSFSVariant = suSFSVariant,
        zygiskEnabled = zygiskEnabled,
        zygiskInfo = zygiskInfo,
        unameRelease = uname?.release.orEmpty(),
        unameMachine = uname?.machine.orEmpty(),
        seccompStatus = seccompStatus,
    )
}

@Composable
private fun InfoCard(autoExpand: Boolean = false) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    val isManager = remember { Natives.isManager }
    val ksuVersion = remember(isManager) { if (isManager) Natives.version else null }

    var expanded by rememberSaveable { mutableStateOf(false) }
    val developerOptionsEnabled = remember(prefs) { prefs.getBoolean("enable_developer_options", false) }

    LaunchedEffect(autoExpand) {
        if (autoExpand) {
            expanded = true
        }
    }

    val moduleViewModel: ModuleViewModel = viewModel()
    var homeInfo by remember { mutableStateOf(HomeInfoSnapshot()) }

    LaunchedEffect(ksuVersion, moduleViewModel.moduleList) {
        homeInfo = withContext(Dispatchers.IO) {
            buildHomeInfoSnapshot(context, ksuVersion, moduleViewModel.moduleList)
        }
    }

    Card(shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val managerVersion = homeInfo.managerVersion
            val managerUAPIVersion = homeInfo.managerUAPIVersion
            InfoCardRow(
                label = stringResource(R.string.home_manager_version),
                content = if (developerOptionsEnabled) {
                    "${managerVersion.first} (${managerVersion.second}-${managerUAPIVersion}) | UID: ${homeInfo.managerAppId}"
                } else {
                    "${managerVersion.first} (${managerVersion.second}-${managerUAPIVersion})"
                },
                icon = Icons.Filled.AutoAwesomeMotion
            )

            if (ksuVersion != null) {
                val hookMode = homeInfo.hookMode ?: stringResource(R.string.unavailable)

                InfoCardRow(
                    label = stringResource(R.string.hook_mode),
                    content = hookMode,
                    icon = Icons.Filled.Phishing
                )

                val metaModule = homeInfo.metaModule
                val metaInfo = homeInfo.metaInfo
                val metaDetail = if (metaInfo != null) " | ${metaInfo.name} | ${metaInfo.version}" else ""
                InfoCardRow(
                    label = stringResource(R.string.home_metamodule_status),
                    content = when {
                        metaModule == "Installed" && metaInfo != null && !metaInfo.enabled ->
                            stringResource(R.string.disabled) + metaDetail
                        metaModule == "Installed" ->
                            stringResource(R.string.installed) + metaDetail
                        else ->
                            stringResource(R.string.home_not_installed)
                    },
                    icon = Icons.Filled.SettingsSuggest
                )

                val suSFS = homeInfo.suSFS
                if (suSFS == "Supported") {
                    InfoCardRow(
                        label = stringResource(R.string.home_susfs_version),
                        content = "${stringResource(R.string.supported)} | ${homeInfo.suSFSVersion ?: stringResource(R.string.unavailable)} (${homeInfo.suSFSVariant ?: stringResource(R.string.unavailable)})",
                        painter = painterResource(R.drawable.ic_sus)
                    )
                }

                if (homeInfo.zygiskEnabled) {
                    val zygiskInfo = homeInfo.zygiskInfo
                    val zygiskDetail = if (zygiskInfo != null) " | ${zygiskInfo.name} | ${zygiskInfo.version}" else ""
                    InfoCardRow(
                        label = stringResource(R.string.zygisk_status),
                        content = stringResource(R.string.enabled) + zygiskDetail,
                        icon = Icons.Filled.Vaccines
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    InfoCardRow(
                        label = stringResource(R.string.home_kernel),
                        content = "${homeInfo.unameRelease} (${homeInfo.unameMachine})",
                        painter = painterResource(R.drawable.ic_linux)
                    )

                    InfoCardRow(
                        label = stringResource(R.string.home_android),
                        content = "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})",
                        icon = Icons.Filled.Android
                    )

                    InfoCardRow(
                        label = stringResource(R.string.home_abi),
                        content = Build.SUPPORTED_ABIS.joinToString(", "),
                        icon = Icons.Filled.Memory
                    )

                    InfoCardRow(
                        label = stringResource(R.string.home_selinux_status),
                        content = getSELinuxStatus(),
                        icon = Icons.Filled.Security
                    )

                    InfoCardRow(
                        label = stringResource(R.string.home_seccomp_status),
                        content = homeInfo.seccompStatus,
                        icon = Icons.Filled.LocalPolice
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val rotationAngle by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(durationMillis = 300),
                    label = "infoChevron"
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 26.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Show less" else "Show more",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.graphicsLayer { rotationZ = rotationAngle }
                    )
                }
            }
        }
    }
}

data class Contributor(
    val login: String,
    val name: String? = null,
    val githubUrl: String,
    val role: String,
    val donationUrl: String
)

@Composable
fun ContributorsCard() {
    val uriHandler = LocalUriHandler.current

    val contributors = listOf(
        Contributor(
            login = "rifsxd",
            name = "Rifat Azad",
            githubUrl = "https://github.com/rifsxd",
            role = "Lead Developer",
            donationUrl = "https://github.com/KernelSU-Next/KernelSU-Next/tree/dev?tab=readme-ov-file#-donations"
        ),
        Contributor(
            login = "tiann",
            name = "Weishu",
            githubUrl = "https://github.com/tiann",
            role = "KernelSU Author",
            donationUrl = "https://www.patreon.com/weishu"
        ),
        Contributor(
            login = "fatalcoder524",
            githubUrl = "https://github.com/fatalcoder524",
            role = "Frontend Maintainer",
            donationUrl = "https://github.com/sponsors/fatalcoder524"
        ),
        Contributor(
            login = "pershoot",
            githubUrl = "https://github.com/pershoot",
            role = "Backend Maintainer",
            donationUrl = "https://github.com/sponsors/pershoot"
        ),
        Contributor(
            login = "maxsteeel",
            name = "Max",
            githubUrl = "https://github.com/maxsteeel",
            role = "Legacy Maintainer",
            donationUrl = "https://github.com/sponsors/maxsteeel"
        )
    )

    Card(shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.contributors),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            contributors.forEach { contributor ->
                ContributorRow(
                    contributor = contributor,
                    onProfileClick = { uriHandler.openUri(contributor.githubUrl) },
                    onDonateClick = { uriHandler.openUri(contributor.donationUrl) }
                )
            }
        }
    }
}

@Composable
private fun ContributorRow(
    contributor: Contributor,
    onProfileClick: () -> Unit,
    onDonateClick: () -> Unit
) {
    var imageLoadFailed by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onProfileClick() }
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!imageLoadFailed) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://avatars.githubusercontent.com/${contributor.login}?s=80")
                            .crossfade(true)
                            .build(),
                        contentDescription = contributor.login,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onError = { imageLoadFailed = true }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Column {
                Text(
                    text = contributor.name?.takeIf { it.isNotBlank() }
                        ?.let { "${contributor.login} ($it)" }
                        ?: contributor.login,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = contributor.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        FilledTonalButton(
            onClick = onDonateClick,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.support),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun IssueReportCard() {
    val uriHandler = LocalUriHandler.current
    val githubIssueUrl = stringResource(R.string.issue_report_github_link)
    val telegramUrl = stringResource(R.string.issue_report_telegram_link)

    Card(shape = MaterialTheme.shapes.large) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.issue_report_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.issue_report_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.issue_report_body_2),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledIconButton(
                    onClick = { uriHandler.openUri(githubIssueUrl) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = stringResource(R.string.issue_report_github),
                        modifier = Modifier.size(20.dp)
                    )
                }
                FilledIconButton(
                    onClick = { uriHandler.openUri(telegramUrl) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_telegram),
                        contentDescription = stringResource(R.string.issue_report_telegram),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
fun handleDynamicShortcuts(context: Context, moduleConfigs: List <Pair<ModuleViewModel.ModuleInfo, Int>>) {
    ShortcutManagerCompat.removeAllDynamicShortcuts(context)

    moduleConfigs.forEach { (module, iconRes) ->
        val shortcut = ShortcutInfoCompat.Builder(context, module.id)
            .setShortLabel(module.name)
            .setLongLabel(module.name)
            .setIcon(IconCompat.createWithResource(context, iconRes))
            .setCategories(setOf(ShortcutInfo.SHORTCUT_CATEGORY_CONVERSATION))
            .setIntent(
                Intent(context, WebUIActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = "kernelsu://webui/${module.id}".toUri()
                    putExtra("id", module.id)
                    putExtra("name", module.name)
                }
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }
}

fun getManagerVersion(context: Context): Pair<String, Long> {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)!!
    val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
    return Pair(packageInfo.versionName!!, versionCode)
}

@Preview
@Composable
private fun StatusCardPreview() {
    Column {
        StatusCard(KernelVersion(5, 10, 101), 1, 1, null)
        StatusCard(KernelVersion(5, 10, 101), 20000, 1, true)
        StatusCard(KernelVersion(5, 10, 101), null, null, true)
        StatusCard(KernelVersion(4, 10, 101), null, null, false)
    }
}

@Preview
@Composable
private fun WarningCardPreview() {
    Column {
        WarningCard(message = "Warning message")
        WarningCard(
            message = "Warning message",
            MaterialTheme.colorScheme.outlineVariant,
            onClick = {})
    }
}
