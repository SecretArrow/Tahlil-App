package my.tahlil.app.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameNanos
import my.tahlil.app.data.AutoScrollMode
import my.tahlil.app.data.SettingsRepository

/**
 * Auto-scroll engine for LazyColumn-based reading screens (HomeScreen,
 * SectionDetailScreen).
 *
 * Supports two modes, mirroring the web app:
 *  - [AutoScrollMode.Continuous]: smooth pixel-by-pixel scroll using the
 *    frame clock. While [pauseSecPerSection] > 0, the engine holds the
 *    scroll for that many seconds every time the first visible item index
 *    ADVANCES (a new section card scrolls into view), then resumes
 *    automatically.
 *  - [AutoScrollMode.Block]: jumps one section card at a time using
 *    `animateScrollToItem(firstVisibleItemIndex + 1)` with a delay derived
 *    from [speedMultiplier] between jumps. The section-pause setting is
 *    ignored in this mode (each jump already pauses).
 *
 * While [active] is true and [paused] is false, the engine drives the list
 * forward; otherwise it is dormant.
 *
 * **End stop**: once the list can no longer scroll forward
 * (`canScrollForward == false`), [onReachEnd] is invoked ONCE and the loop
 * exits — the caller typically turns the feature off so the FAB flips back
 * to its "start" icon.
 *
 * The effect restarts cleanly whenever [active], [paused], [mode],
 * [speedMultiplier] or [pauseSecPerSection] change; cancelling the effect
 * mid-frame is safe because [scrollBy] and [animateScrollToItem] are both
 * suspend calls.
 *
 * **Speed formula** (mirrors the web so both platforms feel identical):
 *  - Continuous: px/sec = [BASE_SPEED_PX_PER_SEC] + speed ×
 *    [SPEED_FACTOR_PX_PER_SEC] (so 0.1 ≈ 14 px/s, 3 ≈ 66 px/s, 25 ≈ 462
 *    px/s).
 *  - Block: delayMs = clamp([BLOCK_BASE_MS] / speed, [BLOCK_MIN_MS],
 *    [BLOCK_MAX_MS]) (so 0.1 = 30s between jumps, 3 ≈ 2.3s, 25 = 700ms).
 */
@Composable
fun AutoScrollEngine(
    state: LazyListState,
    active: Boolean,
    paused: Boolean,
    mode: AutoScrollMode,
    speedMultiplier: Float,
    pauseSecPerSection: Int,
    onReachEnd: () -> Unit = {},
) {
    val density = LocalDensity.current
    LaunchedEffect(active, paused, mode, speedMultiplier, pauseSecPerSection) {
        if (!active || paused) return@LaunchedEffect
        when (mode) {
            AutoScrollMode.Continuous -> runContinuousScroll(
                state = state,
                speedMultiplier = speedMultiplier,
                pauseSecPerSection = pauseSecPerSection,
                densityFactor = density.density,
                onReachEnd = onReachEnd,
            )
            AutoScrollMode.Block -> runBlockScroll(
                state = state,
                speedMultiplier = speedMultiplier,
                onReachEnd = onReachEnd,
            )
        }
    }
}

/**
 * Continuous-mode scroll loop — pixel-by-pixel based on the frame clock.
 * Honors [pauseSecPerSection] by scheduling a hold whenever the first
 * visible item index advances.
 */
