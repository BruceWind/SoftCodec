LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libopenh264

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_SRC_FILES := libs/arm64-v8a/libopenh264.a
endif

ifeq ($(TARGET_ARCH_ABI),x86)
    LOCAL_SRC_FILES := libs/x86/libopenh264.a
endif

LOCAL_EXPORT_C_INCLUDES := include/wels
include $(PREBUILT_STATIC_LIBRARY)
