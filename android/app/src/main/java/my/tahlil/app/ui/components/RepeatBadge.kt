package my.tahlil.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Small circular badge showing a repetition count for blocks with
 * `repeat > 1` (e.g. "×3" for Surat Al-Ikhlas which is read 3 times, or
 * "×11" for "Laa ilaaha illallaah" in the dzikir section).
 *
 * Matches the web app's `×N` suffix rendered in primary color next to
 * the verse.
 */
@Composable
fun RepeatBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    if (count <= 1) return
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary

    Row(
        modifier = modifier
            .background(accent, CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "×",
            color = onAccent,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            ),
        )
        Text(
            text = count.toString(),
            color = onAccent,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            ),
        )
    }
}
