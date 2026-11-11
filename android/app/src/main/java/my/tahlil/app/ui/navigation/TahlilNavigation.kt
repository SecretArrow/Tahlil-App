package my.tahlil.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import my.tahlil.app.data.ArabicStyleSettings
import my.tahlil.app.data.DisplayToggles
import my.tahlil.app.data.TahlilContent
import my.tahlil.app.data.ThemeMode
import my.tahlil.app.ui.components.Header
import my.tahlil.app.ui.screens.HomeScreen
import my.tahlil.app.ui.screens.SectionDetailScreen
import my.tahlil.app.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

/** All navigation routes — keep them in one place so they're easy to audit. */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val SECTION = "section/{sectionId}"

    fun section(id: String) = "section/$id"
}

/**
 * Top-level navigation host.
 *
 * Three destinations:
 *  - [Routes.HOME]       — the hero + scrollable list of all 11 sections
 *  - [Routes.SECTION]    — a focused single-section view (reached from a
 *                          CTA button or a drawer entry)
 *  - [Routes.SETTINGS]   — display & typography settings
 *
 * The drawer is mounted at this level so it's available from any screen.
 *
 * Behavior:
 *  - On first composition, if [lastReadSectionId] is set (and we haven't
 *    already auto-navigated this session), navigate directly to that
 *    section so the user resumes their reading. The flag is
 *    `rememberSaveable` so config changes don't double-navigate.
 *  - Whenever the user opens a section, [onSetLastReadSection] is called
 *    so the repository can persist the new pointer.
 *  - When [isFullscreen] is true, a floating "exit fullscreen" FAB is
 *    overlaid at the top-right corner so the user can exit immersive mode
 *    without the system bars (since they're hidden).
 */
@Composable
fun TahlilNavigation(
    toggles: DisplayToggles,
    style: ArabicStyleSettings,
    themeMode: ThemeMode,
    isDark: Boolean,
    isFullscreen: Boolean,
    lastReadSectionId: String?,
    onTogglesChange: (DisplayToggles) -> Unit,
    onStyleChange: (ArabicStyleSettings) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onToggleThemeClick: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onSetLastReadSection: (String) -> Unit,
    onClearLastReadSection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    var activeSectionId by remember { mutableStateOf(TahlilContent.sections.firstOrNull()?.id ?: "") }
    val drawerState = androidx.compose.material3.rememberDrawerState(
        initialValue = androidx.compose.material3.DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()

    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }
    val closeDrawer: () -> Unit = {
        scope.launch { drawerState.close() }
    }

    // --- Auto-navigate to the last-read section on first launch ----------
    // We use rememberSaveable so the flag survives config changes (rotations)
    // — but it resets on process death, which is fine: "first launch of
    // session" only means within a single foreground session.
    var hasAutoNavigated by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(lastReadSectionId) {
        if (!hasAutoNavigated && !lastReadSectionId.isNullOrEmpty()) {
            // Verify the stored id still maps to a real section (in case the
            // content was updated between sessions).
            if (TahlilContent.byId(lastReadSectionId) != null) {
                hasAutoNavigated = true
                navController.navigate(Routes.section(lastReadSectionId)) {
                    launchSingleTop = true
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                activeSectionId = activeSectionId,
                onSectionClick = { id ->
                    closeDrawer()
                    navController.navigate(Routes.section(id)) {
                        launchSingleTop = true
                    }
                    onSetLastReadSection(id)
                },
                onHomeClick = {
                    closeDrawer()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    closeDrawer()
                    navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                },
            )
        },
        modifier = modifier,
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        val canNavigateBack = currentRoute != null && currentRoute != Routes.HOME

        Box(modifier = Modifier.fillMaxHeight()) {
            // Top-level BackHandler: when the app is in fullscreen mode,
            // the first back press exits fullscreen (regardless of which
            // screen the user is on). The HomeScreen also has its own
            // BackHandler that takes priority on the home route — but on
            // other routes (section / settings), this top-level handler is
            // the only one active, so back-exits-fullscreen works there too.
            BackHandler(enabled = isFullscreen) {
                onToggleFullscreen()
            }

            Column(modifier = Modifier.fillMaxHeight()) {
                Header(
                    title = "Tahlil & Doa",
                    isDark = isDark,
                    themeMode = themeMode,
                    isFullscreen = isFullscreen,
                    onMenuClick = openDrawer,
                    onThemeToggleClick = onToggleThemeClick,
                    onToggleFullscreen = onToggleFullscreen,
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                    canNavigateBack = canNavigateBack,
                    onBackClick = { navController.popBackStack() },
                )

                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.weight(1f),
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            toggles = toggles,
                            style = style,
                            isDark = isDark,
                            onSectionClick = { id ->
                                navController.navigate(Routes.section(id)) { launchSingleTop = true }
                                onSetLastReadSection(id)
                            },
                            onActiveSectionChange = { id -> activeSectionId = id },
                            isFullscreen = isFullscreen,
                            onExitFullscreen = { onToggleFullscreen() },
                            lastReadSectionId = lastReadSectionId,
                            onContinueReading = { id ->
                                navController.navigate(Routes.section(id)) { launchSingleTop = true }
                                onSetLastReadSection(id)
                            },
                            onClearLastRead = onClearLastReadSection,
                        )
                    }
                    composable(
                        route = Routes.SECTION,
                        arguments = listOf(navArgument("sectionId") { type = NavType.StringType }),
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("sectionId").orEmpty()
                        SectionDetailScreen(
                            sectionId = id,
                            toggles = toggles,
                            style = style,
                            isDark = isDark,
                            onActiveSectionChange = { activeSectionId = it },
                        )
                        // Persist the last-read pointer whenever a section opens.
                        onSetLastReadSection(id)
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            toggles = toggles,
                            style = style,
                            themeMode = themeMode,
                            onTogglesChange = onTogglesChange,
                            onStyleChange = onStyleChange,
                            onThemeModeChange = onThemeModeChange,
                        )
                    }
                }
            }

            // Floating "exit fullscreen" button — only visible in fullscreen.
            // Pinned to the top-right corner with status-bar inset padding so
            // it doesn't sit under the (hidden) status bar.
            AnimatedVisibility(
                visible = isFullscreen,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 8.dp),
            ) {
                FloatingActionButton(
                    onClick = onToggleFullscreen,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Filled.FullscreenExit,
                        contentDescription = "Keluar dari mode layar penuh",
                    )
                }
            }
        }
    }
}