private suspend fun runContinuousScroll(
    state: LazyListState,
    speedMultiplier: Float,
    pauseSecPerSection: Int,
    densityFactor: Float,
    onReachEnd: () -> Unit,
) {
    // px/sec = BASE + speed * FACTOR — same formula as the web so the
    // pace feels identical across platforms. Multiply by densityFactor to
    // convert the dp-like value to actual screen pixels.
    val pxPerSecond = (BASE_SPEED_PX_PER_SEC + speedMultiplier * SPEED_FACTOR_PX_PER_SEC) *
        densityFactor
    var holdUntil = 0L
    var lastSeenIndex = state.firstVisibleItemIndex
    coroutineScope {
        // Watcher: advance of the first-visible item = a new section card
        // has scrolled into view → schedule a hold (if configured).
        launch {
            snapshotFlow { state.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect { idx ->
                    if (idx > lastSeenIndex && pauseSecPerSection > 0) {
                        holdUntil =
                            System.currentTimeMillis() + pauseSecPerSection * 1_000L
                    }
                    lastSeenIndex = idx
                }
        }
        // Scroller: canonical per-frame scroll session — ScrollScope.scrollBy
        // is a member (no extension import), delta derived from elapsed
        // nanoseconds so the pace is frame-rate independent.
        state.scroll {
            var lastFrame = withFrameNanos { it }
            while (isActive) {
                val now = withFrameNanos { it }
                val dt = ((now - lastFrame).coerceAtLeast(0L)) / 1_000_000_000f
                lastFrame = now
                if (System.currentTimeMillis() >= holdUntil) {
                    scrollBy(pxPerSecond * dt)
                }
                if (!state.canScrollForward) {
                    onReachEnd()
                    break
                }
            }
        }
    }
}

/**
 * Block-mode scroll loop — animate one section card at a time, pause
 * proportional to [speedMultiplier], repeat until the last item.
 *
 * delayMs = clamp(BLOCK_BASE_MS / speed, BLOCK_MIN_MS, BLOCK_MAX_MS).
 */
private suspend fun runBlockScroll(
    state: LazyListState,
    speedMultiplier: Float,
    onReachEnd: () -> Unit,
) {
    // Guard against speed = 0 (legacy/foreign value that slipped past the
    // repository clamp): fall back to the base delay so we never divide by
    // zero.
    val safeSpeed = if (speedMultiplier > 0f) speedMultiplier else 1f
    val delayMs = (BLOCK_BASE_MS / safeSpeed).coerceIn(BLOCK_MIN_MS, BLOCK_MAX_MS).toLong()
    // Small initial delay so the user can settle before the first jump
    // (mirrors the web's 300ms initial pause).
    delay(300L)
    coroutineScope {
        while (isActive) {
            val target = state.firstVisibleItemIndex + 1
            if (target >= state.layoutInfo.totalItemsCount) {
                onReachEnd()
                break
            }
            state.animateScrollToItem(target)
            // After landing on the last reachable item, give one final
            // pause then stop — same as the web's "reached last block".
            if (!state.canScrollForward) {
                delay(delayMs)
                onReachEnd()
                break
            }
            delay(delayMs)
        }
    }
}

/**
 * Tap / long-press control for the auto-scroll feature, per user request:
 *
 * > "jika layar di ketuk 1x atau ditahan maka dianggap pause lalu jika di
 * > ketuk lagi maka lanjut auto scroll"
 *
 * While [autoScrollActive] is true, ANY tap or long-press on the surface
 * this modifier is attached to toggles pause/resume. Child composables that
 * consume clicks themselves (CTA buttons, FABs) still receive their events —
 * the parent [detectTapGestures] simply runs afterwards; pausing as a side
 * effect is acceptable and matches the "tap anywhere pauses" requirement.
 *
 * When the feature is inactive the modifier is a no-op (taps do nothing),
 * so normal reading interaction is untouched.
 */
fun Modifier.autoScrollTapControl(
    autoScrollActive: Boolean,
    onTogglePause: () -> Unit,
): Modifier = pointerInput(autoScrollActive) {
    detectTapGestures(
        onTap = { if (autoScrollActive) onTogglePause() },
        onLongPress = { if (autoScrollActive) onTogglePause() },
    )
}

/**
 * Continuous-mode speed formula constants — mirror the web's
 * `BASE_SPEED_PX_PER_SEC = 12` and `SPEED_FACTOR_PX_PER_SEC = 18` so the
 * pace feels identical across platforms.
 */
private const val BASE_SPEED_PX_PER_SEC = 12f
private const val SPEED_FACTOR_PX_PER_SEC = 18f

/** Block-mode delay formula constants — mirror the web. */
private const val BLOCK_BASE_MS = 7000f
private const val BLOCK_MIN_MS = 700f
private const val BLOCK_MAX_MS = 30000f

/** Remember helper bundling the two session flags used by the screens. */
class AutoScrollSessionState {
    // Snapshot state — reads in composition are tracked, so mutating these
    // flags recomposes the FAB icon and restarts the engine effect.
    var active: Boolean by mutableStateOf(false)
    var paused: Boolean by mutableStateOf(false)

    /** FAB icon state — pause icon only while actually scrolling. */
    val scrolling: Boolean get() = active && !paused

    fun toggleActive() {
        if (active) {
            active = false
            paused = false
        } else {
            active = true
            paused = false
        }
    }

    fun togglePaused() {
        if (active) paused = !paused
    }

    fun stop() {
        active = false
        paused = false
    }
}

/** Remember an [AutoScrollSessionState] for the current screen. */
@Composable
fun rememberAutoScrollSession(): AutoScrollSessionState = remember { AutoScrollSessionState() }

// ── Draggable auto-scroll FAB ──────────────────────────────────────────────

/** FAB diameter — 36.dp, ~70% of the standard 56.dp FAB (matches the web's 36px button). */
private val AutoScrollFabSize = 36.dp

/**
 * Default horizontal gap from the screen's end edge — centers the 36dp FAB
 * with the 56dp back-to-top FAB below it ("lurus center dengan tombol
 * kembali ke awal"): the back-to-top FAB's center sits at 16dp + 28dp =
 * 44dp from the edge; here 26dp + 18dp = 44dp, so the two align exactly.
 */
private val FabEndPadding = 26.dp

/**
 * Default gap above the navigation-bar inset — clear breathing room above
 * the back-to-top FAB on HomeScreen (its top edge sits at 16dp + 56dp =
 * 72dp; 88dp here leaves a 16dp gap so the two never touch — per user
 * request "kurang keatas sedikit" and "jangan terlalu mepet dengannya").
 */
private val FabBottomPadding = 88.dp

/** Guard margin the FAB keeps from the screen edges when dragged. */
private val FabScreenMargin = 8.dp

/** Orange FAB — per user request the start/pause control is orange in
 *  BOTH states ("warna icon start stop/pause pakai orange saja"); the
 *  Play vs Pause icon tells the running state apart.
 *  Light theme: orange-500 #F97316, dark theme: orange-600 #EA580C. */
private val FabOrangeLight = Color(0xFFF97316)
private val FabOrangeDark = Color(0xFFEA580C)

/**
 * Shared, DRAGGABLE auto-scroll start/pause FAB (port of the draggable FAB
 * from the web's `auto-scroll.tsx`).
 *
 * Look & wiring: a 36dp circular [Surface] — ORANGE in both states per
 * user request ("warna icon start stop/pause pakai orange saja"), showing
 * Pause while scrolling and PlayArrow otherwise. The start/pause logic
 * stays with the caller via [onClick]; the session state
 * ([rememberAutoScrollSession]/[AutoScrollEngine]) is untouched.
 *
 * Dragging: the outer Box consumes drag gestures (`detectDragGestures`,
 * which only fires after touch slop) and offsets the FAB from its default
 * bottom-end corner (end 26.dp, bottom 88.dp — centered above the
 * back-to-top FAB). The offsets are clamped so the FAB never leaves the
 * screen:
 *  - horizontal ∈ [-(screenW − fab − 26 − 8) .. 0]
 *  - vertical   ∈ [-(screenH − fab − 88 − 64 − 8) .. +(88 − 26).toPx()]
 *    (down to just above the nav-bar zone).
 *
 * Position persistence: the offsets are PERSISTED to DataStore via
 * [SettingsRepository.setAutoScrollFabOffset] on every drag end and
 * restored once on composition — the user's chosen position survives app
 * restarts (mirrors the web's tahlil-autoscroll-fab-pos cookie —
 * "posisinya tersimpan jika di pindah2 pengguna").
 *
 * Tap vs drag: `detectDragGestures` consumes position changes only after
 * touch slop, so a plain tap still reaches the Surface's onClick (and a
 * [hasDragged] guard suppresses the click when a real drag occurred,
 * regardless of event-dispatch order). While paused, the screens' hint
 * pill stays at the FAB's default corner — it does NOT follow the drag.
 *
 * Equivalent of the web's "touchmove pauses, except when it originates on
 * the FAB": gestures that begin on the FAB are fully consumed here and
 * never reach the reading surface's tap-to-pause detector
 * ([autoScrollTapControl]).
 */
@Composable
fun AutoScrollFab(
    scrolling: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Drag offsets (px) from the default bottom-end corner. PERSISTED via
    // DataStore below so the user's chosen position survives app restarts
    // (mirrors the web's cookie) — "posisinya tersimpan jika di pindah2
    // pengguna".
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    // True once an actual drag (touch slop passed) moved the FAB in the
    // current gesture — lets the click handler distinguish a tap (start /
    // pause) from a drag (reposition).
    var hasDragged by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Durable position persistence — load once on composition, save on
    // every drag end. Stored in raw screen px and clamped by the bounds
    // below, so a stored offset from a different screen size can never
    // push the FAB off-screen.
    val context = LocalContext.current
    val settingsRepo = remember { SettingsRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(settingsRepo) {
        settingsRepo.autoScrollFabOffset.first()?.let { (x, y) ->
            offsetX = x
            offsetY = y
        }
    }

    // Drag bounds (px), recomputed whenever screen size / density changes
    // (pointerInput below is keyed on them, so a rotation re-clamps). The
    // coerceAtMost guards keep the range valid on absurdly small screens.
    val minOffsetX = with(density) {
        -(configuration.screenWidthDp.dp - AutoScrollFabSize - FabEndPadding - FabScreenMargin).toPx()
    }.coerceAtMost(0f)
    val minOffsetY = with(density) {
        -(configuration.screenHeightDp.dp - AutoScrollFabSize - FabBottomPadding - 64.dp - FabScreenMargin)
            .toPx()
    }.coerceAtMost(0f)
    val maxOffsetY = with(density) {
        // Down to just above the nav-bar zone (72.dp − 16.dp below the base).
        (FabBottomPadding - FabEndPadding).toPx()
    }.coerceAtLeast(0f)

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(end = FabEndPadding, bottom = FabBottomPadding)
            .offset {
                IntOffset(
                    offsetX.coerceIn(minOffsetX, 0f).roundToInt(),
                    offsetY.coerceIn(minOffsetY, maxOffsetY).roundToInt(),
                )
            }
            .pointerInput(minOffsetX, minOffsetY, maxOffsetY) {
                detectDragGestures(
                    onDragEnd = {
                        hasDragged = false
                        // Persist so the position survives app restarts.
                        scope.launch {
                            settingsRepo.setAutoScrollFabOffset(offsetX, offsetY)
                        }
                    },
                    onDragCancel = { hasDragged = false },
                ) { change, dragAmount ->
                    change.consume()
                    hasDragged = true
                    offsetX = (offsetX + dragAmount.x).coerceIn(minOffsetX, 0f)
                    offsetY = (offsetY + dragAmount.y).coerceIn(minOffsetY, maxOffsetY)
                }
            },
    ) {
        Surface(
            onClick = { if (!hasDragged) onClick() },
            modifier = Modifier.size(AutoScrollFabSize),
            shape = CircleShape,
            // Orange in BOTH states per user request — the Play vs Pause
            // icon tells the running state apart.
            color = if (isSystemInDarkTheme()) FabOrangeDark else FabOrangeLight,
            contentColor = Color.White,
            shadowElevation = 6.dp,
            tonalElevation = 6.dp,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (scrolling) {
                        Icons.Filled.Pause
                    } else {
                        Icons.Filled.PlayArrow
                    },
                    contentDescription = contentDescription,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
