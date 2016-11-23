#include <jni.h>
#include "flvmuxer/xiecc_rtmp.h"


/*
 *rtmp的连接
 */
JNIEXPORT jint Java_com_androidyuan_softcodec_MainActivity_open(JNIEnv *env,jobject instance, jstring url_);


/*
 *断开rtmp的
 */

JNIEXPORT jint Java_com_androidyuan_softcodec_MainActivity_stop(JNIEnv *env,jobject instance);
