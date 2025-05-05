#!/bin/bash

# Exit on error
set -e

echo "Building MacNotification library..."

# Create output directories if they don't exist
mkdir -p ../lib
mkdir -p ../../../resources

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

# Compile Swift file directly to dylib with bundle info
swiftc -emit-library -o ../../../resources/libMacNotification.dylib \
    -module-name MacNotification \
    -swift-version 5 \
    -O -whole-module-optimization \
    -framework Foundation \
    -framework UserNotifications \
    -Xlinker -rpath -Xlinker @executable_path/../Frameworks \
    -Xlinker -rpath -Xlinker @loader_path/Frameworks \
    -Xlinker -sectcreate -Xlinker __TEXT -Xlinker __info_plist -Xlinker Info.plist \
    mac_notification_library.swift

# Clean up temporary files
rm -f Info.plist

echo "Build completed successfully. Library is available at knotify/src/jvmMain/resources"
