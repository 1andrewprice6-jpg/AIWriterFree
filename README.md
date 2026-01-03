# AI Writer Free - Complete Edition

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)

A fully-featured AI writing assistant for Android that runs completely on-device with optional web search enhancement.

## ✨ Latest Fixes (v2.0)

### 🎯 Scrolling Issue Fixed
- Wrapped main layout in `ScrollView` with proper viewport configuration
- Content now scrolls smoothly on all screen sizes
- Fixed UI cutoff on smaller devices

### 🛠️ Model Loading Improvements
- **Better error handling** - Clear messages when model isn't loaded
- **Formatting works instantly** - No model download needed for formatting tasks
- **Graceful degradation** - All formatting/styling options work immediately
- **Smart initialization** - Attempts to initialize model only when needed

### 📱 Works Without Model Download
The following features work **immediately** without downloading the 1.5GB AI model:
- ✅ All text formatting (Bold, Italic, Underline, etc.)
- ✅ All case conversions (UPPERCASE, lowercase, Title Case, etc.)
- ✅ All font sizes (Tiny, Small, Large, Huge)
- ✅ All fancy fonts (𝗕𝗼𝗹𝗱, 𝓢𝓬𝓻𝓲𝓹𝓽, Ⓒⓘⓡⓒⓛⓔⓓ, etc.)
- ✅ All emoji decorations (✨ Sparkle, 🔥 Fire, ⭐ Star, 🎉 Celebrate)

## 🔥 Previous Fixes

### Background Download Service
- **NEW**: `ModelDownloadService` - Proper foreground service implementation
- Downloads continue in background even when app is closed
- Progress notifications with cancel button
- Survives activity lifecycle and screen rotation
- Handles network interruptions gracefully
- Proper cleanup on completion/cancellation

### Permissions & Compatibility
- **Android 13+ (API 33+)**: POST_NOTIFICATIONS permission added
- **Android 14+ (API 34+)**: FOREGROUND_SERVICE_DATA_SYNC permission added
- Foreground service properly configured with `dataSync` type
- Runtime permission handling for notifications
- Backward compatibility maintained for older Android versions

### Improved Download Features
- **Resumable**: Temp file prevents corruption
- **Progress tracking**: Real-time MB/MB display
- **Cancel button**: Stop download at any time
- **Notification persistence**: Download continues in background
- **Error handling**: Network failures handled gracefully
- **File verification**: Size check before completion

### App Features Working
All features are fully implemented and working:

#### 🤖 AI Writing (Offline)
- Fix Grammar
- Rewrite (Casual/Formal/Professional)
- Summarize
- Expand
- Simplify

#### 🌐 AI with Web Search (RAG)
- Rewrite with Research
- Expand with Facts
- Update with Current Info
- Fact Check
- Add Citations

#### 🎨 Formatting & Style
- Bold, Italic, Underline
- Heading 1, Heading 2
- Bullet List, Numbered List
- Quote Block
- Emoji decoration (✨🔥⭐🎉)

#### 📝 Case & Size
- UPPERCASE, lowercase, Title Case, Sentence case
- Tiny, Small, Large, Huge

#### ✨ Fancy Fonts
- 𝗕𝗼𝗹𝗱 𝗦𝗮𝗻𝘀, 𝘐𝘵𝘢𝘭𝘪𝘤, 𝓢𝓬𝓻𝓲𝓹𝓽, 𝙼𝚘𝚗𝚘𝚜𝚙𝚊𝚌𝚎, Ⓒⓘⓡⓒⓛⓔⓓ, uǝpᴉsuI

## 📋 Requirements

- Android 8.0 (API 26) or higher
- ~2GB free storage for model
- Internet connection (for initial download and web search features)
- Android SDK 34+ for building

## 🚀 Quick Start

### Build from Source

