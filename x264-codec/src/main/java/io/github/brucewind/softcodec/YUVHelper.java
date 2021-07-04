package io.github.brucewind.softcodec;

import java.nio.ByteBuffer;

/**
 * It is used to convert YUV format of data to another format.
 */
public final class YUVHelper {
    /**
     * I420 to nv21
     */
    public static byte[] I420ToNV21(byte[] data, int width, int height) {
        byte[] ret = new byte[data.length];
        int total = width * height;

        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferVU = ByteBuffer.wrap(ret, total, total / 2);

        bufferY.put(data, 0, total);
        for (int i = 0; i < total / 4; i += 1) {
            bufferVU.put(data[i + total + total / 4]);
            bufferVU.put(data[total + i]);
        }

        return ret;
    }


    /**
     * nv21 to I420
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static byte[] nv21ToI420(byte[] data, int width, int height) {
        byte[] ret = new byte[data.length];
        int total = width * height;

        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferU = ByteBuffer.wrap(ret, total, total / 4);
        ByteBuffer bufferV = ByteBuffer.wrap(ret, total + total / 4, total / 4);

        bufferY.put(data, 0, total);
        for (int i=total; i<data.length; i+=2) {
            bufferV.put(data[i]);
            bufferU.put(data[i+1]);
        }

        return ret;
    }
    //yv12 to yuv420p
    public static void swapYV12toI420(final byte[] yv12bytes, final byte[] i420bytes, int width, int height) {
        int size = width * height;
        int part = size / 4;
        System.arraycopy(yv12bytes, 0, i420bytes, 0, size);
        System.arraycopy(yv12bytes, size + part, i420bytes, size, part);
        System.arraycopy(yv12bytes, size, i420bytes, size + part, part);
    }

}
