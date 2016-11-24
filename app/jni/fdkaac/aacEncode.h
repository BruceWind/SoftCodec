#include "fdkaac/include/fdk-aac/aacenc_lib.h"
#include <stdio.h>
#include <stdint.h>
#include <unistd.h>
#include <stdlib.h>
#include <jni.h>
#include "xiecc_rtmp.h"
#include "stdbool.h"



typedef struct AudioOutput {
	int transMux;  //TODO ??
	int afterburner;  //TODO ??
	int channelLoader; //TODO ？？
	int aot ; //aot类型。
	int channels;
	int samplerate;
	int bitrate;
	CHANNEL_MODE mode; //通道模式。
	HANDLE_AACENCODER handle; //aac处理实例
	AACENC_InfoStruct info;
	int input_size;
	int first;
}AACOutput;

typedef struct EncodeAAC Encode;
jint Java_com_androidyuan_softcodec_AudioRecorder_initAAC(JNIEnv* env,jobject thiz, jint bitrate,jint samplerate,jint channels);
jint Java_com_androidyuan_softcodec_AudioRecorder_getbuffersize(JNIEnv* env, jobject thiz);
jboolean Java_com_androidyuan_softcodec_AudioRecorder_encodeFrame(JNIEnv *pEnv,jobject obj,jbyteArray pcmData);
bool aacEncoderProcess(AACENC_BufDesc* in_buf,AACENC_BufDesc* out_buf,AACENC_InArgs* in_args,AACENC_OutArgs* out_args);
