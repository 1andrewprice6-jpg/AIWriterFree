# Installation Guide - AI Writer Free

## Quick Install (Pre-Built APK)

If you have a pre-built APK:

```bash
adb install AIWriterFree-v1.0.0.apk
```

## Build and Install from Source

### Prerequisites

1. **Android SDK** (if not already installed):
   ```bash
   # Set Android SDK location
   export ANDROID_HOME=~/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/platform-tools
   ```

2. **Java JDK 17** (required for Android Gradle Plugin):
   ```bash
   java -version  # Should show version 17 or higher
   ```

### Build Steps

1. **Extract the project** (if from archive):
   ```bash
   tar -xzf AIWriterFree-Complete.tar.gz
   cd AIWriterFree
   ```

2. **Make build script executable**:
   ```bash
   chmod +x build.sh
   ```

3. **Build the APK**:
   ```bash
   ./build.sh
   ```

4. **Install on connected device**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

   Or install and launch:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb shell am start -n com.aiwriter.free/.MainActivity
   ```

### Verification

After installation:

1. **Open AI Writer** from app drawer
2. **Grant notification permission** (Android 13+)
3. **Tap "Download Model"**
4. **Check notification** - Download should show progress
5. **Close app** - Download continues in background
6. **Wait ~5-10 minutes** (depends on internet speed)
7. **Reopen app** - Should show "AI Model Ready"

## Troubleshooting Build Issues

### Android SDK Not Found

```bash
# Download Android SDK Command Line Tools from:
# https://developer.android.com/studio#command-tools

# Extract and set environment variables
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Accept licenses
sdkmanager --licenses
```

### Gradle Build Failed

```bash
# Clean and retry
./gradlew clean
./gradlew assembleDebug --stacktrace

# If still fails, check:
# - Java version (must be 17+)
# - Internet connection (Gradle downloads dependencies)
# - Disk space (needs ~1GB for build artifacts)
```

### Device Not Found

```bash
# Check USB debugging enabled on device
adb devices

# If no devices listed:
# 1. Enable Developer Options on device
# 2. Enable USB Debugging
# 3. Authorize computer when prompted
# 4. Run: adb devices
```

## Post-Installation

### First Launch

1. Open **AI Writer**
2. Tap **"Download Model (1.5GB)"**
3. Grant **notification permission** if requested
4. Notification appears: **"AI Writer - Starting download..."**
5. Download continues in background
6. Close app if needed - download persists

### Testing Features

After model downloads:

1. Open any app (Notes, Messages, Chrome, etc.)
2. Type or select some text
3. Tap **"..." menu** or **Share** button
4. Choose **"AI Writer"**
5. Select category and action
6. Text is processed and replaced

## Manual Installation Steps

If automated build fails:

1. **Open in Android Studio**:
   - File → Open → Select AIWriterFree folder
   - Wait for Gradle sync
   - Build → Build Bundle(s) / APK(s) → Build APK(s)

2. **Locate APK**:
   - `app/build/outputs/apk/debug/app-debug.apk`

3. **Install**:
   ```bash
   adb install path/to/app-debug.apk
   ```

## Uninstallation

```bash
adb uninstall com.aiwriter.free
```

Or from device:
- Settings → Apps → AI Writer → Uninstall

## Support

Issues? Check logs:
```bash
adb logcat | grep -E "AIWriter|ModelDownload"
```

Common issues:
- **Download fails**: Check internet, try again
- **App crashes**: Clear data, reinstall
- **No text menu**: Check app permissions
- **Slow inference**: Model still loading, wait 10s after launch
