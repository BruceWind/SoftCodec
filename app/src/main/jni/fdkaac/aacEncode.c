#include "aacEncode.h"
#include "stdbool.h"


AACOutput* fdkencode;

#define LOG_TAG "android-fdkaac"
#define LOGI(...) __android_log_print(4, LOG_TAG, __VA_ARGS__);
#define LOGE(...) __android_log_print(6, LOG_TAG, __VA_ARGS__);


jint Java_com_androidyuan_softcodec_AudioRecorder_initAAC(JNIEnv* env, jobject thiz,
		jint bitrate, jint sample_rate, jint channels) {

	fdkencode=(AACOutput *) malloc(sizeof(AACOutput));
	fdkencode->channels=channels;
	fdkencode->samplerate=sample_rate;
	fdkencode->bitrate=bitrate;

	fdkencode->transMux = 2; //TODO ??
	fdkencode->afterburner = 1; //TODO ??
	fdkencode->channelLoader = 1; //TODO ？？
	fdkencode->aot = 2; //aot类型。

	//先打开编码器，给handle 赋值。
	if (aacEncOpen(&fdkencode->handle, 0, channels) != AACENC_OK) {
		fprintf(stderr, "无法打开编码器\n");
		return -1;
	}
	//设置AOT值
	if (aacEncoder_SetParam(fdkencode->handle, AACENC_AOT, fdkencode->aot)
			!= AACENC_OK) {
		fprintf(stderr, "无法设置AOT值：%d\n", fdkencode->aot);
		return -2;
	}

	//设置采样频率
	if (aacEncoder_SetParam(fdkencode->handle, AACENC_SAMPLERATE, sample_rate)
			!= AACENC_OK) {
		fprintf(stderr, "无法设置采样频率\n");
		return -3;
	}

	//根据声道数设置声道模式。
	switch (channels) {
	case 1:
		fdkencode->mode = MODE_1;
		break;
	case 2:
		fdkencode->mode = MODE_2;
		break;
	default:
		fprintf(stderr, "不支持的声道数 %d\n", channels);
		return -4;
	}
	if (aacEncoder_SetParam(fdkencode->handle, AACENC_CHANNELMODE,
			fdkencode->mode) != AACENC_OK) {
		fprintf(stderr, "无法设置声道模式\n");
		return -5;
	}

	//设置比特率
	if (aacEncoder_SetParam(fdkencode->handle, AACENC_BITRATE, bitrate)
			!= AACENC_OK) {
		fprintf(stderr, "无法设置比特率\n");
		return -6;
	}

	if (aacEncoder_SetParam(fdkencode->handle, AACENC_TRANSMUX, 2)
			!= AACENC_OK) {
		fprintf(stderr, "无法设置 ADTS transmux\n");
		return -7;
	}
	if (aacEncoder_SetParam(fdkencode->handle, AACENC_AFTERBURNER,
			fdkencode->afterburner) != AACENC_OK) {
		fprintf(stderr, "无法设置 afterburner 模式\n");
		return -8;
	}
	if (aacEncEncode(fdkencode->handle, NULL, NULL, NULL, NULL) != AACENC_OK) {
		fprintf(stderr, "无法初始化aac编码器\n");
		return -9;
	}
	if (aacEncInfo(fdkencode->handle, &fdkencode->info) != AACENC_OK) {
		fprintf(stderr, "无法获取到编码器信息\n");
		return -10;
	}

	return 1;

}
jint Java_com_androidyuan_softcodec_AudioRecorder_getbuffersize(JNIEnv* env, jobject thiz){
	int frameLength =fdkencode->info.frameLength; //每帧的长度
	int channels = fdkencode->channels; //获取声道数。
	int input_size = channels * 2 * frameLength; //计算每帧的大小
	fdkencode->input_size=input_size;
	return input_size;
}

jboolean Java_com_androidyuan_softcodec_AudioRecorder_encodeFrame(JNIEnv *pEnv,jobject obj,jbyteArray pcmData) {

	int nArrLen = (*pEnv)->GetArrayLength(pEnv,pcmData);
//	if (fdkencode->input_size != nArrLen) {
////		LOGE("预期输入长度和实际长度不一致： 预期输入长度：%d ， 实际输入长度：%d", fdkencode->input_size, nArrLen);
//	}


//	1字节     uint8_t //	2字节     uint16_t
//	uint8_t* input_buf = (uint8_t*) malloc(input_size);//分配输入空间
	jbyte* arrayBody = (*pEnv)->GetByteArrayElements(pEnv,pcmData, 0);
	uint8_t* input_buf = (uint8_t*) arrayBody;
	int16_t* convert_buf = (int16_t*) malloc(fdkencode->input_size); //分配转换空间

	int read = fdkencode->input_size;
	int i;
	//将8位的值转为16位。
	for ( i = 0; i < read / 2; i++) {
		const uint8_t* in = &input_buf[2 * i];
		convert_buf[i] = in[0] | (in[1] << 8);
	}

	AACENC_BufDesc in_buf = { 0 }, out_buf = { 0 };
	AACENC_InArgs in_args = { 0 };
	AACENC_OutArgs out_args = { 0 };
	int in_identifier = IN_AUDIO_DATA;
	int in_size, in_elem_size;
	int out_identifier = OUT_BITSTREAM_DATA;
	int out_size, out_elem_size;
	void *in_ptr, *out_ptr;
	uint8_t outbuf[20480];

	if (read <= 0) {
		in_args.numInSamples = -1;
	} else {
		in_ptr = convert_buf;
		in_size = read;
		in_elem_size = 2;

		in_args.numInSamples = read / 2;
		in_buf.numBufs = 1;
		in_buf.bufs = &in_ptr;
		in_buf.bufferIdentifiers = &in_identifier;
		in_buf.bufSizes = &in_size;
		in_buf.bufElSizes = &in_elem_size;
	}
	out_ptr = outbuf;
	out_size = sizeof(outbuf);
	out_elem_size = 1;
	out_buf.numBufs = 1;
	out_buf.bufs = &out_ptr;
	out_buf.bufferIdentifiers = &out_identifier;
	out_buf.bufSizes = &out_size;
	out_buf.bufElSizes = &out_elem_size;

	bool success = aacEncoderProcess(&in_buf, &out_buf, &in_args,&out_args);

	if (success) {
		if (out_args.numOutBytes != 0) {

			if(fdkencode->first==0){
//				char  asc[2];
//				asc[0] = 0x10 | ((4 >> 1) & 0x3);
//				asc[1] = ((4 & 0x1) << 7) | ((2 & 0xF) << 3);
//				send_rtmp_audio_spec((unsigned char*)asc, 2);
				send_rtmp_audio_spec(fdkencode->info.confBuf,fdkencode->info.confSize);
				fdkencode->first = 1;
			}
			send_rtmp_audio(outbuf,out_args.numOutBytes,getSystemTime());
		}
	}

	free(convert_buf);
	return 1;
}

bool aacEncoderProcess(AACENC_BufDesc* in_buf,
		AACENC_BufDesc* out_buf,
		AACENC_InArgs* in_args,
		AACENC_OutArgs* out_args) {

	AACENC_ERROR err;

	if ((err = aacEncEncode(fdkencode->handle, in_buf, out_buf, in_args, out_args))
			!= AACENC_OK) {
		if (err == AACENC_ENCODE_EOF) {
			//结尾，正常结束
			return true;
		}
		fprintf(stderr, "编码失败！！err = %d \n", err);
		return false;
	}
	return true;
}

