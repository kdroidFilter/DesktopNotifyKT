cmake_minimum_required(VERSION 3.10)

project(NotificationExample C)

set(CMAKE_C_STANDARD 11)

find_package(PkgConfig REQUIRED)
pkg_check_modules(LIBNOTIFY REQUIRED libnotify)

include_directories(${LIBNOTIFY_INCLUDE_DIRS})
link_directories(${LIBNOTIFY_LIBRARY_DIRS})

add_executable(NotificationExample main.c
        notification_library.c
        notification_library.h)

target_link_libraries(NotificationExample ${LIBNOTIFY_LIBRARIES})
