# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1 FATAL_ERROR)
project("openh264-codec")

###------librtmp begin--------
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/librtmp) # directory libs is generated from a shell script.
add_library(libtrmp STATIC IMPORTED)
#message("Current build include : ${distribution_DIR}/${ANDROID_ABI}/libopenh264.a")
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/librtmp)
###------librtmp end --------

###------dfk-aac begin--------
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/fdkaac) # directory libs is generated from a shell script.
add_library(fdkaac STATIC IMPORTED)
set_target_properties(fdkaac PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/lib/${ANDROID_ABI}/libfdk-aac.a)
#message("Current build include : ${distribution_DIR}/${ANDROID_ABI}/libopenh264.a")
include_directories(${distribution_DIR}/include)
###------dfk-aac end --------

###------libopenh264 begin--------
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libopenh264/libs) # directory libs is generated from a shell script.
add_library(libopenh264 STATIC IMPORTED)
set_target_properties(libopenh264 PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/${ANDROID_ABI}/libopenh264.a)
#message("Current build include : ${distribution_DIR}/${ANDROID_ABI}/libopenh264.a")
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/libopenh264/include/wels)
###------libopenh264 end --------




add_library( # Sets the name of the library.
        softcodec
        # Sets the library as a shared library.
        SHARED
        # source files
        softcodec/aacEncode.h
        softcodec/aacEncode.c
        softcodec/h264Encoder.cc
        softcodec/h264Encoder.h
        softcodec/rtmpManage.c
        softcodec/xiecc_rtmp.c)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(
        log-lib
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        softcodec
        libopenh264
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})