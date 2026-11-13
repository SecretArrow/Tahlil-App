package my.tahlil.app.ui.screens

import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.tahlil.app.BuildConfig
import my.tahlil.app.data.HeroInfo
import my.tahlil.app.data.TahlilContent
import my.tahlil.app.ui.components.AutoScrollEngine
import my.tahlil.app.ui.components.SectionCard
import my.tahlil.app.ui.components.autoScrollTapControl
import my.tahlil.app.ui.components.contentMaxWidth
import my.tahlil.app.ui.components.rememberAutoScrollSession
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** CTA button definition for the hero. */
private data class CtaButton(
    val sectionId: String,
    val label: String,
    val primary: Boolean,
)

private val CtaButtons = listOf(
    CtaButton("pembukaan", "Mulai", primary = true),
    CtaButton("tahlil-ahli-kubur", "Tahlil", primary = false),
    CtaButton("dzikir", "Dzikir", primary = false),
    CtaButton("doa-pembuka", "Doa", primary = false),
)

/**
 * HomeScreen — hero + scroll list of all 11 sections.
 *
 * Each section is wrapped in a `key`-anchored item so that active-section
 * tracking via [LazyListState] works (we read the first visible item index).
 *
 * The hero is the first item in the LazyColumn (so it scrolls with the
 * list, matching the web app's behavior).
 *
 * Features:
 *  - **BackHandler**: pressing the back button on the home screen shows a
 *    confirmation dialog ("Keluar dari aplikasi?") with Batal / Keluar
 *    buttons. If the app is currently in fullscreen mode, the first back
 *    press exits fullscreen instead of showing the dialog.
 *  - **Reading progress bar**: a thin emerald `LinearProgressIndicator`
 *    rendered on top of the screen tracks the scroll position of the
 *    LazyColumn.
 *  - **Back-to-top FAB**: appears once the user scrolls past ~2 viewports
 *    and animates the LazyColumn back to the top on tap.
 *  - **Continue reading card**: when [lastReadSectionId] is set, a small
 *    card is inserted above the hero offering to jump straight back to
 *    the last section the user opened. A "Mulai dari awal" affordance
 *    clears the pointer.
 *
 * @param onSectionClick Called when a CTA button or "more" link is tapped.
 * @param onActiveSectionChange Called when the first visible section changes
 *        (used by the drawer to highlight the active entry).
 * @param isFullscreen The current immersive-mode flag — when true, the back
 *        button exits fullscreen first instead of showing the exit dialog.
 * @param onExitFullscreen Called to leave fullscreen mode.
 * @param lastReadSectionId The id of the section the user last opened (or null).
 * @param onContinueReading Called when the user taps the continue-reading card.
 * @param onClearLastRead Called when the user taps "Mulai dari awal".
 */
