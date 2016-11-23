package com.androidyuan.softcodec;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MainActivity extends Activity implements SurfaceHolder.Callback,
        PreviewCallback {
    static String TAG = "CameraPreview";

    Camera camera;
    SurfaceHolder previewHolder;

    byte[] previewBuffer;

    int width = 640;// 编码宽度
    int height = 480;// 编码高度
    int VideoBitrate = 512 * 2;
    int fps = 15;

    private String flv_url = "rtmp://up.quanmin.tv/live/160?key=5376b705a4fd31c449f84c8e9324a9a6";

    private long encoder = 0;
    private byte[] h264Buff = null;

    private int currenttime;
    private int EncodeTime;

    static {
        System.loadLibrary("share_x264");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        SurfaceView svCameraPreview = (SurfaceView) this
                .findViewById(R.id.surfaceView);
        this.previewHolder = svCameraPreview.getHolder();

        this.previewHolder.addCallback(this);

        this.findViewById(R.id.start).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startCamera();
                        AudioRecorde.startRecorde(currenttime);
                    }
                });
        this.findViewById(R.id.stop).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopCamera();
                        AudioRecorde.stopRecorde();
                    }
                });

        this.findViewById(R.id.connect).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Rtmpopen(flv_url) > 0) {
                            Log.d(TAG, "成功连結");
                            currenttime = (int) (System.currentTimeMillis());
                        }
                    }
                });
    }

    @SuppressLint("InlinedApi")
    private void startCamera() {

        encoder = CompressBegin(width, height, VideoBitrate, fps);

        h264Buff = new byte[width * height * 8];

        this.previewHolder.setFixedSize(width, height);

        int stride = (int) Math.ceil(width / 16.0f) * 16;
        int cStride = (int) Math.ceil(width / 32.0f) * 16;
        final int frameSize = stride * height;
        final int qFrameSize = cStride * height / 2;

        this.previewBuffer = new byte[frameSize + qFrameSize * 2];

        try {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(this.previewHolder);
            Camera.Parameters params = camera.getParameters();
            params.setPreviewSize(width, height);
            params.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(params);
            camera.addCallbackBuffer(previewBuffer);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
        } catch (IOException e) {
            // TODO:
        } catch (RuntimeException e) {
            // TODO:
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // TODO Auto-generated method stub

        this.camera.addCallbackBuffer(this.previewBuffer);


        int result = CompressBuffer(encoder,
                data,
                data.length,
                h264Buff);

    }

    private void stopCamera() {
        if (camera != null) {
            Rtmpstop();
            CompressEnd(encoder);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        stopCamera();
        Log.i(TAG, "surface destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /*rtmp*/
    public native int Rtmpopen(String url);

    private native int Rtmpstop();

    /*x264 funtion*/
    private native int CompressBuffer(long encoder, byte[] NV12, int NV12size, byte[] H264);

    private native long CompressBegin(int width, int height, int bitrate, int fps);

    private native int CompressEnd(long encoder);

}