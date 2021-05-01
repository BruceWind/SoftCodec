#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <stdlib.h>
#include <../libx264/include/x264.h>
#include "xiecc_rtmp.h"

/*compressBegin
 *初始化x264
 *width 宽度
 *height 高度
 *bitrate 码率
 *fps 幀率
 */
JNIEXPORT jlong Java_io_github_brucewind_softcodec_StreamHelper_compressBegin(JNIEnv* env,
		jobject thiz, jint width, jint height, jint bitrate, jint fps);

/*compressEnd
 *結束x264编码
 *handle
 */
JNIEXPORT jint Java_io_github_brucewind_softcodec_StreamHelper_compressEnd(JNIEnv* env,
		jobject thiz, jlong handle);

/*compressBegin
 *编码x264
 *handle
 *in nv12
 *insize nv12 长度
 *out h264
 */
JNIEXPORT jint Java_io_github_brucewind_softcodec_StreamHelper_compressBuffer(JNIEnv* env,
		jobject thiz,
		jlong handle,
		jbyteArray in,
		jint insize,
		jbyteArray out);
