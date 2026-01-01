#!/bin/bash

# AI Writer Free - Build Script
# This script builds the Android APK

echo "AI Writer Free - Build Script"
echo "=============================="
echo ""

# Check if Android SDK is available
if [ -z "$ANDROID_HOME" ]; then
    echo "Error: ANDROID_HOME not set"
    echo "Please set ANDROID_HOME to your Android SDK location"
    echo "Example: export ANDROID_HOME=~/Android/Sdk"
    exit 1
fi

echo "Android SDK: $ANDROID_HOME"
echo ""

# Make gradlew executable
chmod +x gradlew

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo ""
echo "Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Build successful!"
    echo ""
    echo "APK location:"
    echo "  app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "To install on device:"
    echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
    echo ""
else
    echo ""
    echo "✗ Build failed!"
    exit 1
fi