/**
 * Drawer (Table of Contents) — shows the app logo at the top, a "Home" /
 * "Settings" pair of shortcuts, then a vertically-scrollable list of all
 * 11 sections.
 *
 * Tapping a section navigates to [Routes.SECTION] for that id; the
 * active section is highlighted.
 */
@Composable
private fun DrawerContent(
    activeSectionId: String,
    onSectionClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
    ) {
        // ONE vertically-scrollable column wrapping the whole sheet content.
        //
        // Material3's ModalDrawerSheet does NOT scroll its content by itself:
        // its internal Column only applies DrawerDefaults.windowInsets
        // (status + navigation bars) and has NO verticalScroll (verified
        // against the material3 1.3.0 sources). Without this scroll the 11
        // "Daftar Isi" entries overflow on small screens or in landscape and
        // the bottom sections are simply unreachable.
        //
        // The sheet's own inset padding sits OUTSIDE this scroll, so while
        // scrolling nothing ever slides under the notification bar or the
        // Android system menu at the bottom.
        //
        // Deliberately a single `verticalScroll` — NOT a LazyColumn — so
        // there is never a nested-scrollable pair here (a historical
        // minified-release IncompatibleClassChangeError class of issue from
        // mixed Compose 1.6/1.7 bytecode). 11 items is well within Column's
        // comfort zone (no perf concern).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            // Header — logo + app name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.secondary,
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "ت",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Tahlil & Doa",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = "Majelis Tahlil & Doa",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Quick links
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                NavigationDrawerItem(
                    label = { Text("Beranda") },
                    selected = false,
                    onClick = onHomeClick,
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(),
                )
                NavigationDrawerItem(
                    label = { Text("Pengaturan") },
                    selected = false,
                    onClick = onSettingsClick,
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(),
                )
            }

            Spacer(Modifier.height(8.dp))

            // Section label
            Text(
                text = "Daftar Isi",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )

            // Section entries — plain Column + forEach. The whole sheet
            // content already lives in the single vertically-scrollable
            // Column above; a second (nested) scrollable here is exactly the
            // pattern we avoid. 11 items is well within Column's comfort
            // zone (no perf concern).
            val sections = remember { TahlilContent.sections }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                sections.forEach { section ->
                    val isActive = section.id == activeSectionId
                    NavigationDrawerItem(
                        label = {
                            Column {
                                Text(
                                    text = "${section.number}. ${section.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (!section.subtitle.isNullOrEmpty()) {
                                    Text(
                                        text = section.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                        selected = isActive,
                        onClick = { onSectionClick(section.id) },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = section.number.toString(),
                                    color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        ),
                    )
                }
            }
        }
    }
}
