//
// Created by faraklit on 08.02.2016.
//

#ifndef _XIECC_RTMP_H_
#define _XIECC_RTMP_H_
#include <stdint.h>
#include <stdbool.h>
#include <unistd.h>
#include <stdlib.h>

#ifdef __cplusplus
extern "C"{
#endif


static int getSystemTime() {
	struct timeval tv; //获取一个时间结构
	gettimeofday(&tv, NULL); //获取当前时间
	int t = tv.tv_sec;
	t *= 1000;
	t += tv.tv_usec / 1000;
	return t;
}

int rtmp_open_for_write(const char * url);

void send_video_sps_pps(uint8_t *sps,int sps_len,uint8_t *pps,int pps_len);

void send_rtmp_video(uint8_t *data, int data_len,int timestamp) ;

void send_rtmp_audio_spec(unsigned char *spec_buf, uint32_t spec_len);

void send_rtmp_audio(unsigned char *buf, uint32_t len,int time) ;
#ifdef __cplusplus
}
#endif
#endif
