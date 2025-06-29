cmake_minimum_required(VERSION 3.10)

project(notification C)

set(CMAKE_C_STANDARD 11)

find_package(PkgConfig REQUIRED)
pkg_check_modules(LIBNOTIFY REQUIRED libnotify)

include_directories(${LIBNOTIFY_INCLUDE_DIRS})
link_directories(${LIBNOTIFY_LIBRARY_DIRS})

add_library(notification SHARED
        linux_notification_library.c
        linux_notification_library.h)

target_link_libraries(notification ${LIBNOTIFY_LIBRARIES})
