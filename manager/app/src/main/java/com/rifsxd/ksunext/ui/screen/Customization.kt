package com.rifsxd.ksunext.ui.screen

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.rifsxd.ksunext.ui.LocalScrollState
import com.rifsxd.ksunext.ui.rememberScrollConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.dropUnlessResumed
import com.rifsxd.ksunext.ui.MainActivity
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.rifsxd.ksunext.Natives
import com.rifsxd.ksunext.R
import android.os.Build
import androidx.compose.material.icons.filled.Palette
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import com.rifsxd.ksunext.ui.theme.ThemeAccent
import com.rifsxd.ksunext.ui.component.TonalIcon
import com.rifsxd.ksunext.ui.component.SwitchItem
import com.rifsxd.ksunext.ui.util.refreshActivity
import com.rifsxd.ksunext.ui.util.LocalSnackbarHost
import com.rifsxd.ksunext.ui.util.LocaleHelper

/**
 * @author rifsxd
 * @date 2025/6/1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun CustomizationScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    // Bottom bar scroll tracking
    val bottomBarScrollState = LocalScrollState.current
    val bottomBarScrollConnection = if (bottomBarScrollState != null) {
        rememberScrollConnection(
            isScrollingDown = bottomBarScrollState.isScrollingDown,
            scrollOffset = bottomBarScrollState.scrollOffset,
            previousScrollOffset = bottomBarScrollState.previousScrollOffset,
            threshold = 30f
        )
    } else null
    val snackBarHost = LocalSnackbarHost.current

    val isManager = Natives.isManager
    val ksuVersion = if (isManager) Natives.version else null

    val scrollState = LocalScrollState.current
    val isNavBarHidden = scrollState?.isScrollingDown?.value ?: false
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + if (isNavBarHidden) 0.dp else 112.dp

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var currentAppLocale by remember {
        mutableStateOf(LocaleHelper.getCurrentAppLocale(context))
    }
    var showLanguageSheet by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        currentAppLocale = LocaleHelper.getCurrentAppLocale(context)
    }

    val languageOptions = remember(context) {
        listOf(
            LocaleHelper.SYSTEM_LANGUAGE_TAG to context.getString(R.string.system_default)
        ) + LocaleHelper.getSupportedLocales(context)
            .map { it.toLanguageTag() to it.getDisplayName(it) }
            .sortedBy { (_, displayName) -> displayName }
    }
    val currentLanguageTag = currentAppLocale?.toLanguageTag()
        ?: LocaleHelper.SYSTEM_LANGUAGE_TAG

    Scaffold(
        topBar = {
            TopBar(
                onBack = dropUnlessResumed {
                    navigator.popBackStack()
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackBarHost, modifier = Modifier.padding(bottom = navBarPadding)) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
    
        Column(
            modifier = Modifier
                .padding(paddingValues)
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
        ) {

            val language = stringResource(id = R.string.settings_language)
            val currentLanguageDisplay = currentAppLocale?.let { it.getDisplayName(it) }
                ?: stringResource(R.string.system_default)

            ListItem(
                leadingContent = { Icon(Icons.Filled.Translate, language) },
                headlineContent = { Text(
                    text = language,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                ) },
                supportingContent = { Text(currentLanguageDisplay) },
                modifier = Modifier.clickable {
                    showLanguageSheet = true
                }
            )

            var useBanner by rememberSaveable {
                mutableStateOf(
                    prefs.getBoolean("use_banner", true)
                )
            }
            if (ksuVersion != null) {
                SwitchItem(
                    icon = Icons.Filled.ViewCarousel,
                    title = stringResource(id = R.string.settings_banner),
                    summary = stringResource(id = R.string.settings_banner_summary),
                    checked = useBanner
                ) {
                    prefs.edit { putBoolean("use_banner", it) }
                    useBanner = it
                }
            }

            var dynamicColorEnabled by rememberSaveable {
                mutableStateOf(
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        prefs.getBoolean("enable_dynamic_color", true)
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val activity = LocalContext.current as? MainActivity
                SwitchItem(
                    icon = Icons.Filled.Palette,
                    title = stringResource(id = R.string.settings_dynamic_color),
                    summary = stringResource(id = R.string.settings_dynamic_color_summary),
                    checked = dynamicColorEnabled
                ) { checked ->
                    activity?.setDynamicColor(checked)
                    dynamicColorEnabled = checked
                }
            }

            if (!dynamicColorEnabled) {
                val activity = LocalContext.current as? MainActivity
                var accent by remember {
                    mutableStateOf(ThemeAccent.fromKey(prefs.getString("theme_accent", null)))
                }
                AccentPicker(
                    selected = accent,
                    onSelect = { picked ->
                        activity?.setThemeAccent(picked)
                        accent = picked
                    }
                )
            }

            var enableAmoled by rememberSaveable {
                mutableStateOf(
                    prefs.getBoolean("enable_amoled", false)
                )
            }
            if (isSystemInDarkTheme()) {
                val activity = LocalContext.current as? MainActivity
                SwitchItem(
                    icon = Icons.Filled.Contrast,
                    title = stringResource(id = R.string.settings_amoled_mode),
                    summary = stringResource(id = R.string.settings_amoled_mode_summary),
                    checked = enableAmoled
                ) { checked ->
                    activity?.setAmoledMode(checked)
                    enableAmoled = checked
                }
            }
        }
    }

    if (showLanguageSheet) {
        LanguageBottomSheet(
            options = languageOptions,
            selectedLanguageTag = currentLanguageTag,
            onDismiss = { showLanguageSheet = false },
            onLanguageSelected = { languageTag ->
                showLanguageSheet = false
                if (languageTag != currentLanguageTag) {
                    LocaleHelper.setAppLocale(context, languageTag)
                    currentAppLocale = LocaleHelper.getCurrentAppLocale(context)
                    if (!LocaleHelper.usesFrameworkLocaleManager) {
                        refreshActivity(context)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageBottomSheet(
    options: List<Pair<String, String>>,
    selectedLanguageTag: String,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = stringResource(R.string.select_language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .semantics { heading() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .selectableGroup(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(options, key = { (tag, _) -> tag }) { (tag, displayName) ->
                val selected = tag == selectedLanguageTag
                ListItem(
                    headlineContent = { Text(displayName) },
                    trailingContent = {
                        RadioButton(selected = selected, onClick = null)
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onLanguageSelected(tag) }
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onBack: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = { Text(
                text = stringResource(R.string.customization),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            ) }, navigationIcon = {
            IconButton(
                onClick = onBack
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

@Preview
@Composable
private fun CustomizationPreview() {
    CustomizationScreen(EmptyDestinationsNavigator)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccentPicker(
    selected: ThemeAccent,
    onSelect: (ThemeAccent) -> Unit
) {
    val dark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TonalIcon(
                imageVector = Icons.Filled.ColorLens,
                contentDescription = null,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(id = R.string.settings_accent_color),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.settings_accent_color_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeAccent.entries.forEach { option ->
                val colour = option.swatch(dark)
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colour)
                        .clickable { onSelect(option) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = option.key,
                            tint = if (dark) Color.Black.copy(alpha = 0.7f) else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
