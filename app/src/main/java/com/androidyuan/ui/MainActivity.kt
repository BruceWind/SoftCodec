package com.androidyuan.ui

import android.view.SurfaceHolder
import android.hardware.Camera.PreviewCallback
import io.github.brucewind.softcodec.RtmpHelper
import android.view.SurfaceView
import android.os.Bundle
import android.view.WindowManager
import com.androidyuan.softcodec.R
import android.text.TextWatcher
import android.text.Editable
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import io.github.brucewind.camera.Camera2Helper
import io.github.brucewind.camera.CameraInfo
import io.github.brucewind.camera.recommendBitRateByte
import java.io.IOException
import java.lang.RuntimeException
import io.github.brucewind.softcodec.YUVHelper
import junit.framework.Assert
import java.util.concurrent.CopyOnWriteArrayList

class MainActivity : Activity(), SurfaceHolder.Callback, PreviewCallback {

    var mCamera: Camera? = null
    var mPreviewHolder: SurfaceHolder? = null
    var mPreviewBuffer: ByteArray? = null

    //TODO obtain the a compatible set of size from camera.

    private val mRtmpHelper = RtmpHelper()
    private var mRtmpPushUrl = "rtmp://192.168.50.14/live/live"
    private var mEncoderPointer: Long = 0
    private var mH264Buff: ByteArray? = null
    private var mCurrentTime = 0
    private val encodeTime = 0
    private var mSurfaveView: SurfaceView? = null
    private var mEditText: EditText? = null


    private  var mCameraInfo: CameraInfo? = null
    private lateinit var mCameraSelectionSpinner: Spinner
    private val mCameraInfoes = CopyOnWriteArrayList<CameraInfo>()//assume it thread-safe

    private fun handleCameraInfoesInSpinner() {
        mCameraSelectionSpinner = findViewById(R.id.camera_selection_spinner) as Spinner
        mCameraInfoes.clear()
        mCameraInfoes.addAll(Camera2Helper.obtainAllSelectedCameraInfoes(getCamera2Manager()))

        val spinnerAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, mCameraInfoes.map { it.name })

        mCameraSelectionSpinner.adapter = spinnerAdapter
        mCameraSelectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                mCameraInfo = mCameraInfoes[pos]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                mCameraInfo =null
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setContentView(R.layout.activity_main)
        handleCameraInfoesInSpinner()
        mSurfaveView = findViewById(R.id.surfaceView) as SurfaceView
        mPreviewHolder = mSurfaveView!!.holder
        mPreviewHolder!!.addCallback(this)
        mEditText = findViewById(R.id.edit_url) as EditText
        mEditText!!.setText(mRtmpPushUrl)
        mEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                mRtmpPushUrl = editable.toString()
            }
        })


        findViewById(R.id.connect).setOnClickListener { v ->

            if(mCameraInfo==null) {
                Toast.makeText(this,"You has not selected any camera size.",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            v.isEnabled = false
            mEditText!!.isEnabled = false
            if (mRtmpHelper.rtmpOpen(mRtmpPushUrl) > 0) {
                Log.d(TAG, getString(R.string.tips_connect_successfully, mRtmpPushUrl))
                mCurrentTime = System.currentTimeMillis().toInt()
                findViewById(R.id.start).isEnabled = true
                mCameraSelectionSpinner.isEnabled = false
            } else {
                v.isEnabled = true
                mEditText!!.isEnabled = true
                Toast.makeText(
                    this@MainActivity, getString(
                        R.string.tips_cannt_connect,
                        mRtmpPushUrl
                    ), Toast.LENGTH_SHORT
                ).show()
            }
        }
        findViewById(R.id.start).setOnClickListener { v ->
            v.isEnabled = false
            startCamera()
            mRtmpHelper.startRecordeAudio(mCurrentTime)
            findViewById(R.id.stop).isEnabled = true
            mCameraSelectionSpinner.isEnabled = true
        }
        findViewById(R.id.stop).setOnClickListener { v ->
            stopStreaming()
            mRtmpHelper.stopRecordeAudio()
            v.isEnabled = false
            findViewById(R.id.connect).isEnabled = true
            mCameraSelectionSpinner.isEnabled = false
        }
    }

    @SuppressLint("InlinedApi")
    private fun startCamera() {
        if(mCameraInfo==null) {
            Toast.makeText(this,"You has not selected any camera size.",Toast.LENGTH_LONG).show()
            return
        }
        val width  = mCameraInfo!!.size.width
        val height  = mCameraInfo!!.size.height
        mEncoderPointer = mRtmpHelper.compressBegin(
            width,
            height,
            mCameraInfo!!.recommendBitRateByte().toInt()/1000,
            mCameraInfo!!.fps)
        if (mEncoderPointer == 0L) {
            Toast.makeText(this, "encoder init error.", Toast.LENGTH_LONG).show()
            return
        }
        mH264Buff = ByteArray(width * height * 8)
        mPreviewHolder!!.setFixedSize(width, height)
        val stride = Math.ceil((width / 16.0f).toDouble()).toInt() * 16
        val cStride = Math.ceil((width / 32.0f).toDouble()).toInt() * 16
        val frameSize = stride * height
        val qFrameSize = cStride * height / 2
        mPreviewBuffer = ByteArray(frameSize + qFrameSize * 2)
        val layoutParams = mSurfaveView!!.layoutParams
        layoutParams.width = height
        layoutParams.height = width
        mSurfaveView!!.layoutParams = layoutParams
        try {
            mCamera = Camera.open()
            mCamera!!.setDisplayOrientation(90)
            mCamera!!.setPreviewDisplay(mPreviewHolder)
            val params = mCamera!!.getParameters()
            params.setPreviewSize(width, height)
            params.previewFormat = ImageFormat.NV21 //may selected codec not support this format.
            //自动对焦
            if (params.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }
            mCamera!!.setParameters(params)
            mCamera!!.addCallbackBuffer(mPreviewBuffer)
            mCamera!!.setPreviewCallbackWithBuffer(this)
            mCamera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        // TODO Auto-generated method stub
        mCamera!!.addCallbackBuffer(mPreviewBuffer)
        val i420bytes = YUVHelper.nv21ToI420(data, mCameraInfo!!.size.width, mCameraInfo!!.size.height)
        Assert.assertEquals(i420bytes.size, data.size)
        val result = mRtmpHelper.compressBuffer(
            mEncoderPointer,
            i420bytes,
            i420bytes.size,
            mH264Buff
        )
    }

    private fun stopStreaming() {
        if (mCamera != null) {
            mCamera!!.setPreviewCallback(null)
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
        }
        if (mEncoderPointer != 0L) {
            mRtmpHelper.rtmpStop()
            mRtmpHelper.compressEnd(mEncoderPointer)
            mEncoderPointer = 0
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}
    override fun surfaceChanged(
        holder: SurfaceHolder, format: Int,
        width: Int,
        height: Int
    ) {
        Log.w(TAG, "onSurfaceChange : " + width + "x" + height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // TODO Auto-generated method stub
        stopStreaming()
        Log.i(TAG, "surface destroyed")
    }

    override fun onPause() {
        findViewById(R.id.stop).performClick()
        super.onPause()
    }

    companion object {
        private const val TAG = "MainActivity"
    }


    private fun getCamera2Manager(): CameraManager =
        getSystemService(Context.CAMERA_SERVICE) as CameraManager
}