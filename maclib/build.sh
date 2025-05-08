#!/bin/bash

# Exit on error
set -e

echo "Building MacNotification library..."

# Create Info.plist file for bundle identification
cat > Info.plist << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleIdentifier</key>
    <string>io.github.kdroidfilter.knotify</string>
    <key>CFBundleName</key>
    <string>KNotify</string>
    <key>CFBundleVersion</key>
    <string>1.0</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0</string>
    <key>NSHumanReadableCopyright</key>
    <string>Copyright © 2023 KDroidFilter. All rights reserved.</string>
</dict>
</plist>
EOF

echo "Building for ARM64..."

# Compile Swift file directly to dylib with bundle info for ARM64 (Apple Silicon)
swiftc -emit-library -o ../knotify/src/jvmMain/resources/darwin-aarch64/libMacNotification.dylib \
    -module-name MacNotification \
    -swift-version 5 \
    -O -whole-module-optimization \
    -framework Foundation \
    -framework UserNotifications \
    -Xlinker -rpath -Xlinker @executable_path/../Frameworks \
    -Xlinker -rpath -Xlinker @loader_path/Frameworks \
    -Xlinker -sectcreate -Xlinker __TEXT -Xlinker __info_plist -Xlinker Info.plist \
    mac_notification_library.swift

echo "Building for x86_64..."

# Compile Swift file directly to dylib with bundle info for x86_64 (Intel)
swiftc -emit-library -o ../knotify/src/jvmMain/resources/darwin-x86-64/libMacNotification.dylib \
    -module-name MacNotification \
    -swift-version 5 \
    -target x86_64-apple-macosx10.14 \
    -O -whole-module-optimization \
    -framework Foundation \
    -framework UserNotifications \
    -Xlinker -rpath -Xlinker @executable_path/../Frameworks \
    -Xlinker -rpath -Xlinker @loader_path/Frameworks \
    -Xlinker -sectcreate -Xlinker __TEXT -Xlinker __info_plist -Xlinker Info.plist \
    mac_notification_library.swift

# Clean up temporary files
rm -f Info.plist

echo "Build completed successfully."
