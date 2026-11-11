#!/usr/bin/env bash
# ============================================================
# prepare-keystore-secret.sh
# ============================================================
# Helper script untuk menyiapkan KEYSTORE_BASE64 secret dari
# file key-sign-apk.zip (password-protected).
#
# Cara pakai (di mesin lokal Anda, BUKAN di CI):
#
#   1. Letakkan file key-sign-apk.zip di folder yang sama dengan script ini.
#   2. Jalankan:
#        ./prepare-keystore-secret.sh
#   3. Script akan:
#        - unzip key-sign-apk.zip (password: hKq71w4q4nWj)
#        - detect file .jks / .keystore di dalamnya
#        - encode ke base64 (satu baris panjang)
#        - tulis ke file keystore.b64
#        - print semua secret values yang perlu ditambahkan ke GitHub
#
# Setelah itu, copy paste masing-masing value ke:
#   https://github.com/SecretArrow/Tahlil-App/settings/secrets/actions
#
# JANGAN commit file keystore.b64 ke repo!
# ============================================================

set -e

ZIP_PASSWORD="hKq71w4q4nWj"
ZIP_FILE="key-sign-apk.zip"
EXTRACT_DIR="key-sign-apk-extracted"
OUTPUT_FILE="keystore.b64"

echo "=== Tahlil — Keystore Secret Preparation ==="
echo ""

# 1. Verify zip exists
if [ ! -f "$ZIP_FILE" ]; then
  echo "ERROR: $ZIP_FILE tidak ditemukan di folder ini."
  echo "Letakkan file $ZIP_FILE di folder yang sama dengan script ini, lalu jalankan ulang."
  exit 1
fi

# 2. Unzip with password
echo "1. Unzip $ZIP_FILE (password: $ZIP_PASSWORD)..."
rm -rf "$EXTRACT_DIR"
mkdir -p "$EXTRACT_DIR"
unzip -P "$ZIP_PASSWORD" "$ZIP_FILE" -d "$EXTRACT_DIR" >/dev/null 2>&1 || {
  echo "ERROR: unzip gagal. Periksa password atau integritas file."
  exit 1
}
echo "   OK — diekstrak ke $EXTRACT_DIR/"
echo ""

# 3. Find keystore file (.jks or .keystore)
KEYSTORE_FILE=$(find "$EXTRACT_DIR" -type f \( -name "*.jks" -o -name "*.keystore" \) | head -1)
if [ -z "$KEYSTORE_FILE" ]; then
  echo "ERROR: tidak ada file .jks atau .keystore di $EXTRACT_DIR."
  echo "Isi folder:"
  ls -la "$EXTRACT_DIR"
  exit 1
fi
echo "2. Keystore file ditemukan: $KEYSTORE_FILE"
echo ""

# 3. Base64 encode
echo "3. Encode ke base64..."
if [[ "$(uname)" == "Darwin" ]]; then
  # macOS base64 tidak support -w 0
  base64 -i "$KEYSTORE_FILE" | tr -d '\n' > "$OUTPUT_FILE"
else
  # Linux base64 with -w 0 (no line wrap)
  base64 -w 0 "$KEYSTORE_FILE" > "$OUTPUT_FILE"
fi
B64_SIZE=$(wc -c < "$OUTPUT_FILE")
echo "   OK — ditulis ke $OUTPUT_FILE ($B64_SIZE bytes)"
echo ""

# 4. Try to list keystore entries (requires keytool from JDK)
echo "4. List keystore entries (memerlukan keytool dari JDK)..."
if command -v keytool >/dev/null 2>&1; then
  echo "   Masukkan keystore password saat diminta ($ZIP_PASSWORD atau password lain):"
  keytool -list -v -keystore "$KEYSTORE_FILE" 2>/dev/null | grep -E "Alias name|Entry type|Creation date" | head -20 || true
else
  echo "   keytool tidak tersedia — skip listing."
  echo "   Untuk melihat alias: install JDK lalu jalankan:"
  echo "     keytool -list -v -keystore $KEYSTORE_FILE"
fi
echo ""

# 5. Print summary
echo "============================================================"
echo "✅ Secret siap! Salin masing-masing value ke:"
echo "   https://github.com/SecretArrow/Tahlil-App/settings/secrets/actions"
echo ""
echo "Secret 1: KEYSTORE_BASE64"
echo "  Value: (isi dari file $OUTPUT_FILE)"
echo "         -> copy dengan: pbcopy < $OUTPUT_FILE  (macOS)"
echo "         -> copy dengan: xclip -sel c < $OUTPUT_FILE  (Linux)"
echo ""
echo "Secret 2: KEYSTORE_PASSWORD"
echo "  Value: $ZIP_PASSWORD   (atau password keystore Anda yang sebenarnya)"
echo ""
echo "Secret 3: KEY_ALIAS"
echo "  Value: (lihat output keytool di atas — biasanya 'release' atau 'key0')"
echo ""
echo "Secret 4: KEY_PASSWORD"
echo "  Value: $ZIP_PASSWORD   (biasanya sama dengan KEYSTORE_PASSWORD)"
echo ""
echo "⚠️  JANGAN commit file $OUTPUT_FILE atau folder $EXTRACT_DIR ke repo!"
echo "    Tambahkan ke .gitignore jika perlu."
echo "============================================================"
