include $(call all-subdir-makefiles)

LOCAL_PATH := $(call my-dir)



include $(CLEAR_VARS) 
LOCAL_MODULE    := share_x264
LOCAL_SHARED_LIBRARIES := libx264
LOCAL_STATIC_LIBRARIES :=  librtmp \
							static_aac
LOCAL_LDLIBS := -ldl -lc -lz -lm -llog
LOCAL_SRC_FILES := h264Encoder.c \
								xiecc_rtmp.c \
								aacEncode.c \
								rtmpManage.c
include $(BUILD_SHARED_LIBRARY)




