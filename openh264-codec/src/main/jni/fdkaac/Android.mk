LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

include $(CLEAR_VARS)
LOCAL_MODULE :=static_aac


ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_SRC_FILES := lib/arm64-v8a/libfdk-aac.a
endif

ifeq ($(TARGET_ARCH_ABI),x86)
    LOCAL_SRC_FILES := lib/x86/libfdk-aac.a
endif


LOCAL_EXPORT_C_INCLUDES := include
include $(PREBUILT_STATIC_LIBRARY)