@Composable
fun HomeScreen(
    toggles: my.tahlil.app.data.DisplayToggles,
    style: my.tahlil.app.data.ArabicStyleSettings,
    isDark: Boolean,
    onSectionClick: (String) -> Unit,
    onActiveSectionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isFullscreen: Boolean = false,
    onExitFullscreen: () -> Unit = {},
    lastReadSectionId: String? = null,
    onContinueReading: (String) -> Unit = {},
    onClearLastRead: () -> Unit = {},
) {
    val state = rememberLazyListState()
    val sections = remember { TahlilContent.sections }
    val scope = rememberCoroutineScope()

    // --- Auto scroll (Gulir Otomatis) --------------------------------------
    // Start/stop via the bottom-start FAB; tap or long-press anywhere on the
    // list pauses; tapping again resumes. Speed & per-section pause come
    // from the user's display settings (SettingsScreen → Gulir Otomatis).
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

    // Track the first visible section's id (for drawer highlighting).
    LaunchedEffect(state) {
        snapshotFlow {
            val firstVisible = state.firstVisibleItemIndex
            // The hero takes index 0; sections start at index 1.
            if (firstVisible <= 0) sections.firstOrNull()?.id
            else sections.getOrNull(firstVisible - 1)?.id
        }.distinctUntilChanged().collect { id ->
            id?.let { onActiveSectionChange(it) }
        }
    }

    // --- Exit dialog state ------------------------------------------------
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler(enabled = true) {
        if (isFullscreen) {
            // First back press exits fullscreen; subsequent press shows dialog.
            onExitFullscreen()
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Keluar dari aplikasi?") },
            text = { Text("Apakah Anda yakin ingin keluar dari Tahlil & Doa?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    // Finish the host activity — closes the app to the home
                    // screen (matches the system "back" gesture).
                    (context as? Activity)?.finish()
                }) { Text("Keluar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Batal") }
            },
        )
    }

    // --- Reading progress (scroll-based) ----------------------------------
    val progress by remember {
        derivedStateOf {
            val total = state.layoutInfo.totalItemsCount
            if (total == 0) 0f
            else {
                val first = state.firstVisibleItemIndex.toFloat()
                // Cap at 1f — once the user reaches the last item, the bar
                // is full even if the offset scrolls past it.
                (first / (total - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
            }
        }
    }

    // --- Show back-to-top FAB once the user scrolls past ~2 viewports ------
    val showBackToTop by remember {
        derivedStateOf {
            // Show when firstVisibleItemIndex >= 2 OR when scrolled far
            // enough on the first item.
            state.firstVisibleItemIndex >= 2 ||
                (state.firstVisibleItemIndex == 1 &&
                    state.firstVisibleItemScrollOffset > 400)
        }
    }

    // --- Bottom system-bar inset ------------------------------------------
    // The app is edge-to-edge, so the bottom of the list (the footer) and
    // the back-to-top FAB would otherwise sit UNDER the Android navigation
    // bar (gesture pill or 3-button bar). The navigationBars inset is read
    // in BOTH modes: in fullscreen the bars are hidden and it reads 0, so
    // the content stays truly edge-to-edge — but if the bars are ever
    // visible again (transient swipe-reveal, an OEM ROM that ignores the
    // hide call, or the exit transition) the padding re-applies and neither
    // the list nor the FAB can ever collide with the system menu.
    val density = LocalDensity.current
    val navBarBottomDp = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }

    Box(modifier = modifier.fillMaxSize()) {
        // Card list capped at CONTENT_MAX_WIDTH and centered — on phones it
        // uses the full width; on tablets/desktop the list stays a comfortable
        // reading width instead of stretching edge to edge.
        LazyColumn(
            state = state,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .contentMaxWidth()
                .fillMaxSize()
                // Tap / long-press anywhere on the list toggles auto-scroll
                // pause (no-op while the feature is off).
                .autoScrollTapControl(autoScroll.active) { autoScroll.togglePaused() },
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding() + navBarBottomDp + 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Continue reading card (only if a last-read section exists).
            val lastSection = lastReadSectionId?.let { id ->
                sections.firstOrNull { it.id == id }
            }
            if (lastSection != null) {
                item(key = "continue_reading") {
                    ContinueReadingCard(
                        sectionTitle = lastSection.title,
                        sectionNumber = lastSection.number,
                        onContinue = { onContinueReading(lastSection.id) },
                        onClear = onClearLastRead,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            item(key = "hero") {
                Hero(
                    onCtaClick = onSectionClick,
                )
            }
            items(items = sections, key = { it.id }) { section ->
                SectionCard(
                    section = section,
                    toggles = toggles,
                    style = style,
                    isDark = isDark,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item(key = "footer") {
                Footer()
            }
        }

        // Thin reading progress bar pinned to the top of the screen.
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        )

        // Back-to-top FAB
        AnimatedVisibility(
            visible = showBackToTop,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // Keep the FAB clear of the Android navigation bar in EVERY
                // mode — the inset reads 0 while the bars are hidden in
                // fullscreen, so it only takes space when the bar is visible.
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 16.dp),
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        state.scrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Kembali ke atas",
                )
            }
        }

        // Auto-scroll start/stop FAB (bottom-end, above the back-to-top FAB).
        // Icon: Pause while scrolling, PlayArrow when off or paused.
        // Rendered only while the user has enabled Gulir Otomatis in
        // Settings; otherwise the FAB is omitted entirely so it never
        // obscures the reading surface.
        //
        // Implementation note: we use Surface (not Material3
        // FloatingActionButton) because FloatingActionButton has a
        // built-in 16.dp contentPadding that, combined with .size(36.dp),
        // would leave only ~4dp of content area and clip the icon.
        // Surface lets us control the icon sizing directly while keeping
        // the FAB-like look (circular shape, elevation, ripple).
        if (toggles.autoScrollEnabled) {
            Surface(
                onClick = { autoScroll.toggleActive() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    // Sit ABOVE the back-to-top FAB (which is bottom = 16.dp
                    // + 56.dp height ≈ 72.dp from screen bottom).
                    .padding(end = 16.dp, bottom = 72.dp)
                    // Size 36.dp = ~70% of the default 56.dp FAB (matches
                    // the web's 36px button).
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

            // Paused hint — small pill above the FAB telling the user the
            // scroll is on hold and that tapping the screen resumes it.
            AnimatedVisibility(
                visible = autoScroll.active && autoScroll.paused,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 116.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shadowElevation = 4.dp,
                ) {
                    Text(
                        text = "Gulir dijeda — ketuk layar untuk lanjut",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

/**
 * Continue-reading card — appears above the hero when the user has a
 * last-read section saved. Tapping it navigates straight to that section;
 * the "Mulai dari awal" affordance clears the pointer.
 */
@Composable
private fun ContinueReadingCard(
    sectionTitle: String,
    sectionNumber: Int,
    onContinue: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        ),
        onClick = onContinue,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lanjutkan membaca",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$sectionNumber. $sectionTitle",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            TextButton(onClick = onClear) {
                Text(
                    text = "Mulai dari awal",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

/**
 * Hero — title, subtitle, description, Bismillah, and 4 CTA buttons.
 */
@Composable
private fun Hero(onCtaClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(modifier = modifier.fillMaxWidth()) {
        // Decorative background orbs
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(180.dp)
                .blur(80.dp)
                .background(primary.copy(alpha = 0.25f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 60.dp)
                .size(160.dp)
                .blur(80.dp)
                .background(secondary.copy(alpha = 0.2f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Decorative top icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(primary, tertiary, secondary))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "ت",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = HeroInfo.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            // Only render subtitle when non-empty — the hero can work
            // without one (e.g. when the title alone is sufficient).
            if (HeroInfo.subtitle.isNotEmpty()) {
                Text(
                    text = HeroInfo.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = primary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Bismillah
            Text(
                text = HeroInfo.arabicBismillah,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )

            Text(
                text = HeroInfo.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(20.dp))

            // CTA buttons (2x2 grid on small screens)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CtaButtons.take(2).forEach { btn ->
                    CtaChip(btn, onCtaClick, Modifier.weight(1f))
                }
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CtaButtons.drop(2).forEach { btn ->
                    CtaChip(btn, onCtaClick, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Sumber: ${HeroInfo.source}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun CtaChip(
    btn: CtaButton,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (btn.primary) {
        Button(
            onClick = { onClick(btn.sectionId) },
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(btn.label, fontWeight = FontWeight.SemiBold)
        }
    } else {
        OutlinedButton(
            onClick = { onClick(btn.sectionId) },
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(50),
        ) {
            Text(btn.label, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Footer shown at the bottom of the home list and the section reading view.
 * Internal (not private) so [SectionDetailScreen] can reuse it as an
 * end-cap. The version line reads the real versionName from BuildConfig so
 * it always matches the release instead of drifting like the old hardcoded
 * "v1.0.0".
 */
@Composable
internal fun Footer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.primary,
                            Color.Transparent,
                        )
                    )
                )
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Tahlil & Doa",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        )
    }
}
