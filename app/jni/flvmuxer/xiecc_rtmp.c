#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include "rtmp.h"
#include "log.h"
#include "x264/include/x264.h"
#include "xiecc_rtmp.h"
#include <android/log.h>

#define RTMP_HEAD_SIZE (sizeof(RTMPPacket)+RTMP_MAX_HEADER_SIZE)

#define  LOG_TAG    "rtmp-muxer"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

RTMP *rtmp;
int StartTime;
int rtmp_open_for_write(const char * url) {
	rtmp = RTMP_Alloc();
	if (rtmp == NULL) {
		return -1;
	}

	RTMP_Init(rtmp);
	int ret = RTMP_SetupURL(rtmp, url);

	if (!ret) {
		RTMP_Free(rtmp);
		return -2;
	}

	RTMP_EnableWrite(rtmp);

	ret = RTMP_Connect(rtmp, NULL);
	if (!ret) {
		RTMP_Free(rtmp);
		return -3;
	}
	ret = RTMP_ConnectStream(rtmp, 0);

	if (!ret) {
		return -4;
	}

	StartTime=getSystemTime();

	return 1;
}
void send_video_sps_pps(uint8_t *sps, int sps_len, uint8_t *pps, int pps_len) {
	RTMPPacket * packet;
	unsigned char * body;
	int i;
	if (rtmp != NULL) {
		packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + 1024);
		memset(packet, 0, RTMP_HEAD_SIZE);

		packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
		body = (unsigned char *) packet->m_body;
		i = 0;
		body[i++] = 0x17;
		body[i++] = 0x00;

		body[i++] = 0x00;
		body[i++] = 0x00;
		body[i++] = 0x00;

		/*AVCDecoderConfigurationRecord*/
		body[i++] = 0x01;
		body[i++] = sps[1];
		body[i++] = sps[2];
		body[i++] = sps[3];
		body[i++] = 0xff;

		/*sps*/
		body[i++] = 0xe1;
		body[i++] = (sps_len >> 8) & 0xff;
		body[i++] = sps_len & 0xff;
		memcpy(&body[i], sps, sps_len);

		i += sps_len;

		/*pps*/
		body[i++] = 0x01;
		body[i++] = (pps_len >> 8) & 0xff;
		body[i++] = (pps_len) & 0xff;
		memcpy(&body[i], pps, pps_len);
		i += pps_len;

		packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
		packet->m_nBodySize = i;
		packet->m_nChannel = 0x04;

		packet->m_nTimeStamp = 0;
		packet->m_hasAbsTimestamp = 0;
		packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
		packet->m_nInfoField2 = rtmp->m_stream_id;

		if (RTMP_IsConnected(rtmp)) {
			//调用发送接口
			int success = RTMP_SendPacket(rtmp, packet, TRUE);
			if (success != 1) {
				LOGE("send_video_sps_pps fail");
			}
		} else {
			LOGE("send_video_sps_pps RTMP is not ready");
		}
		free(packet);
	}
}
void send_rtmp_video(uint8_t *data, int data_len, int timestamp) {
	int type;
	RTMPPacket * packet;
	unsigned char * body;
	unsigned char* buffer = data;
	uint32_t length = data_len;

	if (rtmp != NULL) {
		timestamp = timestamp - StartTime;
		/*去掉帧界定符(这里可能2种,但是sps or  pps只能为 00 00 00 01)*/
		if (buffer[2] == 0x00) { /*00 00 00 01*/
			buffer += 4;
			length -= 4;
		} else if (buffer[2] == 0x01) { /*00 00 01*/
			buffer += 3;
			length -= 3;
		}
		type = buffer[0] & 0x1f;

		packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + length + 9);
		memset(packet, 0, RTMP_HEAD_SIZE);

		packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
		packet->m_nBodySize = length + 9;

		/*send video packet*/
		body = (unsigned char *) packet->m_body;
		memset(body, 0, length + 9);

		/*key frame*/
		body[0] = 0x27;
		if (type == NAL_SLICE_IDR) //此为关键帧
				{
			body[0] = 0x17;
		}

		body[1] = 0x01; /*nal unit*/
		body[2] = 0x00;
		body[3] = 0x00;
		body[4] = 0x00;

		body[5] = (length >> 24) & 0xff;
		body[6] = (length >> 16) & 0xff;
		body[7] = (length >> 8) & 0xff;
		body[8] = (length) & 0xff;

		/*copy data*/
		memcpy(&body[9], buffer, length);

		packet->m_nTimeStamp = timestamp;
		packet->m_hasAbsTimestamp = 0;
		packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
		packet->m_nInfoField2 = rtmp->m_stream_id;
		packet->m_nChannel = 0x04;
		packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;

		if (RTMP_IsConnected(rtmp)) {
			// 调用发送接口

			int success = RTMP_SendPacket(rtmp, packet, TRUE);
			if (success != 1) {
				LOGE("send_rtmp_video fail");
			} else {
//			LOGE("發送成功");
			}
		} else {
//		rtmp_open_for_write();
			LOGE("send_rtmp_video RTMP is not ready");
		}
		free(packet);
	}
}

