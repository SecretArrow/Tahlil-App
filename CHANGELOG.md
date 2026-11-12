# Changelog — Tahlil Singkat Web

Semua pembaharuan dan perbaikan dari awal pengembangan hingga saat ini.

---

## [Versi Terbaru] — 2026-11-05

### Audit & Fix Harakat Mode Naskhi Tebal

- **Audit harakat komprehensif** dilakukan untuk semua 38 blok Arab di `src/data/tahlil-content.ts`
- Dibuat script audit (`scripts/audit-harakat-v3.py`) untuk mendeteksi huruf konsonan bare (tanpa harakat)
- **Hasil audit** menemukan 3 kategori masalah:
  - **Grup A**: Lam1 di varian "Allah" (standalone, Allahumma, bi-llah, wa-llah) tanpa sukun eksplisit
  - **Grup B**: Lam1 di artikel wa+al+syamsiyyah tanpa sukun
  - **Grup C**: Ha dengan superscript dammah/kasrah Quran (ٗ/ٖ) — sudah berharakat

### Perbaikan yang Diterapkan (Sesuai Keputusan User)

#### ✅ Fix 1: Tambah sukun (ْ) di lam1 artikel "wa+al+syamsiyyah"
- Pattern: `وَال(?!ْ)([تثدذرزسشصضطظلن])` → `وَالْ\1`
- 10 kemunculan di-fix:
  - `وَالشُّهَدَاءِ` → `وَالْشُّهَدَاءِ`
  - `وَالصَّالِحِيْنَ` → `وَالْصَّالِحِيْنَ`
  - `وَالصَّحَابَةِ` → `وَالْصَّحَابَةِ`
  - `وَالتَّابِعِيْنَ` → `وَالْتَّابِعِيْنَ`
  - `وَالنَّاسِ` → `وَالْنَّاسِ`
  - `وَالنَّجَاةَ` → `وَالْنَّجَاةَ`

#### ✅ Fix 2: Tambah fathah+syadda di lam2 "li-llah" yang telanjang
- Pattern: `لِلهِ` → `لِلَّهِ` (lam2 dari li-llah sebelumnya telanjang, sekarang punya fathah+syadda)
- 6 kemunculan di-fix

#### ❌ Revert (sesuai permintaan user): Tidak menambah sukun di lam1 varian "Allah"
- User memilih untuk TIDAK menambah sukun di lam1 "Allah" (konvensi standar idgham Arab dipertahankan)
- Semua varian "Allah" tetap menggunakan konvensi idgham:
  - `اللَّهِ` (Allah standalone) — 34 kemunculan, tanpa sukun di lam1
  - `اَللّٰهُمَّ` (Allahumma) — 6 kemunculan, tanpa sukun di lam1
  - `بِاللَّهِ` (bi-llah) — 2 kemunculan, tanpa sukun di lam1
  - `وَاللَّهُ` (wa-llah) — 4 kemunculan, tanpa sukun di lam1

---

## [Versi Sebelumnya] — 2026-11-05

### Toggle "Warnai Kata Arab" (colorizeArabic)

- **Field baru di `DisplayToggles`**: `colorizeArabic` (default `false`)
- **Persistensi cookie**: tersimpan di `tahlil-display-toggles-v1` (SSR anti-flash)
- **Logika warna** (di `src/components/arabic-text.tsx`):
  - Deteksi tema via `useTheme()` dari next-themes → gunakan `resolvedTheme`
  - **Light mode** (teks gelap): `["#000000", "#011b45"]` bergantian per kata
  - **Dark mode** (teks terang): `["#ffffff", "#d9dadb"]` bergantian per kata
  - Counter shared across all line splits (ayah lines, sentence splits, trailing) → pola A/B/A/B kontinu
  - Ayah marks (۝X) tetap pakai styling sendiri, TIDAK ikut diwarnai
  - Hydration-safe: pakai `mounted` flag — SSR render tanpa warna, setelah mount warna diterapkan
  - Skip di compare view (`forceMode` di-set) supaya preview 3 kolom tidak terpengaruh
- **UI toggle di panel Tampilan Bacaan**: ikon Palette
- **Pixel-level verification**:
  - Light mode: 1.076 piksel `RGB=(0,0,0)` + 1.097 piksel `RGB=(1,27,69)` — bergantian per kata
  - Dark mode: 21 kata warna `RGB=(255,255,255)` dan `RGB=(217,218,219)` bergantian

