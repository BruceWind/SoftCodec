//
// Created by bruce on 5/1/21.
//

#ifndef SOFTCODEC_H264_ENCODER_LOG_H
#define SOFTCODEC_H264_ENCODER_LOG_H


#define LOGTAG "LiveCamera_encoder"
#define LOGI(...) __android_log_print(ANDROID_LOG_DEBUG, LOGTAG, __VA_ARGS__)
#define printf(...) LOGI(__VA_ARGS__)
#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOGTAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO   , LOGTAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN   , LOGTAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR  , LOGTAG, __VA_ARGS__)



#endif //SOFTCODEC_H264_ENCODER_LOG_H
