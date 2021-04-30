
#include "h264Encoder.h"

typedef struct {
	x264_param_t *param;
	x264_t *handle;
	x264_picture_t *picture;
	x264_nal_t *nal;


} Encoder;

unsigned char sps[30];
unsigned char pps[10];
int first=0;
int sps_len;
int pps_len;
#define RC_MARGIN 10000 /*bits per sec*/
#define LOGTAG "LiveCamera_encoder"
#define LOGI(...) __android_log_print(ANDROID_LOG_DEBUG, LOGTAG, __VA_ARGS__)
#define printf(...) LOGI(__VA_ARGS__)
#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOGTAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO   , LOGTAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN   , LOGTAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR  , LOGTAG, __VA_ARGS__)


JNIEXPORT jlong Java_io_github_brucewind_softcodec_StreamHelper_compressBegin(JNIEnv* env,
		jobject thiz, jint width, jint height, jint bitrate, jint fps) {


	Encoder *en = (Encoder*) malloc(sizeof(Encoder));
		en->param = (x264_param_t*) malloc(sizeof(x264_param_t));
		en->picture = (x264_picture_t*) malloc(sizeof(x264_picture_t));
		x264_param_default(en->param); //set default param
		x264_param_default_preset(en->param, "veryfast", "zerolatency"); //set encoder params
		en->param->i_width = width;
		en->param->i_height = height;
		en->param->i_threads = 1;/* encode multiple frames in parallel */
		en->param->b_sliced_threads = 1;
		en->param->i_bframe_adaptive = X264_B_ADAPT_FAST;
		en->param->i_bframe_pyramid = X264_B_PYRAMID_NONE; /*允许部分B为参考帧,可选值为0，1，2 */
		en->param->b_intra_refresh = 0; //用周期帧内刷新替代IDR
		en->param->analyse.i_trellis = 1; /* Trellis量化，对每个8x8的块寻找合适的量化值，需要CABAC，默认0 0：关闭1：只在最后编码时使用2：一直使用*/
		en->param->analyse.b_chroma_me = 1;/* 亚像素色度运动估计和P帧的模式选择 */
		en->param->b_interlaced = 0;/* 隔行扫描 */
		en->param->analyse.b_transform_8x8 = 1;/* 帧间分区*/
		en->param->rc.f_qcompress = 0;/* 0.0 => cbr, 1.0 => constant qp */
		en->param->i_frame_reference = 4;/*参考帧的最大帧数。*/
		en->param->i_bframe = 0; /*两个参考帧之间的B帧数目*/
		en->param->analyse.i_me_range = 16;/* 整像素运动估计搜索范围 (from predicted mv) */
		en->param->analyse.i_me_method = X264_ME_DIA;/* 运动估计算法 (X264_ME_*)*/
		en->param->rc.i_lookahead = 0;
		en->param->i_keyint_max = 30;/* 在此间隔设置IDR关键帧(每过多少帧设置一个IDR帧) */
		en->param->i_scenecut_threshold = 40;/*如何积极地插入额外的I帧 */
		en->param->rc.i_qp_min = 10; //关键帧最小间隔
		en->param->rc.i_qp_max = 50; //关键帧最大间隔
		en->param->rc.i_qp_constant = 20;
		en->param->rc.i_bitrate = bitrate; /*设置平均码率 */
		en->param->i_fps_num = fps;/*帧率*/
		en->param->i_fps_den = 1;/*用两个整型的数的比值，来表示帧率*/
		en->param->b_annexb = 1; //如果设置了该项，则在每个NAL单元前加一个四字节的前缀符
		en->param->b_cabac = 0;
		en->param->rc.i_rc_method = X264_RC_ABR; //参数i_rc_method表示码率控制，CQP(恒定质量)，CRF(恒定码率)，ABR(平均码率)

		x264_param_apply_profile(en->param, "baseline");

		if ((en->handle = x264_encoder_open(en->param)) == 0) {
			return 0;
		}
		en->picture->i_type = X264_TYPE_AUTO;
		//create a new pic
		x264_picture_alloc(en->picture, X264_CSP_I420, en->param->i_width,
				en->param->i_height);

		return (jlong) en;

}

/**
 * When compress end, clear some resource
 */
JNIEXPORT jint Java_io_github_brucewind_softcodec_StreamHelper_compressEnd(JNIEnv* env,
		jobject thiz, jlong handle) {
	Encoder *en = (Encoder*) handle;
	ALOGE("begin free!");
	if (en->picture) {
		ALOGE("begin free picture");
		x264_picture_clean(en->picture);
		free(en->picture);
		en->picture = 0;
	}

	ALOGE("after free picture");

	if (en->param) {
		free(en->param);
		en->param = 0;
	}

	ALOGE("after free param");

	if (en->handle) {
		x264_encoder_close(en->handle);
	}
	ALOGE("after close encoder");

	free(en);

	ALOGE("after free encoder");

	return 0;
}

/**
 * compress the buffer data
 */
JNIEXPORT jint Java_io_github_brucewind_softcodec_StreamHelper_compressBuffer(JNIEnv* env,jobject thiz, jlong handle, jbyteArray in, jint insize,jbyteArray out) {


    if(!isConnected())
        return 0;


	Encoder *en = (Encoder*) handle;
	x264_picture_t pic_out;
	int i_data = 0;
	int nNal = -1;
	int result = 0;
	int i = 0;
	int i_frame_size=0;
	int j = 0;
	int nPix = 0;

	jbyte *Buf = (jbyte*) (*env)->GetByteArrayElements(env, in, 0);
	jbyte *h264Buf = (jbyte*) (*env)->GetByteArrayElements(env, out, 0);
	unsigned char *pTemp = h264Buf;
	int nPicSize = en->param->i_width * en->param->i_height;

	/**
	 * YUV => 4:2:0
	 * YYYY
	 * YYYY
	 * UVUV
	 */
	jbyte *y = en->picture->img.plane[0];
	jbyte *v = en->picture->img.plane[1];
	jbyte *u = en->picture->img.plane[2];

	memcpy(en->picture->img.plane[0], Buf, nPicSize);
	for (i = 0; i < nPicSize / 4; i++) {
		*(u + i) = *(Buf + nPicSize + i * 2);
		*(v + i) = *(Buf + nPicSize + i * 2 + 1);
	}


    //this line is slow in  nexus5.
	i_frame_size=x264_encoder_encode(en->handle, &(en->nal), &nNal, en->picture,
			&pic_out) ;

	if( i_frame_size < 0) {
		return -1;
	}

	for (i = 0; i < nNal; i++) {
		memcpy(pTemp, en->nal[i].p_payload, en->nal[i].i_payload);

		if (en->nal[i].i_type == NAL_SPS) {
			sps_len = en->nal[i].i_payload - 4;
			memcpy(sps, en->nal[i].p_payload + 4, sps_len);
		} else if (en->nal[i].i_type == NAL_PPS) {
			pps_len = en->nal[i].i_payload - 4;
			memcpy(pps, en->nal[i].p_payload + 4, pps_len);
			if (first == 0) {
				send_video_sps_pps(sps, sps_len, pps, pps_len);
				first=1;
			}
		} else {
			send_rtmp_video(en->nal[i].p_payload,i_frame_size-result,getSystemTime());
		}
		pTemp += en->nal[i].i_payload;
		result += en->nal[i].i_payload;

		//release the buffer
		(*env)->ReleaseByteArrayElements(env, in, Buf, 0);
		(*env)->ReleaseByteArrayElements(env, out, h264Buf, 0);
	}
	return result;
}
