package my.tahlil.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import my.tahlil.app.data.ArabicStyleSettings
import my.tahlil.app.data.DisplayToggles
import my.tahlil.app.data.TahlilContent
import my.tahlil.app.ui.components.AutoScrollEngine
import my.tahlil.app.ui.components.SectionCard
import my.tahlil.app.ui.components.autoScrollTapControl
import my.tahlil.app.ui.components.contentMaxWidth
import my.tahlil.app.ui.components.rememberAutoScrollSession
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * SectionDetailScreen — the reading view opened from a CTA button, the
 * drawer, or the "Lanjutkan membaca" (continue reading) card.
 *
 * The screen renders ALL sections — positioned initially at [sectionId] —
 * instead of only the target section. This way, after continuing their
 * reading, the user can still scroll UP to revisit earlier sections and
 * DOWN to keep reading the following ones (the whole list behaves like the
 * home list, minus the hero).
 *
 * The list starts exactly at the requested section:
 *  - [rememberLazyListState] is seeded with the target index so the first
 *    frame already shows the right card (no visible jump);
 *  - a [LaunchedEffect] on [sectionId] re-scrolls whenever the target
 *    changes while the screen is already visible — the drawer navigates
 *    with `launchSingleTop`, which UPDATES the arguments of the existing
 *    entry instead of pushing a new one, so seeding alone wouldn't fire.
 *
 * The first visible section is reported through [onActiveSectionChange]
 * while scrolling so the drawer's highlight follows the reading position
 * (same tracking approach as [HomeScreen]).
 */
@Composable
fun SectionDetailScreen(
    sectionId: String,
    toggles: DisplayToggles,
    style: ArabicStyleSettings,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onActiveSectionChange: (String) -> Unit = {},
) {
    val sections = remember { TahlilContent.sections }
    val startIndex = remember(sectionId, sections) {
        sections.indexOfFirst { it.id == sectionId }.coerceAtLeast(0)
    }
    val state = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    // Bottom navigation-bar inset — the app is edge-to-edge, so without this
    // the last cards would sit under the Android system menu (gesture pill
    // / 3-button bar). When the bars are hidden (fullscreen) the inset reads
    // 0 and the content stays truly edge-to-edge.
    val density = LocalDensity.current
    val navBarBottomDp = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }

    // Follow the reading position for the drawer's active-section highlight.
    LaunchedEffect(state, sections) {
        androidx.compose.runtime.snapshotFlow { state.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                sections.getOrNull(index)?.let { onActiveSectionChange(it.id) }
            }
    }

    // --- Auto scroll (Gulir Otomatis) --------------------------------------
    // Same engine & interaction as HomeScreen: start/stop via the
    // bottom-end FAB (rendered only while the user has enabled Gulir
    // Otomatis), tap/long-press anywhere pauses, tap again resumes.
    val autoScroll = rememberAutoScrollSession()
    AutoScrollEngine(
        state = state,
        active = autoScroll.active,
        paused = autoScroll.paused,
        mode = toggles.autoScrollMode,
        speedMultiplier = toggles.autoScrollSpeed,
        pauseSecPerSection = toggles.autoScrollSectionPauseSec,
        onReachEnd = { autoScroll.stop() },
    )

    // When the requested section changes while this screen is already on
    // screen (drawer navigation with launchSingleTop), jump to it.
    LaunchedEffect(sectionId) {
        state.scrollToItem(startIndex)
    }

    // Centered + width-capped so the cards keep a comfortable reading width
    // on tablets / desktop instead of stretching edge to edge.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .contentMaxWidth()
                .fillMaxSize()
                // Tap / long-press anywhere pauses auto-scroll (no-op off).
                .autoScrollTapControl(autoScroll.active) { autoScroll.togglePaused() },
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = contentPadding.calculateBottomPadding() + navBarBottomDp + 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = sections, key = { it.id }) { section ->
                SectionCard(
                    section = section,
                    toggles = toggles,
                    style = style,
                    isDark = isDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
            item(key = "footer") {
                Footer(modifier = Modifier.fillMaxWidth())
            }
        }

        // Auto-scroll start/stop FAB (bottom-end, above where the back-to-
        // top FAB would be on HomeScreen — but this screen has none, so we
        // still keep the same vertical offset for cross-screen parity).
        // Rendered only while the user has enabled Gulir Otomatis in
        // Settings; otherwise the FAB is omitted entirely so it never
        // obscures the reading surface.
        //
        // Implementation note: we use Surface (not Material3
        // FloatingActionButton) because FloatingActionButton has a built-in
        // 16.dp contentPadding that, combined with .size(36.dp), would
        // leave only ~4dp of content area and clip the icon. Surface lets
        // us control the icon sizing directly while keeping the FAB-like
        // look (circular shape, elevation, ripple).
        if (toggles.autoScrollEnabled) {
            Surface(
                onClick = { autoScroll.toggleActive() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 72.dp)
                    // Size 36.dp = ~70% of the default 56.dp FAB.
                    .size(36.dp),
                shape = CircleShape,
                color = if (autoScroll.active) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (autoScroll.active) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                shadowElevation = 6.dp,
                tonalElevation = 6.dp,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (autoScroll.scrolling) {
                            Icons.Filled.Pause
                        } else {
                            Icons.Filled.PlayArrow
                        },
                        contentDescription = if (autoScroll.active) {
                            "Hentikan gulir otomatis"
                        } else {
                            "Mulai gulir otomatis"
                        },
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
