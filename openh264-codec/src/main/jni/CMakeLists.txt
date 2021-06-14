cmake_minimum_required(VERSION 3.4.1 FATAL_ERROR)
project("openh264-codec")


###------libdfk-aac begin--------
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/fdkaac) # directory libs is generated from a shell script.
add_library(fdkaac STATIC IMPORTED)
set_target_properties(fdkaac PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/lib/${ANDROID_ABI}/libfdk-aac.a)
include_directories(${distribution_DIR}/include)
###------libdfk-aac end --------

###------libopenh264 begin--------
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libopenh264/libs) # directory libs is generated from a shell script.
add_library(libopenh264 STATIC IMPORTED)
set_target_properties(libopenh264 PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/${ANDROID_ABI}/libopenh264.a)
#message("Current build include : ${distribution_DIR}/${ANDROID_ABI}/libopenh264.a")
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/libopenh264/include/wels)
###------libopenh264 end --------

set(RTMP_DIR ${CMAKE_CURRENT_SOURCE_DIR}/librtmp)


add_library(
        softcodec  SHARED
        # source files
        softcodec/rtmpManage.c
        softcodec/xiecc_rtmp.c
        softcodec/aacEncode.h
        softcodec/aacEncode.c
        softcodec/h264Encoder.cc
        softcodec/h264Encoder.h
        softcodec/print_hex.cc

        # import lib rtmp code source.
        ${RTMP_DIR}/amf.c
        ${RTMP_DIR}/amf.h
        ${RTMP_DIR}/bytes.h
        ${RTMP_DIR}/dh.h
        ${RTMP_DIR}/dhgroups.h
        ${RTMP_DIR}/handshake.h
        ${RTMP_DIR}/hashswf.c
        ${RTMP_DIR}/http.h
        ${RTMP_DIR}/log.c
        ${RTMP_DIR}/log.h
        ${RTMP_DIR}/parseurl.c
        ${RTMP_DIR}/rtmp.c
        ${RTMP_DIR}/rtmp.h
        ${RTMP_DIR}/rtmp_sys.h
)


include_directories(${RTMP_DIR})
add_definitions(-DNO_CRYPTO)
add_definitions(-DRTMPDUMP_VERSION=v2.4)


find_library(
        log-lib
        log)

# target linklibs must at end of CMakeLists.txt
target_link_libraries( # Specifies the target library.
        softcodec
        libopenh264
        fdkaac
#        librtmp
        ${log-lib})