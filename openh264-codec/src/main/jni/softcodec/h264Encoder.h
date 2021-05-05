
#include <stdint.h>
#include <stdio.h>
#include <android/log.h>
#include "xiecc_rtmp.h"
#include "../libopenh264/include/wels/codec_app_def.h"
#include "../libopenh264/include/wels/codec_api.h"
#include "h264_encoder_log.h"


JNIEXPORT jlong
Java_io_github_brucewind_softcodec_StreamHelper_compressBegin(JNIEnv * env ,
jobject thiz, jint
width , jint height, jint
bitrate , jint fps);

JNIEXPORT jint
Java_io_github_brucewind_softcodec_StreamHelper_compressEnd(JNIEnv * env ,
jobject thiz, jlong
handle );

JNIEXPORT jint
Java_io_github_brucewind_softcodec_StreamHelper_compressBuffer(JNIEnv * env ,
jobject thiz,
    jlong
handle ,
jbyteArray in,
    jint
insize ,
jbyteArray out );