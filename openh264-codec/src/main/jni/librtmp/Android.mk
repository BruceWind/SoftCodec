## depend librtmp with source code.
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := librtmp

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_SRC_FILES :=  \
    amf.c       \
    log.c       \
    parseurl.c  \
    rtmp.c      \
    hashswf.c

LOCAL_CFLAGS := -DRTMPDUMP_VERSION=v2.4 -DNO_CRYPTO

include $(BUILD_STATIC_LIBRARY)
