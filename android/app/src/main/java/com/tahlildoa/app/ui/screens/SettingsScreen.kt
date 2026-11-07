package com.tahlildoa.app.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tahlildoa.app.data.ArabicAlign
import com.tahlildoa.app.data.ArabicStyleMode
import com.tahlildoa.app.data.ArabicStyleSettings
import com.tahlildoa.app.data.DisplayToggles
import com.tahlildoa.app.data.ThemeMode
import com.tahlildoa.app.ui.components.SliderRow
import com.tahlildoa.app.ui.components.ToggleRow
import com.tahlildoa.app.ui.components.contentMaxWidth

/**
 * SettingsScreen — all display & typography settings on one scrollable
 * screen, grouped into sections:
 *
 *  1. Tampilan (display toggles: show/hide arabic/translit/translation,
 *     plus the comma and colorize toggles)
 *  2. Tema (light/dark/system)
 *  3. Mode Font Arab (Standard / Quran / Naskhi)
 *  4. Tipografi Arab (sliders for size, line height, word spacing, letter
 *     spacing, alignment)
 */
@Composable
fun SettingsScreen(
    toggles: DisplayToggles,
    style: ArabicStyleSettings,
    themeMode: ThemeMode,
    onTogglesChange: (DisplayToggles) -> Unit,
    onStyleChange: (ArabicStyleSettings) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Bottom navigation-bar inset — the app is edge-to-edge, so without
    // this the last settings card (Tipografi Arab) would sit under the
    // Android system menu (gesture pill / 3-button bar). When the bars are
    // hidden (fullscreen) the inset reads 0.
    val density = LocalDensity.current
    val navBarBottomDp = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }

    // Centered + width-capped for tablet / desktop screen widths.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.contentMaxWidth().fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = navBarBottomDp + 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SectionTitle("Tampilan") }
            togglesSection(toggles, onTogglesChange)

            item { SectionTitle("Tema") }
            themeSection(themeMode, onThemeModeChange)

            item { SectionTitle("Mode Font Arab") }
            fontModeSection(style, onStyleChange)

            item { SectionTitle("Tipografi Arab") }
            typographySection(style, onStyleChange)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Box(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        content()
    }
}

private fun LazyListScope.togglesSection(
    toggles: DisplayToggles,
    onChange: (DisplayToggles) -> Unit,
) {
    item {
        SettingsCard {
            ToggleRow(
                title = "Teks Arab",
                subtitle = "Tampilkan teks Arab asli",
                checked = toggles.arabic,
                onCheckedChange = { v -> onChange(toggles.copy(arabic = v)) },
            )
            ToggleRow(
                title = "Transliterasi",
                subtitle = "Tampilkan teks Latin (transliterasi)",
                checked = toggles.transliteration,
                onCheckedChange = { v -> onChange(toggles.copy(transliteration = v)) },
            )
            ToggleRow(
                title = "Terjemahan",
                subtitle = "Tampilkan terjemahan Bahasa Indonesia",
                checked = toggles.translation,
                onCheckedChange = { v -> onChange(toggles.copy(translation = v)) },
            )
            ToggleRow(
                title = "Koma pada Teks Arab",
                subtitle = "Tambahkan tanda koma (،) sebelum kata penghubung وَ. Tidak berlaku untuk ayat Al-Qur'an.",
                checked = toggles.punctuateArabic,
                onCheckedChange = { v -> onChange(toggles.copy(punctuateArabic = v)) },
            )
            ToggleRow(
                title = "Koma = Baris Baru",
                subtitle = "Mulai baris baru pada setiap koma (،) dan titik (.) di teks Arab. Matikan agar teks mengalir sebagai satu paragraf.",
                checked = toggles.splitArabicLines,
                onCheckedChange = { v -> onChange(toggles.copy(splitArabicLines = v)) },
            )
            ToggleRow(
                title = "Warnai Kata Arab",
                subtitle = "Selang-seling warna untuk tiap kata Arab",
                checked = toggles.colorizeArabic,
                onCheckedChange = { v -> onChange(toggles.copy(colorizeArabic = v)) },
            )
            ToggleRow(
                title = "Layar Tetap Hidup",
                subtitle = "Cegah layar dari mode tidur selama aplikasi terbuka",
                checked = toggles.keepScreenOn,
                onCheckedChange = { v -> onChange(toggles.copy(keepScreenOn = v)) },
            )
        }
    }
}

private fun LazyListScope.themeSection(
    themeMode: ThemeMode,
    onChange: (ThemeMode) -> Unit,
) {
    item {
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeChip("Sistem", themeMode == ThemeMode.System) { onChange(ThemeMode.System) }
                ThemeChip("Terang", themeMode == ThemeMode.Light) { onChange(ThemeMode.Light) }
                ThemeChip("Gelap", themeMode == ThemeMode.Dark) { onChange(ThemeMode.Dark) }
            }
        }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            selectedLabelColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

private fun LazyListScope.fontModeSection(
    style: ArabicStyleSettings,
    onChange: (ArabicStyleSettings) -> Unit,
) {
    item {
        SettingsCard {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ArabicStyleMode.values().forEach { mode ->
                    FontModeRow(
                        mode = mode,
                        selected = style.mode == mode,
                        onClick = {
                            // When switching to Naskhi for the first time,
                            // apply the suggested defaults.
                            val nextStyle = if (mode == ArabicStyleMode.Naskhi) {
                                ArabicStyleSettings.NaskhiSuggested
                            } else {
                                style.copy(mode = mode)
                            }
                            onChange(nextStyle)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FontModeRow(
    mode: ArabicStyleMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(mode.label) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                selectedLabelColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
    Spacer(Modifier.height(4.dp))
    Text(
        text = mode.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 0.dp, bottom = 8.dp),
    )
}

private fun LazyListScope.typographySection(
    style: ArabicStyleSettings,
    onChange: (ArabicStyleSettings) -> Unit,
) {
    item {
        SettingsCard {
            SliderRow(
                title = "Ukuran Font",
                value = style.fontSize,
                onValueChange = { v -> onChange(style.copy(fontSize = v)) },
                valueRange = ArabicStyleSettings.FONT_SIZE_RANGE,
                valueFormat = { String.format("%.2f rem", it) },
            )
            SliderRow(
                title = "Tinggi Baris",
                value = style.lineHeight,
                onValueChange = { v -> onChange(style.copy(lineHeight = v)) },
                valueRange = ArabicStyleSettings.LINE_HEIGHT_RANGE,
                valueFormat = { String.format("%.2f", it) },
            )
            SliderRow(
                title = "Spasi Kata",
                value = style.wordSpacing,
                onValueChange = { v -> onChange(style.copy(wordSpacing = v)) },
                valueRange = ArabicStyleSettings.WORD_SPACING_RANGE,
                valueFormat = { String.format("%.2f em", it) },
            )
            SliderRow(
                title = "Spasi Huruf",
                value = style.letterSpacing,
                onValueChange = { v -> onChange(style.copy(letterSpacing = v)) },
                valueRange = ArabicStyleSettings.LETTER_SPACING_RANGE,
                valueFormat = { String.format("%.3f em", it) },
            )
            // Alignment selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Perataan:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FilterChip(
                    selected = style.align == ArabicAlign.Right,
                    onClick = { onChange(style.copy(align = ArabicAlign.Right)) },
                    label = { Text(ArabicAlign.Right.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                FilterChip(
                    selected = style.align == ArabicAlign.Justify,
                    onClick = { onChange(style.copy(align = ArabicAlign.Justify)) },
                    label = { Text(ArabicAlign.Justify.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }
    }
}
