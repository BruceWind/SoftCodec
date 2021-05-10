package com.androidyuan.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.Toast;
import io.github.brucewind.softcodec.RtmpHelper;
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

public class MainActivity extends Activity implements SurfaceHolder.Callback,
        PreviewCallback {
    private static final String TAG = "MainActivity";

    Camera mCamera;
    SurfaceHolder mPreviewHolder;
    byte[] mPreviewBuffer;
    //TODO get the compatible size from camera.
    int width = 640;
    int height = 480;
    private final int VIDEOBITRATE = 512 *4;
    private final int FPS = 60;
    private RtmpHelper mRtmpHelper = new RtmpHelper();
    private String mRtmpPushUrl = "rtmp://172.26.201.159/live/live";
    private long mEncoderPointer = 0;
    private byte[] mH264Buff = null;
    private int mCurrentTime;
    private int encodeTime;


    private SurfaceView mSurfaveView;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mSurfaveView = (SurfaceView) this
                .findViewById(R.id.surfaceView);
        mPreviewHolder = mSurfaveView.getHolder();

        mPreviewHolder.addCallback(this);


        editText = (EditText) findViewById(R.id.edit_url);
        editText.setText(mRtmpPushUrl);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mRtmpPushUrl = editable.toString();
            }
        });

        findViewById(R.id.connect).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    editText.setEnabled(false);
                    if (mRtmpHelper.rtmpOpen(mRtmpPushUrl) > 0) {
                        Log.d(TAG, getString(R.string.tips_connect_successfully, mRtmpPushUrl));
                        mCurrentTime = (int) (System.currentTimeMillis());
                        findViewById(R.id.start).setEnabled(true);
                    }
                    else{
                        v.setEnabled(true);
                        editText.setEnabled(true);
                        Toast.makeText(MainActivity.this,getString(R.string.tips_cannt_connect,
                            mRtmpPushUrl),Toast.LENGTH_SHORT).show();
                    }
                }
            });

        findViewById(R.id.start).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setEnabled(false);
                        startCamera();
                        mRtmpHelper.startRecordeAudio(mCurrentTime);
                        findViewById(R.id.stop).setEnabled(true);
                    }
                });

        findViewById(R.id.stop).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopStreaming();
                        mRtmpHelper.stopRecordeAudio();
                        v.setEnabled(false);
                        findViewById(R.id.connect).setEnabled(true);
                    }
                });

    }

    @SuppressLint("InlinedApi")
    private void startCamera() {

        mEncoderPointer = mRtmpHelper.compressBegin(width, height, VIDEOBITRATE, FPS);

        mH264Buff = new byte[width * height * 8];

        mPreviewHolder.setFixedSize(width, height);

        int stride = (int) Math.ceil(width / 16.0f) * 16;
        int cStride = (int) Math.ceil(width / 32.0f) * 16;
        final int frameSize = stride * height;
        final int qFrameSize = cStride * height / 2;

        mPreviewBuffer = new byte[frameSize + qFrameSize * 2];



        LayoutParams layoutParams= mSurfaveView.getLayoutParams();

        layoutParams.width = height;
        layoutParams.height = width;

        mSurfaveView.setLayoutParams(layoutParams);

        try {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);

            mCamera.setPreviewDisplay(mPreviewHolder);
            Camera.Parameters params = mCamera.getParameters();

            params.setPreviewSize(width, height);
            params.setPreviewFormat(ImageFormat.YUV_420_888);
            //自动对焦
            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            mCamera.setParameters(params);
            mCamera.addCallbackBuffer(mPreviewBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // TODO Auto-generated method stub

        mCamera.addCallbackBuffer(mPreviewBuffer);


        int result = mRtmpHelper.compressBuffer(mEncoderPointer,
                data,
                data.length,
            mH264Buff);

    }

    private void stopStreaming() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if(mEncoderPointer!=0){
            mRtmpHelper.rtmpStop();
            mRtmpHelper.compressEnd(mEncoderPointer);
            mEncoderPointer=0;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
        int width,
        int height) {
        Log.w(TAG,"onSurfaceChange : "+width +"x"+ height);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        stopStreaming();
        Log.i(TAG, "surface destroyed");
    }

    @Override
    protected void onPause() {
        findViewById(R.id.stop).performClick();
        super.onPause();
    }

}