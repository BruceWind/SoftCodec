package com.androidyuan.ui;

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
import android.view.WindowManager;

import com.androidyuan.softcodec.R;
import com.androidyuan.softcodec.RtmpHelper;

public class MainActivity extends Activity implements SurfaceHolder.Callback,
        PreviewCallback {
    static String TAG = "CameraPreview";

    Camera camera;
    SurfaceHolder previewHolder;
    byte[] previewBuffer;
    int width = 640;// 编码宽度
    int height = 480;// 编码高度
    int VideoBitrate = 512 *4;
    int fps = 60;
    RtmpHelper mRtmpHelper = new RtmpHelper();
    private String rtmpPushUrl = "rtmp://172.26.201.159/live/live";
    private long encoder = 0;
    private byte[] h264Buff = null;
    private int currenttime;
    private int encodeTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setContentView(R.layout.activity_main);

        SurfaceView svCameraPreview = (SurfaceView) this
                .findViewById(R.id.surfaceView);
        this.previewHolder = svCameraPreview.getHolder();

        this.previewHolder.addCallback(this);


        findViewById(R.id.connect).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRtmpHelper.rtmpOpen(rtmpPushUrl) > 0) {
                        Log.d(TAG, "成功连結");
                        currenttime = (int) (System.currentTimeMillis());
                        findViewById(R.id.start).setEnabled(true);
                        v.setEnabled(false);
                    }
                }
            });

        findViewById(R.id.start).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setEnabled(false);
                        startCamera();
                        mRtmpHelper.startRecordeAudio(currenttime);
                        findViewById(R.id.stop).setEnabled(true);
                    }
                });

        findViewById(R.id.stop).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopCamera();
                        mRtmpHelper.stopRecordeAudio();
                        v.setEnabled(false);
                        findViewById(R.id.connect).setEnabled(true);
                    }
                });

    }

    @SuppressLint("InlinedApi")
    private void startCamera() {

        encoder = mRtmpHelper.compressBegin(width, height, VideoBitrate, fps);

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
            //自动对焦
            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            camera.setParameters(params);
            camera.addCallbackBuffer(previewBuffer);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // TODO Auto-generated method stub

        this.camera.addCallbackBuffer(this.previewBuffer);


        int result = mRtmpHelper.compressBuffer(encoder,
                data,
                data.length,
                h264Buff);

    }

    private void stopCamera() {
        if (camera != null) {
            mRtmpHelper.rtmpStop();
            mRtmpHelper.compressEnd(encoder);
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
        findViewById(R.id.stop).performClick();
        super.onPause();
    }

}