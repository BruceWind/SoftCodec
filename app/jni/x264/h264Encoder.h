#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <stdlib.h>
#include <x264/include/x264.h>
#include "flvmuxer/xiecc_rtmp.h"

/*CompressBegin
 *初始化x264
 *width 宽度
 *height 高度
 *bitrate 码率
 *fps 幀率
 */
JNIEXPORT jlong Java_com_androidyuan_softcodec_MainActivity_CompressBegin(JNIEnv* env,
		jobject thiz, jint width, jint height, jint bitrate, jint fps);

/*CompressEnd
 *結束x264编码
 *handle
 */
JNIEXPORT jint Java_com_androidyuan_softcodec_MainActivity_CompressEnd(JNIEnv* env,
		jobject thiz, jlong handle);

/*CompressBegin
 *编码x264
 *handle
 *in nv12
 *insize nv12 长度
 *out h264
 */
JNIEXPORT jint Java_com_androidyuan_softcodec_MainActivity_CompressBuffer(JNIEnv* env,
		jobject thiz,
		jlong handle,
		jbyteArray in,
		jint insize,
		jbyteArray out);
