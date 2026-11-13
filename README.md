# Tahlil

> Web + Native Android (Kotlin) application for reading Tahlil Lengkap (Malam Jumat) — sourced from Kitab Tahlil Lengkap with 32 sections.

[![CI](https://github.com/SecretArrow/Tahlil-App/actions/workflows/ci.yml/badge.svg)](https://github.com/SecretArrow/Tahlil-App/actions/workflows/ci.yml)
[![Auto-fix](https://github.com/SecretArrow/Tahlil-App/actions/workflows/auto-fix.yml/badge.svg)](https://github.com/SecretArrow/Tahlil-App/actions/workflows/auto-fix.yml)
[![GitHub Pages](https://img.shields.io/badge/web-GitHub%20Pages-emerald?logo=github)](https://secretarrow.github.io/Tahlil-App/)

## 🌐 Live Web

**URL:** https://secretarrow.github.io/Tahlil-App/

Di-deploy otomatis dari branch `main` ke GitHub Pages (job `pages-deploy` di CI).
Semua settings (mode gelap/terang, tipografi Arab, toggles tampilan, auto-scroll)
disimpan di cookie browser — tidak butuh akun, tidak butuh server.

## 📱 Struktur Repository

```
Tahlil-App/
├── web/                 # Next.js 16 web app (TypeScript, Tailwind v4)
├── android/             # Native Android app (Kotlin, Jetpack Compose) — package: my.tahlil.app
├── .github/workflows/   # CI/CD pipeline + auto-fix (GitHub Actions)
│   ├── ci.yml          # Lint + unit test + instrumented test + release
│   └── auto-fix.yml    # Prettier auto-format & commit
├── docs/                # Dokumentasi tambahan
├── CHANGELOG.md
└── README.md
```

## 🌐 Web App

**Stack:** Next.js 16 App Router, TypeScript, Tailwind CSS v4, shadcn/ui, Framer Motion, next-themes

**Fitur:**
- 32 section lengkap (Pembuka → Salam Penutup) — Tahlil Lengkap Malam Jumat
- 3 mode font Arab: Standar, Gaya Mushaf Utsmani, Naskh Tradisional
- Toggle show/hide Arab/Latin/Terjemah
- Toggle "Koma pada Teks Arab"
- Toggle "Warnai Kata Arab" (alternating colors per word)
- Slider tipografi (ukuran, jarak baris, jarak kata, jarak huruf)
- Mode gelap/terang dengan auto-save via cookie

**Cara menjalankan:**
```bash
cd web
npm install
npm run dev
```

**Build:**
```bash
npm run build
```

**Format kode (Prettier):**
```bash
npm run format       # auto-format
npm run format:check # cek tanpa ubah
```

## 📱 Android App

**Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, DataStore, Coroutines, Gradle Kotlin DSL

**Package:** `my.tahlil.app`

**Fitur (sama dengan web):**
- 32 section dengan Arabic text + transliterasi + terjemahan
- Toggle show/hide Arab/Latin/Terjemah
- Toggle "Koma pada Teks Arab"
- Toggle "Warnai Kata Arab"
- 3 mode font Arab (Standard, Quran, Naskh Tradisional)
- Slider tipografi
- Dark/Light theme (auto-save via DataStore)
- Navigasi drawer + scroll active tracking
- Tanda waqf mushaf ۝X dengan CSS-drawn circle
- Repeat badge untuk blok dengan `repeat` field
- Logo baru: Rub el Hizb (8-pointed Islamic star) + crescent + star

**Cara build (lokal):**
```bash
cd android
./gradlew assembleDebug
# atau untuk release signed (memerlukan keystore):
./gradlew assembleRelease
```

**APK output:**
- `app-arm64-v8a-release.apk` (modern 64-bit ARM)
- `app-armeabi-v7a-release.apk` (legacy 32-bit ARM)
- `app-x86_64-release.apk` (emulators / x86_64 devices)
- `app-universal-release.apk` (all ABIs)

## 🚀 CI/CD Pipeline

Pipeline otomatis via GitHub Actions:

1. **auto-fix** (`.github/workflows/auto-fix.yml`):
   - Trigger: push ke `main`
   - Jalankan `prettier --write` di `web/`
   - Commit otomatis (dengan `[skip ci]` agar tidak loop)

2. **ci.yml** — pipeline utama:
   - **web-test**: Prettier check + Next.js lint + build
   - **android-test**: Gradle lint + unit test (JVM) + assembleRelease (signed)
   - **android-split-apks**: Verifikasi signature setiap APK dengan `apksigner`
   - **android-instrumented-test**: Instrumented test (API 29) + APK release di-install & di-launch di emulator
   - **release**: Buat GitHub Release dengan semua artifacts (push to main only)

## 🔐 Setup Signing Secret (untuk Release APK)

Pipeline release akan sign APK hanya jika semua secret berikut ada di repo
`SecretArrow/Tahlil-App` (Settings → Secrets and variables → Actions):

| Secret | Nilai | Cara dapat |
|--------|-------|------------|
| `KEYSTORE_BASE64` | base64 dari file `.jks` | Lihat langkah 1-2 di bawah |
| `KEYSTORE_PASSWORD` | password keystore | `hKq71w4q4nWj` (password unzip zip) — ATAU password keystore sendiri jika berbeda |
| `KEY_ALIAS` | alias key di dalam keystore | Tergantung konfigurasi keystore Anda |
| `KEY_PASSWORD` | password untuk key alias | Biasanya sama dengan `KEYSTORE_PASSWORD` |

### Cara menyiapkan `KEYSTORE_BASE64`

1. **Unzip** file `key-sign-apk.zip` (password: `hKq71w4q4nWj`):

   ```bash
   # Di terminal lokal Anda:
   unzip -P hKq71w4q4nWj key-sign-apk.zip -d key-sign-apk/
   # Hasilnya biasanya file .jks atau .keystore
   ls key-sign-apk/
   ```

2. **Encode ke base64:**

   ```bash
   # Asumsi file hasil unzip adalah release.jks
   base64 -w 0 key-sign-apk/release.jks > keystore.b64
   # Atau di macOS:
   # base64 -i key-sign-apk/release.jks | tr -d '\n' > keystore.b64
   ```

3. **Tambahkan ke GitHub Secrets:**
   - Buka https://github.com/SecretArrow/Tahlil-App/settings/secrets/actions
   - Klik **New repository secret**
   - Name: `KEYSTORE_BASE64`
   - Value: paste isi `keystore.b64` (string panjang, satu baris)
   - Ulangi untuk `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

4. **Verifikasi:**
   - Push commit ke `main`
   - Buka tab Actions → lihat job `android-test`
   - Log harus menampilkan: `Signing keystore installed.`
   - APK release yang dihasilkan sudah signed (verifiable via `apksigner`)

> ⚠️ **Jangan pernah commit** file `key-sign-apk.zip`, file `.jks`, atau password
> apa pun ke repo. Hanya simpan di GitHub Secrets.

## 📦 Releases

Release otomatis dibuat setiap push ke `main` branch. Setiap release berisi:
- 4 APK (per-ABI + universal) yang sudah signed
- Web build (zip)

Lihat: https://github.com/SecretArrow/Tahlil-App/releases

## 📚 Sumber Konten

Kitab Tahlil Lengkap (Malam Jumat) — 32 bagian dari Pembuka hingga Salam Penutup.

## 📄 License

MIT
