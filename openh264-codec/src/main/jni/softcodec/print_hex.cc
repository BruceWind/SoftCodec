#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdint.h>
#include <android/log.h>
#include <iostream>
#include <string>
#include "h264_encoder_log.h"

void print_hex(char * tag, unsigned char* data,int len){
    char buffer[len*2+1];
    buffer[len*2]=0;

    for(auto j = 0; j < len; j++){
        sprintf(&buffer[2*j], "%02X", data[j]);
    }
    ALOGI("%s hex:%s",tag,buffer);
}