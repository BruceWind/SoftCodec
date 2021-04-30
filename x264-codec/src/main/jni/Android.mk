LOCAL_PATH := $(call my-dir)


#链接静态库 x264
include $(CLEAR_VARS)
LOCAL_MODULE    := static_x264
LOCAL_SRC_FILES := x264/lib/libx264.a
LOCAL_EXPORT_C_INCLUDES := x264 x264/include
include $(PREBUILT_STATIC_LIBRARY) 

#链接静态库 librtmp
include $(CLEAR_VARS)
LOCAL_MODULE := librtmp
LOCAL_SRC_FILES := librtmp/lib/librtmp.a
LOCAL_EXPORT_C_INCLUDES := librtmp/include
include $(PREBUILT_STATIC_LIBRARY)


#链接静态库 fdk-aac
include $(CLEAR_VARS)
LOCAL_MODULE :=static_aac 
LOCAL_SRC_FILES :=fdkaac/lib/libfdk-aac.a
LOCAL_EXPORT_C_INCLUDES := fdkaac/include
include $(PREBUILT_STATIC_LIBRARY)

#生成动态库
include $(CLEAR_VARS) 
LOCAL_MODULE    := share_x264 
LOCAL_STATIC_LIBRARIES := static_x264 \
							librtmp \
							static_aac
LOCAL_LDLIBS := -ldl -lc -lz -lm -llog
LOCAL_SRC_FILES := x264/h264Encoder.c flvmuxer/xiecc_rtmp.c fdkaac/aacEncode.c librtmp/rtmpManage.c
include $(BUILD_SHARED_LIBRARY)