### Toggle "Koma pada Teks Arab" (punctuateArabic)

- **Field baru di `DisplayToggles`**: `punctuateArabic` (default `false`)
- **Logika transformasi** (`addArabicCommas()` di `src/components/arabic-text.tsx`):
  - Ayat Al-Qur'an TIDAK diubah — blok yang mengandung ۝ atau ۩ dilewati
  - Tambah koma Arab (،) sebelum konektor "وَ" dengan pengecualian:
    - Frasa shalawat seperti " وَسَلَّمْ" / " وَسَلِّمْ" → skip
    - Konektor setelah frasa shalawat → skip (pengantar item daftar pertama)
    - Konektor TERAKHIR di tiap segmen → skip (pola "X، Y، Z dan W")
  - Blok dengan 0-1 konektor → tidak diubah
- **Hasil verifikasi** (sholawat Nabi):
  - Sebelum: `...وَسَلَّمْ وَعَلَى آلِهِ وَأَصْحَابِهِ وَأَزْوَاجِهِ وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ...`
  - Sesudah: `...وَسَلَّمْ وَعَلَى آلِهِ، وَأَصْحَابِهِ، وَأَزْوَاجِهِ، وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ...`
  - Cocok dengan terjemahan "keluarga, sahabat, istri, anak cucu dan Ahli Baitnya"
- **UI toggle di panel Tampilan Bacaan**: ikon ListPlus

### Hero: Tombol "Mulai Membaca" → "Mulai"

- Label tombol CTA pertama diubah dari **"Mulai Membaca"** → **"Mulai"**
- 4 tombol CTA sekarang: Mulai, Tahlil, Dzikir, Doa
- File: `src/components/tahlil/hero.tsx`

### Pemecahan Teks Arab Panjang per Kalimat

- Teks Arab panjang (doa) dipecah per kalimat dengan tanda (، atau ۝X) di akhir setiap baris
- Implementasi di `src/components/arabic-text.tsx` dengan fungsi `splitBySentence()`
- Inline style `LINE_STYLE = { display: "block", marginBottom: "0.25em" }` di setiap `.ayah-line` span
- Verifikasi VLM: Al-Fatihah 7 baris, Doa block 5 Section 9 8 baris, semua dengan tanda di akhir

### Hapus Closing Dua Block Redundan di `page.tsx`

- `Rabbanaa aatinaa` muncul 2x — dihapus dari `page.tsx` (sudah ada di Section 10)
- Import `ArabicText` & `useDisplaySettings` dibersihkan

### Section 9 Dipecah Jadi 5 Blok

- Untuk memudahkan hafalan, Section 9 (Doa Penyampaian Pahala) dipecah jadi 5 blok terpisah

### Audit Urutan Doa (Vertikal → Horizontal)

- Pola ideal: Allah → Nabi → Wali → Tujuan Acara → Keluarga → Umat Muslim
- **Kesimpulan akhir**: urutan saat ini sudah sesuai konvensi kitab tahlil
- Doa hadiah/isal tsawab mulai dengan "Ilaa/Tsumma ilaa" (bukan "Ya Allah")
- Doa permohonan mulai dengan "Ya Allah"

### Hapus Semua Penyebutan NU/Nahdlatul Ulama

- Hapus `wa khushushon ilaa mu-aassisi jam'iyyati nahdhatil-'ulamaa` dari Arab/Latin/Terjemahan
- Tidak ada satu pun penyebutan NU di seluruh teks

### Tambah Blok: Ikhwan Nabi + Syekh Abdul Qadir Al-Jailani

- Section 3 sekarang 7 blok:
  1. Ikhwan Nabi (TANPA menyebut NU)
  2. Al-Fatihah
  3. Tahlil awal
  4. Syaikh/guru
  5. Al-Fatihah
  6. Mimman dzukirot
  7. Al-Fatihah
- Termasuk blok Syekh Abdul Qadir Al-Jailani

### Perubahan Terjemahan Blok-Blok Tertentu

- `Dan kepada orang yang menyebabkan kami sekalian berkumpul di sini` → `dan bagi ahli kubur/arwah yang menjadi sebab kami berkumpul di sini`
- `Khushushon ilaa aabaainaa wa ummahaatinaa` → `Khushūṣan ilā ḥaḍrati rūḥi...`

