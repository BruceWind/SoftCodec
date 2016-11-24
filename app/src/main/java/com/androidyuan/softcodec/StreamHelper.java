package com.androidyuan.softcodec;

/**
 * Created by wei on 16-11-24.
 */
public class StreamHelper {


    static {
        System.loadLibrary("share_x264");
    }


    /*rtmp*/
    public native int rtmpOpen(String url);

    public native int rtmpStop();

    /*x264 funtion*/
    public native int compressBuffer(long encoder, byte[] NV12, int NV12size, byte[] H264);

    public native long compressBegin(int width, int height, int bitrate, int fps);

    public native int compressEnd(long encoder);


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        rtmpStop();
    }
}
