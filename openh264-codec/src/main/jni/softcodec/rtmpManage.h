#include <jni.h>


/*
 *rtmp的连接
 */
JNIEXPORT jint Java_io_github_brucewind_softcodec_StreamHelper_open(JNIEnv *env,jobject instance, jstring url_);

//TODO does it need to add pause function?

/*
 *断开rtmp的
 */

JNIEXPORT jint Java_io_github_brucewind_softcodec_StreamHelper_stop(JNIEnv *env,jobject instance);
