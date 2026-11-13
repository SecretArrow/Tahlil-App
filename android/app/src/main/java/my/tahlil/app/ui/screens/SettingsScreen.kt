package my.tahlil.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import my.tahlil.app.data.ArabicAlign
import my.tahlil.app.data.ArabicStyleMode
import my.tahlil.app.data.ArabicStyleSettings
import my.tahlil.app.data.AutoScrollMode
import my.tahlil.app.data.DisplayToggles
import my.tahlil.app.data.ThemeMode
import my.tahlil.app.ui.components.ArabicText
import my.tahlil.app.ui.components.SliderRow
import my.tahlil.app.ui.components.ToggleRow
import my.tahlil.app.ui.components.contentMaxWidth
import my.tahlil.app.ui.components.hamzaWashalColorFor
import my.tahlil.app.ui.components.waqafColorFor
import my.tahlil.app.util.HamzaWashalUtil
import my.tahlil.app.util.TajwidUtil
import my.tahlil.app.util.WaqafUtil

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
 *  5. Teks Latin & Terjemahan (line spacing for the transliteration +
 *     translation body text)
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

    // Resolve the effective dark flag for theme-aware widgets (Tajwid
    // legend swatches) — same resolution logic as MainActivity.
    val isDark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

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
            item(key = "title_tampilan", contentType = { "title" }) { SectionTitle("Tampilan") }
            togglesSection(toggles, style, onTogglesChange, isDark)

            item(key = "title_autoscroll", contentType = { "title" }) { SectionTitle("Gulir Otomatis") }
            autoScrollSection(toggles, onTogglesChange)

            item(key = "title_tema", contentType = { "title" }) { SectionTitle("Tema") }
            themeSection(themeMode, onThemeModeChange)

            item(key = "title_fontmode", contentType = { "title" }) { SectionTitle("Mode Font Arab") }
            fontModeSection(style, onStyleChange)

            item(key = "title_tipografi", contentType = { "title" }) { SectionTitle("Tipografi Arab") }
            typographySection(style, onStyleChange)

            item(key = "title_perataan", contentType = { "title" }) { SectionTitle("Perataan Teks Arab") }
            alignmentSection(style, onStyleChange)
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

/**
 * Auto-scroll (Gulir Otomatis) settings:
 *  - "Aktifkan Gulir Otomatis" toggle — master switch. When off (default),
 *    no start/pause FAB is rendered on the reading screens.
 *  - "Mode Gulir" chips — Continuous (smooth pixel-by-pixel) vs Block
 *    (jumps one section card at a time).
 *  - "Kecepatan Gulir" slider — dimensionless 0.1–25 value. Higher = faster.
 *    In Continuous mode the engine derives px/sec from this; in Block mode
 *    it derives the delay between jumps.
 *  - "Jeda per Bagian" chips — only meaningful in Continuous mode, so
 *    hidden while Block is selected.
 *
 * The feature itself is started/stopped from the bottom-end FAB on the
 * reading screens; tap or long-press the screen to pause/resume.
 */
