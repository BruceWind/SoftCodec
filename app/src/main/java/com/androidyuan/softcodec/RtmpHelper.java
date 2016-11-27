package com.androidyuan.softcodec;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wei on 16-11-27.
 */

public class RtmpHelper {


    final ExecutorService rtmpExecutor = Executors.newSingleThreadExecutor();

    StreamHelper mStreamHelper = new StreamHelper();

    /*rtmp*/
    public  int rtmpOpen(final String url){
        return mStreamHelper.rtmpOpen(url);
    }

    public  int rtmpStop(){
        rtmpExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mStreamHelper.rtmpStop();
                rtmpExecutor.shutdown();
            }
        });
        return 0;
    }

    /*x264 funtion*/
    public  int compressBuffer(final long encoder, final byte[] NV12, final int NV12size, final byte[] H264){
        rtmpExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mStreamHelper.compressBuffer(encoder,NV12,NV12size,H264);
            }
        });
        return 0;
    }

    public  long compressBegin(final int width, int height, int bitrate, int fps)
    {
        return  mStreamHelper.compressBegin(width,height,bitrate,fps);

    }

    public  int compressEnd(long encoder)
    {
        return  mStreamHelper.compressEnd(encoder);
    }

    public void startRecordeAudio(int currenttime) {
        AudioRecorder.startRecorde(currenttime);
    }

    public void stopRecordeAudio() {
        AudioRecorder.stopRecorde();
    }
}
