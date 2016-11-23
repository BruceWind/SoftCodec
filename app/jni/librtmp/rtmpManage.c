#include "rtmpManage.h"


/*
 *rtmp的连接
 */
JNIEXPORT jint Java_com_androidyuan_softcodec_MainActivity_Rtmpopen(JNIEnv *env,
		jobject instance, jstring url_) {

	const char *url = (*env)->GetStringUTFChars(env, url_, 0);

	int result = rtmp_open_for_write(url);

	(*env)->ReleaseStringUTFChars(env, url_, url);

	return result;
}

/*
 *断开rtmp的连接
 */

JNIEXPORT jint Java_com_androidyuan_softcodec_MainActivity_Rtmpstop(JNIEnv *env,
		jobject instance) {

	int result = stopRtmpConnect();

	return result;
}
