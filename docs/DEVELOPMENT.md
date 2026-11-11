# Development

## Web Local Development

```bash
cd web
npm install
npm run dev    # http://localhost:3000
```

## Android Local Development

```bash
cd android
# Generate gradle wrapper (if missing)
gradle wrapper --gradle-version 8.5 --distribution-type bin

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Lint
./gradlew lint
```

### Signing for Release (Local)

1. Generate keystore (or get from CI secrets):
```bash
keytool -genkeypair \
  -keystore release.jks \
  -storepass YOUR_PASSWORD \
  -alias tahlil-doa \
  -keypass YOUR_PASSWORD \
  -keyalg RSA \
  -keysize 2048 \
  -validity 36500
```

2. Set env vars:
```bash
export KEYSTORE_PATH=/path/to/release.jks
export KEYSTORE_PASSWORD=YOUR_PASSWORD
export KEY_ALIAS=tahlil-doa
export KEY_PASSWORD=YOUR_PASSWORD
```

3. Build release:
```bash
./gradlew assembleRelease
```

## Building via CI/CD

Push to `main` branch triggers the workflow automatically:
- Web build (Next.js)
- Android signed APKs (per-ABI + universal)
- APK signature verification
- GitHub Release with artifacts

Manual trigger: Actions → CI → Run workflow