private fun LazyListScope.autoScrollSection(
    toggles: DisplayToggles,
    onChange: (DisplayToggles) -> Unit,
) {
    item(key = "autoscroll", contentType = { "settings_card" }) {
        SettingsCard {
            ToggleRow(
                title = "Aktifkan Gulir Otomatis",
                subtitle = "Tampilkan tombol mulai/jeda di layar baca",
                checked = toggles.autoScrollEnabled,
                onCheckedChange = { v -> onChange(toggles.copy(autoScrollEnabled = v)) },
            )

            // The mode picker + speed slider + pause chips only matter while
            // the feature is enabled — gate them so the card stays compact
            // when auto-scroll is off (mirrors the web).
            if (toggles.autoScrollEnabled) {
                // Mode picker — Continuous vs Block (FilterChip row).
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(
                        text = "Mode Gulir",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Terus-menerus: gulir halus. Per Blok: lompat satu bagian.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AutoScrollMode.values().forEach { mode ->
                            FilterChip(
                                selected = toggles.autoScrollMode == mode,
                                onClick = { onChange(toggles.copy(autoScrollMode = mode)) },
                                label = { Text(mode.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                        }
                    }
                }

                SliderRow(
                    title = "Kecepatan Gulir",
                    value = toggles.autoScrollSpeed,
                    onValueChange = { v ->
                        onChange(
                            toggles.copy(
                                autoScrollSpeed = v.coerceIn(
                                    DisplayToggles.AUTO_SCROLL_SPEED_RANGE.start,
                                    DisplayToggles.AUTO_SCROLL_SPEED_RANGE.endInclusive,
                                )
                            )
                        )
                    },
                    valueRange = DisplayToggles.AUTO_SCROLL_SPEED_RANGE,
                    valueFormat = { String.format("%.1f", it) },
                )

                // Jeda per bagian — segmented chips (Mati / 2 / 5 / 10 detik).
                // Only meaningful in Continuous mode (each Block jump already
                // pauses between sections), so hidden while Block is active.
                if (toggles.autoScrollMode == AutoScrollMode.Continuous) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(
                            text = "Jeda per Bagian",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Berhenti sesaat setiap masuk bagian baru",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            DisplayToggles.AUTO_SCROLL_PAUSE_CHOICES.forEach { sec ->
                                val label = if (sec == 0) "Mati" else "$sec dtk"
                                FilterChip(
                                    selected = toggles.autoScrollSectionPauseSec == sec,
                                    onClick = {
                                        onChange(toggles.copy(autoScrollSectionPauseSec = sec))
                                    },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
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
    style: ArabicStyleSettings,
    onChange: (DisplayToggles) -> Unit,
    isDark: Boolean,
) {
    item(key = "toggles", contentType = { "settings_card" }) {
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
                title = "Tajwid",
                subtitle = "Warnai hukum bacaan pada teks Arab: Izhar, Ikhfa, Idgham, Iqlab, Ghunnah, dan Qalqalah. Teks tidak diubah, hanya diberi warna. Saat aktif, pewarnaan kata dinonaktifkan sementara.",
                checked = toggles.tajwid,
                onCheckedChange = { v -> onChange(toggles.copy(tajwid = v)) },
            )
            if (toggles.tajwid) {
                TajwidLegend(isDark = isDark)
            }
            ToggleRow(
                title = "Warnai Waqaf",
                subtitle = "Ditandai warna biru pada huruf yang cara bacanya berubah saat berhenti: Waqaf bil Iskan, Mad Aridh Lissukun, Mad Iwadh. Pelengkap warna tajwid; teks asli tidak diubah.",
                checked = toggles.waqafColor,
                onCheckedChange = { v -> onChange(toggles.copy(waqafColor = v)) },
            )
            if (toggles.waqafColor) {
                WaqafLegend(
                    isDark = isDark,
                    toggles = toggles,
                    style = style,
                )
            }
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
    item(key = "theme", contentType = { "settings_card" }) {
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

/**
 * TajwidLegend — color swatch + label list for the 7 tajwid rules,
 * shown right under the "Tajwid" toggle while it is active (port of the
 * web's `TajwidLegend` component). Colors come from [TajwidUtil.COLORS],
 * so the legend always matches the actual highlighting.
 *
 * An extra row documents Hamzatul Washal ([HamzaWashalUtil]): the silent
 * wasl-alif is marked grayish blue automatically whenever Tajwid is on —
 * the feature has no toggle of its own, so its legend lives in this block.
 */
@Composable
private fun TajwidLegend(isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = "KODE WARNA TAJWID",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        TajwidUtil.RULES_META.forEach { meta ->
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(TajwidUtil.colorFor(meta.rule, isDark)),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = meta.name + " — " + meta.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // Hamzatul Washal — active together with Tajwid (no separate
        // toggle). Same row pattern as the waqaf legend rows; the swatch
        // uses [hamzaWashalColorFor], the same helper the text highlighting
        // uses, so the legend always matches the actual coloring.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 2.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(hamzaWashalColorFor(isDark)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = HamzaWashalUtil.META.name + " — " + HamzaWashalUtil.META.desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * WaqafLegend — blue swatch + explanation for the waqaf coloring feature,
 * shown right under the "Warnai Waqaf" toggle while it is active (port of
 * the web's `WaqafLegend` component + the panel's preview block). The blue
 * marks letters whose reading changes when the reciter stops: waqaf bil
 * iskan, mad aridh lissukun, mad iwadh. Light theme uses the exact
 * specified blue; dark theme lightens the same blue for contrast — the
 * swatch uses [waqafColorFor], the same helper the text highlighting
 * uses, so the legend always matches the actual coloring.
 *
 * Also renders a live ArabicText preview ([WaqafUtil.PREVIEW_WAQAF_TEXT])
 * so the user sees the three waqaf kinds in action, mirroring the web's
 * "Contoh waqaf (pratinjau)" block.
 */
@Composable
private fun WaqafLegend(
    isDark: Boolean,
    toggles: DisplayToggles,
    style: ArabicStyleSettings,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = "KETERANGAN WARNA WAQAF",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        WaqafUtil.RULES_META.forEach { meta ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(waqafColorFor(isDark)),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = meta.name + " — " + meta.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = "Warna biru menandai huruf yang terpengaruh cara baca ketika berhenti " +
                "(di akhir klausa, akhir ayat, dan akhir teks). Teks asli Al-Qur'an tidak diubah.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 6.dp),
        )

        // Live preview — same pattern as the reading view: the ArabicText
        // composable applies the waqaf coloring itself (toggles.waqafColor
        // is already on inside this branch).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f)),
        )
        Text(
            text = "CONTOH WAQAF (PRATINJAU)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        ArabicText(
            text = WaqafUtil.PREVIEW_WAQAF_TEXT,
            style = style,
            toggles = toggles,
            isDark = isDark,
        )
    }
}

private fun LazyListScope.fontModeSection(
    style: ArabicStyleSettings,
    onChange: (ArabicStyleSettings) -> Unit,
) {
    item(key = "fontmode", contentType = { "settings_card" }) {
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
    item(key = "tipografi", contentType = { "settings_card" }) {
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
            SliderRow(
                title = "Jarak Antar Kalimat (Arab)",
                value = style.bodyLineHeight,
                onValueChange = { v -> onChange(style.copy(bodyLineHeight = v)) },
                valueRange = ArabicStyleSettings.BODY_LINE_HEIGHT_RANGE,
                valueFormat = { String.format("%.2f em", it) },
            )
        }
    }
}

/**
 * Alignment section — its own card at the bottom of the Settings screen.
 * The user picks how Arabic text should be aligned. The default is
 * "Smart (Adaptif)" which picks the best alignment per-block based on
 * content (Quran verses → right, dua/dzikir → justify).
 */
private fun LazyListScope.alignmentSection(
    style: ArabicStyleSettings,
    onChange: (ArabicStyleSettings) -> Unit,
) {
    item(key = "perataan", contentType = { "settings_card" }) {
        SettingsCard {
            var alignExpanded by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Perataan:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ExposedDropdownMenuBox(
                    expanded = alignExpanded,
                    onExpandedChange = { alignExpanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = style.align.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mode") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = alignExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = alignExpanded,
                        onDismissRequest = { alignExpanded = false },
                    ) {
                        ArabicAlign.entries.forEach { align ->
                            DropdownMenuItem(
                                text = { Text(align.label) },
                                onClick = {
                                    onChange(style.copy(align = align))
                                    alignExpanded = false
                                },
                            )
                        }
                    }
                }
            }
            // Helpful description of what the current mode does.
            Text(
                text = when (style.align) {
                    ArabicAlign.Smart -> "Ayat Al-Qur'an (ber tanda ۝/۩) otomatis rata kanan (gaya Mushaf). Doa, dzikir, dan shalawat diratakan rata kanan-kiri agar blok bacaan rapi dan mudah dipindai. Frasa pendek tidak terpengaruh."
                    ArabicAlign.Right -> "Seluruh teks Arab rata kanan (arah baca default Arab). Cocok untuk Mushaf gaya tradisional."
                    ArabicAlign.Justify -> "Seluruh teks Arab diratakan rata kanan-kiri (kedua sisi rapi). Teks panjang yang terbalik ke beberapa baris akan direntangkan."
                    ArabicAlign.Center -> "Seluruh teks Arab rata tengah. Cocok untuk kalimat pendek/frasa; teks panjang bisa terlihat berantakan."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 0.dp, bottom = 4.dp),
            )
        }
    }
}
