package io.github.brucewind.softcodec;

/**
 * Created by bruce on 16-11-24.
 * It is used to connect server and push stream.
 */
class StreamHelper {


    static {
        System.loadLibrary("share_x264");
    }

    /**
     * connect rtmp server
     */
    public native int rtmpOpen(String url);

    public native int rtmpStop();

    /**
     * x264 function
     * @param encoder
     * @param NV12
     * @param NV12size
     * @param H264
     * @return
     */
    public native int compressBuffer(long encoder, byte[] NV12, int NV12size, byte[] H264);

    public native long compressBegin(int width, int height, int bitrate, int fps);

    public native int compressEnd(long encoder);

}
