# Tahlil App — Web

Aplikasi web Tahlil Lengkap (Malam Jumat) — 32 bagian rangkaian zikir, tahlil, Al-Fatihah, Ayat Kursi, Al-Ikhlas, Al-Falaq, An-Nas, sholawat, dan doa untuk mendoakan para arwah kaum muslimin.

## Fitur

- 32 section lengkap (Pembuka → Salam Penutup) — Tahlil Lengkap Malam Jumat
- 3 mode font Arab: Standar, Gaya Mushaf Utsmani, Naskhi Tebal
- Toggle show/hide Arab/Latin/Terjemah
- Toggle "Koma pada Teks Arab" (auto tambah koma Arab mengikuti terjemahan)
- Toggle "Warnai Kata Arab" (warna bergantian per kata)
- Slider tipografi: ukuran, jarak baris, jarak kata, jarak huruf
- Mode gelap/terang dengan auto-save via cookie
- Tanda waqf mushaf ۝X dengan CSS-drawn circle
- Teks Arab dipecah per kalimat dengan tanda di akhir baris
- Logo baru: Rub el Hizb (8-pointed Islamic star) + crescent + star

## Stack

- Next.js 16 App Router + TypeScript
- Tailwind CSS v4
- shadcn/ui
- Framer Motion
- next-themes (cookie-based)
- Prettier (auto-format via GitHub Actions)

## Pengembangan

```bash
npm install
npm run dev
```

## Build

```bash
npm run build
```

## Format kode

```bash
npm run format       # auto-format dengan Prettier
npm run format:check # cek tanpa ubah
```

CI juga menjalankan `format:check` dan ada workflow `auto-fix.yml` yang akan
auto-commit formatting changes ke main.

## Sumber

Kitab Tahlil Lengkap (Malam Jumat) — 32 bagian dari Pembuka hingga Salam Penutup.
