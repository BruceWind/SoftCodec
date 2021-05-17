
#include "h264Encoder.h"

static ISVCEncoder *_encoder;
static SEncParamExt param;


unsigned char sps[30];
unsigned char pps[10];
int first = 0;
int sps_len;
int pps_len;

#define RC_MARGIN 10000 /*bits per sec*/
extern "C" JNIEXPORT  jlong
Java_io_github_brucewind_softcodec_StreamHelper_compressBegin(JNIEnv *env,
                                                              jobject thiz,
                                                              jint width,
                                                              jint height,
                                                              jint bitrate,
                                                              jint fps) {

    ISVCEncoder *encoder;
    WelsCreateSVCEncoder(&encoder);
    encoder->GetDefaultParams(
            &param);//call InitializeExt must have a extension parameter with this default.
    param.iUsageType = CAMERA_VIDEO_REAL_TIME;  //tell openH264 steam type is LIVE.
    param.iRCMode = RC_QUALITY_MODE;
    param.fMaxFrameRate = fps;                  //fps
    param.iPicWidth = width;
    param.iPicHeight = height;
    param.iTargetBitrate = bitrate * 1024;        //the input parameter need to * 1024
//    param.iInputCsp = videoFormatI420;          //it is stable YUV format for openh264
    param.bEnableDenoise = 1;                   //enable eliminating noisy.
    param.iSpatialLayerNum = 1;
    //if (sliceMode != SM_SINGLE_SLICE && sliceMode != SM_DYN_SLICE) //SM_DYN_SLICE don't support multi-thread now
    param.iMultipleThreadIdc = 2;
    for (int i = 0; i < param.iSpatialLayerNum; i++) {
        param.sSpatialLayers[i].iVideoWidth = width >> (param.iSpatialLayerNum - 1 - i);
        param.sSpatialLayers[i].iVideoHeight = height >> (param.iSpatialLayerNum - 1 - i);
        param.sSpatialLayers[i].fFrameRate = fps;
        param.sSpatialLayers[i].iSpatialBitrate = param.iTargetBitrate;
        //param.sSpatialLayers[i].sSliceCfg.uiSliceMode = sliceMode;
    }
    param.iTargetBitrate *= param.iSpatialLayerNum;
    int inited_status = encoder->InitializeExt(&param);

    if (inited_status == 0) {
        _encoder = encoder;
    } else {
        ALOGE("encoder init failed.");
        return 0;
    }
    return (jlong) encoder;
}

/**
 * When compress end, clear some resource
 */
extern "C" JNIEXPORT  jint
Java_io_github_brucewind_softcodec_StreamHelper_compressEnd(
        JNIEnv *env,
        jobject thiz,
        jlong handle
) {

    return 0;
}

/**
 * There are two steps:
 * 1. compressing the buffer data into serveral NALUs;
 * 2. transferring the NALUs to RTMP server.
 */
extern "C" JNIEXPORT  jint Java_io_github_brucewind_softcodec_StreamHelper_compressBuffer(
        JNIEnv *env,
        jobject thiz,
        jlong handler,//it is a pointer.
        jbyteArray in,
        jint input_size,
        jbyteArray out) {


    if (!isConnected())
        return 0;


    int width = param.iPicWidth;
    int height = param.iPicHeight;


    //find initialized encode by pointer address.
    ISVCEncoder *en = (ISVCEncoder *) _encoder;

    int i_data = 0;
    int count_of_NALU = -1;//A frame may be separated into saveral NALU.
    int result = 0;
    int i = 0;
    int i_frame_size = 0;
    int j = 0;
    int nPix = 0;
//  jbyte *c_array = env->GetByteArrayElements(j_array, 0);
    jbyte *nv12_buf = (jbyte *) env->GetByteArrayElements(in, 0);
    jbyte *h264_buf = (jbyte *) env->GetByteArrayElements(out, 0);
    unsigned char *pTemp = (unsigned char *) h264_buf;
    int nPicSize = width * height;


    //1. the first step: encode YUV420 into H264 NALUs.

    //encode and  store ouput bistream
    int frameSize = width * height * 3 / 2;

    SFrameBSInfo info;
    memset(&info, 0, sizeof(SFrameBSInfo));
    SSourcePicture pic;
    memset(&pic, 0, sizeof(SSourcePicture));
    pic.iPicWidth = width;
    pic.iPicHeight = height;
    pic.iColorFormat = videoFormatI420;//OpenH264 encoder only support this format.
    pic.iStride[0] = pic.iPicWidth;
    pic.iStride[1] = pic.iStride[2] = pic.iPicWidth >> 1;
    pic.pData[0] = reinterpret_cast<unsigned char *>(nv12_buf);
    pic.pData[1] = pic.pData[0] + width * height;
    pic.pData[2] = pic.pData[1] + (width * height >> 2);

    //prepare input data
    int rv = _encoder->EncodeFrame(&pic, &info);
    if (rv != cmResultSuccess) {//failed.
        if (rv == cmInitParaError) {
            ALOGE("openh264 parameter is wrong.");
        } else {
            ALOGE("encodeFrame falied, the result is %d.", rv);
        }
        return -1;
    }
    if (info.eFrameType == videoFrameTypeSkip) {//it need to skip.
        ALOGW("skip this frame");
        return 0;
    } else {
        ALOGD("it has encoded a frame.");
    }

    if (info.iLayerNum <= 0) {
        ALOGE("something wrong in \"i_frame_size < 0\".");
        return -1;
    }

    /**
     * 2. The second step:
     * transmit serveral NALUs to RTMP server.
     * Two NAL types : SPS & PPS are very important.
     */
    for (i = 0; i < info.iLayerNum; i++) {//iLayerNum is the count of  NALUs.
        int type = info.sLayerInfo[i].uiLayerType;
        if (type != 1) {
            ALOGD("foreach NAL type : %d.", info.sLayerInfo[i].uiLayerType);
        }
        if (info.sLayerInfo[i].uiLayerType == NAL_SPS) {//this NAL type is SPS.
            ALOGD("got SPS.");
//      sps_len = en->nal[i].i_payload - 4;
//      memcpy(sps, en->nal[i].p_payload + 4, sps_len);
        } else if (info.sLayerInfo[i].uiLayerType == NAL_PPS) {//this NAL type is PPS.
            ALOGD("got PPS.");
//      pps_len = en->nal[i].i_payload - 4;
//      memcpy(pps, en->nal[i].p_payload + 4, pps_len);
//      if (first == 0) {
//        send_video_sps_pps(sps, sps_len, pps, pps_len);
//        first = 1;
//      }
        } else {//TODO
      send_rtmp_video(info.sLayerInfo[i].pBsBuf,
                      info.sLayerInfo[i].pNalLengthInByte[0],
                      getSystemTime());     //into.uiTimeStamp
            ALOGD("send_rtmp_video() end");
        }

        //release buffers.
        env->ReleaseByteArrayElements(in, nv12_buf, 0);
        env->ReleaseByteArrayElements(out, h264_buf, 0);
    }
    return result;
}
