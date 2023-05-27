cmake_minimum_required(VERSION 3.4.1)



include_directories(
        ${CMAKE_SOURCE_DIR}/util
        ${CMAKE_SOURCE_DIR}/render
        ${CMAKE_SOURCE_DIR}/glm
        ${CMAKE_SOURCE_DIR}/context
        ${CMAKE_SOURCE_DIR}/filter
)

file(GLOB src-files
        ${CMAKE_SOURCE_DIR}/JniImpl.cpp
        ${CMAKE_SOURCE_DIR}/render/*.cpp
        ${CMAKE_SOURCE_DIR}/context/RenderContext.cpp
#        ${CMAKE_SOURCE_DIR}/filter/*.cpp
        ${CMAKE_SOURCE_DIR}/util/*.cpp
        )

add_library(

        opencamera-android
        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${src-files}
)


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)


target_link_libraries( # Specifies the target library.
        opencamera-android

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        EGL
        GLESv3
        android)