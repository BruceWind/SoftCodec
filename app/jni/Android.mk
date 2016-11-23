LOCAL_PATH := $(call my-dir)


#链接静态库 x264
include $(CLEAR_VARS)
LOCAL_MODULE    := static_x264
LOCAL_SRC_FILES := x264/lib/libx264.a 
include $(PREBUILT_STATIC_LIBRARY) 

#链接静态库 librtmp
include $(CLEAR_VARS)
LOCAL_MODULE := rtmp 
LOCAL_SRC_FILES := librtmp/lib/librtmp.a
include $(PREBUILT_STATIC_LIBRARY)


#链接静态库 fdk-aac
include $(CLEAR_VARS)
LOCAL_MODULE :=static_aac 
LOCAL_SRC_FILES :=fdkaac/lib/libfdk-aac.a
include $(PREBUILT_STATIC_LIBRARY)

#生成动态库
include $(CLEAR_VARS) 
LOCAL_MODULE    := share_x264 
LOCAL_STATIC_LIBRARIES := static_x264 \ rtmp \ static_aac
LOCAL_LDLIBS := -ldl -lc -lz -lm -llog #加入android Log。
LOCAL_C_INCLUDES = librtmp/include  flvmuxer fdkaac/include  faac/include
LOCAL_SRC_FILES := x264/h264Encoder.c flvmuxer/xiecc_rtmp.c fdkaac/aacEncode.c librtmp/rtmpManage.c
include $(BUILD_SHARED_LIBRARY)




