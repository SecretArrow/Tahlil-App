# Architecture

## Overview

Tahlil & Doa is a multi-platform Islamic prayer book app with two implementations:

1. **Web** — Next.js 16 web app (`/web`)
2. **Android** — Native Kotlin + Jetpack Compose app (`/android`)

Both share the same content data (sections, Arabic text, translations) but use platform-native UI.

## Web Architecture

- **Framework**: Next.js 16 App Router
- **Language**: TypeScript
- **Styling**: Tailwind CSS v4 + shadcn/ui
- **State**: React Context (ArabicStyleProvider, DisplaySettingsProvider)
- **Persistence**: HTTP cookies (anti-flash SSR via cookie reading)
- **Fonts**: Amiri, Amiri Quran, Scheherazade New (Google Fonts)

### Key Files
- `src/data/tahlil-content.ts` — 11 sections with all Arabic blocks
- `src/components/arabic-text.tsx` — Arabic text rendering (ayah marks, colorize, comma, sentence splitting)
- `src/components/display-settings-provider.tsx` — Toggle state context
- `src/components/arabic-style-provider.tsx` — Font settings context

## Android Architecture

- **Language**: Kotlin 1.9.22
- **UI**: Jetpack Compose (Material 3)
- **Navigation**: Navigation Compose
- **State**: ViewModel + StateFlow
- **Persistence**: DataStore Preferences
- **DI**: Manual (single Application-level repository)

### Key Files
- `app/src/main/java/my/tahlil/app/data/TahlilContent.kt` — Same 11 sections
- `app/src/main/java/my/tahlil/app/ui/components/ArabicText.kt` — Composable replicating web ArabicText
- `app/src/main/java/my/tahlil/app/util/ArabicCommaUtil.kt` — Port of addArabicCommas
- `app/src/main/java/my/tahlil/app/util/ColorizeUtil.kt` — Word coloring
- `app/src/main/java/my/tahlil/app/util/AyahMarkUtil.kt` — ۝X parsing & rendering

## CI/CD Pipeline

See `.github/workflows/ci.yml`:
1. Web test & build (Node 20)
2. Android test & assembleRelease (JDK 17)
3. APK signature verification
4. GitHub Release with artifacts

## Secrets

- `KEYSTORE_BASE64` — base64-encoded keystore
- `KEYSTORE_PASSWORD` — keystore password
- `KEY_ALIAS` — key alias
- `KEY_PASSWORD` — key password
