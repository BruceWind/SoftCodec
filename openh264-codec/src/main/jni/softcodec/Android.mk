include $(call all-subdir-makefiles)

LOCAL_PATH := $(call my-dir)



include $(CLEAR_VARS)


LOCAL_MODULE    := softcodec
LOCAL_SHARED_LIBRARIES := libopenh264  \
							librtmp \
							static_aac
LOCAL_LDLIBS := -ldl -lc -lz -lm -llog
LOCAL_SRC_FILES := h264Encoder.cc \
								xiecc_rtmp.c \
								aacEncode.c \
								rtmpManage.c

include $(BUILD_SHARED_LIBRARY)




