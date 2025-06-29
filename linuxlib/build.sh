#!/bin/bash

# Exit on error
set -e

echo "Building LinuxNotification library..."

# Build the shared library using gcc
gcc -shared -o ../knotify/src/jvmMain/resources/linux-x86-64/libnotification.so -fPIC linux_notification_library.c $(pkg-config --cflags --libs libnotify glib-2.0 gdk-pixbuf-2.0)

echo "Build completed successfully."