int isConnected()
{
    if(rtmp!=NULL && RTMP_IsConnected(rtmp))
         return 1;
    else
        return 0;
}

void send_rtmp_audio_spec(unsigned char *spec_buf, uint32_t spec_len) {


	if (rtmp != NULL) {
		RTMPPacket * packet;

		            unsigned char * body;
		            uint32_t len;

		            len = spec_len;  /*spec data长度,一般是2*/

		            packet = (RTMPPacket *)malloc(RTMP_HEAD_SIZE+len+2);
		            memset(packet,0,RTMP_HEAD_SIZE);

		            packet->m_body = (char *)packet + RTMP_HEAD_SIZE;
		            body = (unsigned char *)packet->m_body;

		            /*AF 00 + AAC RAW data*/
		            body[0] = 0xAF;
		            body[1] = 0x00;
		            memcpy(&body[2],spec_buf,len); /*spec_buf是AAC sequence header数据*/

		            packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
		            packet->m_nBodySize = len + 2;
		            packet->m_nChannel = 0x05;
		            packet->m_nTimeStamp = 0;
		            packet->m_hasAbsTimestamp = 0;
		            packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
		            packet->m_nInfoField2 = rtmp->m_stream_id;

		if (RTMP_IsConnected(rtmp)) {
			/*调用发送接口*/
			int success = RTMP_SendPacket(rtmp, packet, TRUE);
			if (success != 1) {
				LOGE("send_rtmp_audio_spec fail");
			}
		}
//		free(packet);
	} else {
		LOGE("send_rtmp_audio_spec RTMP is not ready");
	}
}

void send_rtmp_audio(unsigned char *buf, uint32_t len,int time) {

	 unsigned char* buffer = buf;
     uint32_t length = len;
     time=time-StartTime;
	    if(rtmp != NULL)
	    {
	    	  buffer += 7;
	    	  length -= 7;
	        if (length > 0) {
	            RTMPPacket * packet;
	            unsigned char * body;

	            packet = (RTMPPacket *)malloc(RTMP_HEAD_SIZE + length + 2);
	            memset(packet,0,RTMP_HEAD_SIZE);

	            packet->m_body = (char *)packet + RTMP_HEAD_SIZE;
	            body = (unsigned char *)packet->m_body;

	            /*AF 01 + AAC RAW data*/
	            body[0] = 0xAF;
	            body[1] = 0x01;
	            memcpy(&body[2],buffer,length);

	            packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
	            packet->m_nBodySize = length + 2;
	            packet->m_nChannel = 0x05;
	            packet->m_nTimeStamp = time;
	            packet->m_hasAbsTimestamp = 0;
	            packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
	            packet->m_nInfoField2 = rtmp->m_stream_id;

	            if(RTMP_IsConnected(rtmp))
	            {
	                /*调用发送接口*/
	                int success = RTMP_SendPacket(rtmp,packet,TRUE);
	                if(success != 1)
	                {
	                    LOGE("send_rtmp_audio fail");
	                }
	            }
	            free(packet);
	        }
	    }
	    else
	    {
	       LOGE("send_rtmp_audio RTMP is not ready");
	    }

}

int stopRtmpConnect() {
	if (isConnected()) {

		//時間戳要歸0 否則打次傳輸 會有問題
//            videotimeoffset=0;
//            audiotimeoffset=0;

		RTMP_Close(rtmp);
		RTMP_Free(rtmp);
		rtmp = NULL;
		return 1;
	}
	return -1;
}
