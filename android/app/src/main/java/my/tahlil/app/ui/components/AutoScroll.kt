package my.tahlil.app.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameNanos

/**
 * Auto-scroll engine for LazyColumn-based reading screens (HomeScreen,
 * SectionDetailScreen).
 *
 * While [active] is true and [paused] is false, the list scrolls downward
 * continuously at [speedMultiplier] × [BASE_DP_PER_SECOND] using the frame
 * clock — the speed stays identical across devices because the distance is
 * computed from the elapsed frame time.
 *
 * **Section pause**: when [pauseSecPerSection] > 0, the engine holds the
 * scroll for that many seconds every time the first visible item index
 * ADVANCES (a new section card scrolls into view), then resumes
 * automatically. Backwards scrolls and identical indices never trigger a
 * hold.
 *
 * **End stop**: once the list can no longer scroll forward
 * (`canScrollForward == false`), [onReachEnd] is invoked ONCE and the loop
 * exits — the caller typically turns the feature off so the FAB flips back
 * to its "start" icon.
 *
 * The effect restarts cleanly whenever [active], [paused],
 * [speedMultiplier] or [pauseSecPerSection] change; cancelling the effect
 * mid-frame is safe because [scrollBy] is a suspend call.
 */
@Composable
fun AutoScrollEngine(
    state: LazyListState,
    active: Boolean,
    paused: Boolean,
    speedMultiplier: Float,
    pauseSecPerSection: Int,
    onReachEnd: () -> Unit = {},
) {
    val density = LocalDensity.current
    LaunchedEffect(active, paused, speedMultiplier, pauseSecPerSection) {
        if (!active || paused) return@LaunchedEffect
        val pxPerSecond = with(density) { (BASE_DP_PER_SECOND * speedMultiplier).toPx() }
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
            // Scroller: one scrollBy per frame, delta derived from elapsed
            // nanoseconds so the pace is frame-rate independent.
            var lastFrame = withFrameNanos { it }
            while (isActive) {
                val now = withFrameNanos { it }
                val dt = ((now - lastFrame).coerceAtLeast(0)) / 1_000_000_000f
                lastFrame = now
                if (System.currentTimeMillis() < holdUntil) continue
                state.scrollBy(pxPerSecond * dt)
                if (!state.canScrollForward) {
                    onReachEnd()
                    break
                }
            }
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

/** Base pace of the auto-scroll at speed multiplier 1.0 (dp per second). */
private const val BASE_DP_PER_SECOND = 56f

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
