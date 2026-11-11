package my.tahlil.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.tahlil.app.data.ArabicStyleSettings
import my.tahlil.app.data.ContentBlock
import my.tahlil.app.data.DisplayToggles
import my.tahlil.app.data.Section
import my.tahlil.app.data.SectionType

/** Accent colors per section type — mirrors the web's `typeConfig`. */
private fun SectionType.accentColors(): Pair<Color, Color> = when (this) {
    SectionType.Pembukaan -> Color(0xFF10B981) to Color(0xFF0D9488)  // emerald → teal
    SectionType.Sholawat -> Color(0xFF14B8A6) to Color(0xFF0D9488)  // teal
    SectionType.Surah -> Color(0xFFF59E0B) to Color(0xFFD97706)  // amber
    SectionType.Tahlil -> Color(0xFF10B981) to Color(0xFF0D9488)
    SectionType.Ayat -> Color(0xFFEAB308) to Color(0xFFCA8A04)  // yellow
    SectionType.Doa -> Color(0xFFF43F5E) to Color(0xFFE11D48)  // rose
    SectionType.Dzikir -> Color(0xFF8B5CF6) to Color(0xFF7C3AED)  // violet
}

/** A small circular badge with the section number, gradient-filled. */
@Composable
private fun SectionNumberBadge(
    number: Int,
    type: SectionType,
    modifier: Modifier = Modifier,
) {
    val (start, end) = type.accentColors()
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(start, end))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
    }
}

/** The colored pill showing the section type label. */
@Composable
private fun TypePill(type: SectionType, modifier: Modifier = Modifier) {
    val (start, end) = type.accentColors()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(start.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = type.label,
            color = start,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** Ornamental divider between header and body. */
@Composable
private fun ArabicOrnament(modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Spacer(
            Modifier
                .width(20.dp)
                .height(1.dp)
                .background(accent),
        )
        Box(
            Modifier
                .size(5.dp)
                .clip(RoundedCornerShape(50))
                .background(accent),
        )
        Spacer(
            Modifier
                .width(8.dp)
                .height(1.dp)
                .background(accent),
        )
        Box(
            Modifier
                .size(5.dp)
                .clip(RoundedCornerShape(50))
                .background(accent),
        )
        Spacer(
            Modifier
                .width(20.dp)
                .height(1.dp)
                .background(accent),
        )
    }
}

/**
 * A single block within a section — Arabic verse + transliteration + translation.
 *
 * Honors the [DisplayToggles] for showing/hiding each part. If all three
 * are hidden, shows a subtle hint message instead.
 */
@Composable
private fun BlockItem(
    block: ContentBlock,
    toggles: DisplayToggles,
    style: ArabicStyleSettings,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val allHidden = !toggles.arabic && !toggles.transliteration && !toggles.translation
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // --- Arabic verse ------------------------------------------------
        // Own container: gradient box, auto-height (no fixed sizes — grows
        // with the number of wrapped lines). Horizontal padding keeps the
        // text clear of the card edges.
        if (!block.arabic.isNullOrEmpty() && toggles.arabic) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                ArabicText(
                    text = block.arabic,
                    style = style,
                    toggles = toggles,
                    isDark = isDark,
                    modifier = Modifier.fillMaxWidth(),
                    suffix = if (block.repeat != null && block.repeat > 1) {
                        { RepeatBadge(count = block.repeat) }
                    } else null,
                )
            }
        }

        // --- Transliteration (Latin) ------------------------------------
        if (toggles.transliteration) {
            Text(
                text = block.transliteration,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 22.sp,
                ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
            )
        }

        // --- Translation (Indonesian) -----------------------------------
        if (toggles.translation) {
            Text(
                text = block.translation,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
        }

        // --- Hint if all hidden -----------------------------------------
        if (allHidden) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "Semua tampilan disembunyikan. Aktifkan minimal satu di tombol Tampilan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // --- Note --------------------------------------------------------
        if (!block.note.isNullOrEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "◆",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = block.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * SectionCard — renders one [Section] (header + blocks).
 *
 * Card treatment mirrors the web (`rounded-3xl border bg-card shadow-sm`):
 * a rounded surface with a 1dp hairline border for definition. The old
 * implementation applied `Modifier.shadow(...)` AFTER `.background(...)`,
 * which made the shadow wrap only the inner content — some devices then
 * rendered that shadow ON TOP of the card background, producing a gray
 * ring that looked like a square "backing card" sticking out beyond the
 * rounded card. The shadow is therefore gone entirely; the border gives
 * the same crisp, professional edge on both themes without any shadow
 * rendering quirks.
 *
 * Set [animate] to false on the home screen to keep things simple (the
 * LazyColumn already handles entrance animations).
 */
@Composable
fun SectionCard(
    section: Section,
    toggles: DisplayToggles,
    style: ArabicStyleSettings,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
    ) {
        // Top accent gradient strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.secondary,
                        )
                    )
                )
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            // Header: [number badge] [title + subtitle group • weight(1f)]
            // The badge and the text group are vertically centered against
            // each other; the type pill sits at the row's end and can never
            // collide with the title (title is weight-constrained + wraps).
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionNumberBadge(number = section.number, type = section.type)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        TypePill(type = section.type)
                    }
                    if (!section.subtitle.isNullOrEmpty()) {
                        Text(
                            text = section.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Ornamental divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                ArabicOrnament()
            }

            Spacer(Modifier.height(12.dp))

            // Blocks
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                section.blocks.forEachIndexed { i, block ->
                    BlockItem(
                        block = block,
                        toggles = toggles,
                        style = style,
                        isDark = isDark,
                    )
                }
            }
        }
    }
}
