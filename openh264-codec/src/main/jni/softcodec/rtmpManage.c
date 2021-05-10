#include "rtmpManage.h"
#include "xiecc_rtmp.h"


/*
 *rtmp的连接
 */
JNIEXPORT jint Java_io_github_brucewind_softcodec_StreamHelper_rtmpOpen(JNIEnv *env,
		jobject instance, jstring url_) {

	const char *url = (*env)->GetStringUTFChars(env, url_, 0);

	int result = rtmp_open_for_write(url);

	(*env)->ReleaseStringUTFChars(env, url_, url);

	return result;
}

/*
 *断开rtmp的连接
 */

JNIEXPORT jint Java_io_github_brucewind_softcodec_StreamHelper_rtmpStop(JNIEnv *env,
		jobject instance) {

	int result = stopRtmpConnect();

	return result;
}
