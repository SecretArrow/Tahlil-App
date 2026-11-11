package my.tahlil.app.ui.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Maximum readable content width.
 *
 * On phones the content uses the full screen width (balanced by the caller's
 * horizontal padding). On tablets / desktop-width screens the card list is
 * centered and capped at this width so cards never stretch uncomfortably
 * wide — mirroring a `max-width + margin:auto` layout on the web.
 *
 * IMPORTANT: apply BEFORE `fillMaxSize()` in the modifier chain:
 *   Modifier.contentMaxWidth().fillMaxSize()   // correct
 *   Modifier.fillMaxSize().contentMaxWidth()   // NO-OP — width already filled
 */
val CONTENT_MAX_WIDTH: Dp = 640.dp

fun Modifier.contentMaxWidth(): Modifier = this.widthIn(max = CONTENT_MAX_WIDTH)
