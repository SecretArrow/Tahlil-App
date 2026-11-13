package my.tahlil.app.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameNanos
import my.tahlil.app.data.AutoScrollMode

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
