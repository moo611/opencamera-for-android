cmake_minimum_required(VERSION 3.10.2)


set(distribution_DIR ../../../../libs)

add_library( # Sets the name of the library.
        yuv-decoder

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        yuv-decoder.c)



find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)



target_link_libraries( # Specifies the target library.
        yuv-decoder

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        GLESv2
        jnigraphics)