```bash
# Build the APK
chmod +x build.sh
./build.sh

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### First Run

1. **Launch AI Writer** from app drawer
2. **Tap "Download Model"** - Download happens in background
3. **Download notification appears** - You can close the app
4. **Wait for completion** (~1.5GB download)
5. **Done!** Select text anywhere and choose "AI Writer" from menu

## 🎯 How to Use

### In Any App
1. Select text
2. Tap "..." or "Share"
3. Choose "AI Writer"
4. Select category and action
5. Text is replaced or shown in dialog

## 🏗️ Architecture

### Background Download System
```
MainActivity
    ↓
ModelDownloadService (Foreground Service)
    ↓
Notification with Progress
    ↓
Download Complete → Initialize AI Engine
```

### Text Processing Flow
```
Text Selection → TextProcessorActivity
    ↓
Task Category Selection
    ↓
LocalAIEngine.processText()
    ↓
[Formatting Task] → TextFormatterService (Instant)
[AI Task] → ONNX Runtime (Local Inference)
[Web Task] → RAGEngine + WebSearch → AI Processing
    ↓
Result Returned/Displayed
```

## 📁 Project Structure

```
AIWriterFree/
├── app/
│   ├── src/main/java/com/aiwriter/free/
│   │   ├── MainActivity.kt              # Main UI with download manager
│   │   ├── ModelDownloadService.kt      # Background download service (NEW)
│   │   ├── TextProcessorActivity.kt     # Text selection handler
│   │   ├── LocalAIEngine.kt             # ONNX inference engine
│   │   ├── RAGEngine.kt                 # Web search & context
│   │   ├── TextFormatterService.kt      # Formatting utilities
│   │   ├── WebSearchService.kt          # Web search API
│   │   └── AIWriterApplication.kt       # App singleton
│   ├── AndroidManifest.xml              # Updated with permissions
│   └── build.gradle                     # Dependencies
├── build.gradle                         # Root build config
├── settings.gradle                      # Gradle settings
├── build.sh                             # Build script
└── README.md                            # This file
```

## 🔧 Technical Details

### Model Information
- **Model**: Qwen2.5-1.5B-Instruct (Quantized Q4)
- **Format**: ONNX
- **Size**: ~1.5GB
- **Source**: HuggingFace
- **Inference**: ONNX Runtime with Android NNAPI acceleration

### Dependencies
```gradle
// Core
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0

// Coroutines
kotlinx-coroutines-android:1.7.3

// AI Engine
com.microsoft.onnxruntime:onnxruntime-android:1.16.3

// Tokenizer
com.knuddels:jtokkit:1.0.0
```

### Permissions Explained
- `INTERNET` - Download model & web search
- `ACCESS_NETWORK_STATE` - Check connectivity
- `FOREGROUND_SERVICE` - Run download service
- `FOREGROUND_SERVICE_DATA_SYNC` - Android 14+ requirement
- `POST_NOTIFICATIONS` - Show download progress (Android 13+)

## 🐛 Troubleshooting

### Download Not Starting
- Grant notification permission in app settings
- Check internet connection
- Ensure 2GB+ free space

### Download Interrupted
- Service will retry on next app open
- Delete partial file if needed
- Restart download

### Features Not Working
- Ensure model downloaded successfully
- Check notification for errors
- Restart app to reinitialize engine

## 📝 Development

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Clean
./gradlew clean

# Install and run
./gradlew installDebug
adb shell am start -n com.aiwriter.free/.MainActivity
```

### Testing Download Service
```bash
# Monitor logs
adb logcat | grep ModelDownloadService

# Check notification
adb shell dumpsys notification
```

## 🔐 Privacy

- **100% On-Device AI** - No data sent to servers
- **Local Processing** - Text never leaves device
- **Web Search** (Optional) - Only when using enhanced features
- **No Analytics** - Zero tracking
- **No Ads** - Completely free

## 📄 License

This project is free and open-source.

## 🙏 Credits

- **Qwen2.5** - Alibaba Cloud
- **ONNX Runtime** - Microsoft
- **Material Design** - Google

---

**Version**: 1.0.0  
**Min Android**: 8.0 (API 26)  
**Target Android**: 14 (API 34)  
**Model Size**: 1.5GB
# Force rebuild