### Mobile: Ayat Baru = Baris Baru + Kecilkan Lingkaran

- Mobile: jika ayat baru maka baris baru saja
- Lingkaran dekoratif ukurannya terlalu besar → diperkecil 50% (2.2em → 1.1em)

### Perbaiki ۝١ agar Angka Tampil di Tengah Ornament

- CSS-drawn double circle (border + ::before ring) dengan digit centered
- Menghindari font rendering inconsistency
- File: `src/app/globals.css` — class `.ayah-mark`

### Tambah Slider "Jarak Antar Huruf" (letter-spacing)

- Slider keempat di Arabic Style Panel
- Range 0–0.2em
- File: `src/components/arabic-style-panel.tsx`, `src/data/arabic-style.ts`

### Auto Load/Save State/Config via Cookie Browser

- Cookie-based persistence (bukan localStorage)
- Cookie keys:
  - `tahlil-theme` (next-themes)
  - `tahlil-arabic-style-v1`
  - `tahlil-display-toggles-v1`
- API route `/api/settings` untuk server-side cookie + SSR initial settings (anti-flash)
- File: `src/app/api/settings/route.ts`, `src/lib/cookies/client.ts`, `src/lib/cookies/server.ts`

### Hero Redesign: 4 Tombol CTA

- Hapus semua konten hero sebelumnya (judul, deskripsi, sumber)
- Ganti dengan 4 tombol CTA:
  - **Mulai Membaca** (#pembukaan) — primary variant
  - **Tahlil** (#tahlil-ahli-kubur)
  - **Dzikir** (#dzikir)
  - **Doa** (#doa-pembuka)
- File: `src/components/tahlil/hero.tsx`

### Hapus Teks "(Pemula)" dari Label Mode Naskhi

- Label "Naskhi Tebal (Pemula)" → "Naskhi Tebal"
- File: `src/data/arabic-style.ts`

### Harakat Lengkap + Tanda Baca Tepat (Opsi A + B)

- **Opsi A**: Konsistensi tanda baca — koma Arab (،) digunakan konsisten
- **Opsi B**: Tanda waqf mushaf `۝X` tanpa titik di akhir ayat
- Regex parsing `/(۝[٠-٩]+)/g` untuk ayah marks

### Toggle Show/Hide Arab/Latin/Terjemah

- 3 toggle di Display Settings Panel (popover dengan ikon Eye di header)
- Live preview di panel
- Persistensi cookie
- File: `src/components/display-settings-panel.tsx`, `src/components/display-settings-provider.tsx`

### Audit Bacaan Arab vs Latin

- Cross-check bacaan Arab dengan transliterasi Latin
- Fix: "wa ilaa aalihi" → "wa 'alaa aalihi" (pola shalawat)
- Arab diubah ke `وَعَلَى آلِهِ`

### Fitur Pilihan Gaya Teks Arab

- 3 mode dengan pratinjau langsung:
  1. **Arab Standar** — font Amiri
  2. **Gaya Mushaf Al-Qur'an (Utsmani)** — font Amiri Quran
  3. **Naskhi Tebal** — font Scheherazade New (tanpa ligatur, harakat lengkap, spasi renggang)
- OpenType font features:
  - Naskhi: `liga`/`rlig`/`clig`/`calt` OFF (no ligature)
  - Naskhi: `mark`/`mkmk`/`ccmp`/`kern`/`rvrn` ON (untuk harakat positioning)
- Suggested settings untuk Naskhi: fontSize 2.0, lineHeight 2.6, wordSpacing 0.18, letterSpacing 0.03
- File: `src/data/arabic-style.ts`, `src/components/arabic-style-panel.tsx`, `src/components/arabic-text.tsx`

### Audit & Bandingkan dengan Versi NU (quran.nu.or.id/tahlil)

- Perbandingan struktur tanpa mengubah yang ada
- Identifikasi perbedaan struktur dengan kitab tahlil versi NU

### Inisialisasi Web Tahlil Singkat

- Web Tahlil Singkat (dari PDF Salik Channel) — support PC/Android
- Tampilan menarik dengan palet Islamic emerald/gold
- Mode gelap/terang (next-themes)
- State saat di scroll (IntersectionObserver untuk active section tracking)
- Framer Motion untuk scroll animations
- Stack: Next.js 15 App Router, TypeScript, Tailwind CSS v4
- Font Google: Amiri, Amiri Quran, Scheherazade New, Plus Jakarta Sans
- File utama:
  - `src/app/layout.tsx` — async server component dengan cookie reading
  - `src/app/page.tsx` — main page
  - `src/app/globals.css` — palet Islamic, font classes
  - `src/data/tahlil-content.ts` — 11 sections, ~38 blok Arab
  - `src/components/tahlil/header.tsx` — sticky header dengan 4 tombol
  - `src/components/tahlil/hero.tsx` — hero section dengan 4 CTA
  - `src/components/arabic-text.tsx` — component render Arab
  - `src/components/arabic-style-provider.tsx` — context untuk Arabic style
  - `src/components/display-settings-provider.tsx` — context untuk toggles

### Zip Source Code untuk Download

- Source code di-zip dan disediakan di `/home/z/my-project/download/tahlil-singkat-source.zip`

---

## Struktur File Penting

```
src/
├── app/
│   ├── api/settings/route.ts          # POST endpoint untuk persist cookie
│   ├── globals.css                    # Palet Islamic, font classes, ayah mark CSS
│   ├── layout.tsx                     # Async server component, baca cookies SSR
│   └── page.tsx                       # Main page
├── components/
│   ├── arabic-style-panel.tsx         # Panel dengan 3 ModeCard + 4 slider
│   ├── arabic-style-provider.tsx      # Context Arabic style
│   ├── arabic-text.tsx                # Component render Arab (ayah marks, colorize, comma)
│   ├── display-settings-panel.tsx     # Popover 5 toggle + live preview
│   ├── display-settings-provider.tsx  # Context toggles
│   ├── theme-provider.tsx             # next-themes wrapper
│   ├── theme-toggle.tsx               # Theme toggle button
│   └── tahlil/
│       ├── header.tsx                 # Sticky header 4 tombol
│       └── hero.tsx                   # Hero 4 CTA
├── data/
│   ├── arabic-style.ts                # 3 mode definitions
│   ├── display-settings.ts            # DisplayToggles type + defaults
│   └── tahlil-content.ts              # 11 sections, 38 blok Arab
└── lib/
    └── cookies/
        ├── client.ts                  # Cookie helper client-side
        └── server.ts                  # Cookie helper server-side

scripts/
├── audit-harakat-v3.py                # Audit harakat untuk Naskhi Tebal
├── apply-harakat-fix.py               # Apply fix harakat
├── revert-allah-sukun.py              # Revert sukun di lam1 Allah
└── test-arabic-comma.js               # Test logic addArabicCommas
```

---

## Teknologi yang Digunakan

- **Framework**: Next.js 16.1.3 (App Router, Turbopack)
- **Language**: TypeScript
- **Styling**: Tailwind CSS v4
- **UI Components**: shadcn/ui
- **Animations**: Framer Motion
- **Theme**: next-themes (cookie-based)
- **Fonts Google**: Amiri, Amiri Quran, Scheherazade New, Plus Jakarta Sans

## Catatan Penting

- **Cookie-based persistence**: Semua state (theme, Arabic style, display toggles) disimpan di cookie browser (bukan localStorage) untuk anti-flash via SSR
- **Hydration-safe**: Colorize menggunakan `mounted` flag untuk menghindari SSR/CSR mismatch
- **OpenType features**: Mode Naskhi mematikan ligatur (`liga`/`rlig`/`clig`/`calt` OFF) dan mengaktifkan mark positioning (`mark`/`mkmk`/`ccmp`/`kern`/`rvrn` ON)
- **Ayah marks**: CSS-drawn double circle dengan digit centered (bukan font-based)
- **RTL enforcement**: `dir="rtl"` + `unicode-bidi: isolate` di semua ArabicText

## Sumber Referensi

- **Kitab Tahlil**: Salik Channel (PDF)
- **Perbandingan**: quran.nu.or.id/tahlil (versi NU)
- **Konvensi Arab**: Idgham untuk lam1 "Allah" (standar Arab), sukun eksplisit untuk artikel wa+al+syamsiyyah
