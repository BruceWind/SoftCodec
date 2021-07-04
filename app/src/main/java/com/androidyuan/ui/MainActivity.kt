package com.androidyuan.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.androidyuan.softcodec.R
import com.androidyuan.softcodec.databinding.ActivityMainBinding
import io.github.brucewind.camera.Camera2Helper
import io.github.brucewind.camera.CameraInfo
import io.github.brucewind.camera.recommendBitRateByte
import io.github.brucewind.softcodec.RtmpHelper
import io.github.brucewind.softcodec.YUVHelper
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, PreviewCallback {

    var mCamera: Camera? = null
    var mPreviewHolder: SurfaceHolder? = null
    var mPreviewBuffer: ByteArray? = null

    lateinit var binding: ActivityMainBinding
    //TODO obtain the a compatible set of size from camera.

    private val mRtmpHelper = RtmpHelper()
    private var mRtmpPushUrl = "rtmp://192.168.50.14/live/live"
    private var mEncoderPointer: Long = 0
    private var mH264Buff: ByteArray? = null
    private var mCurrentTime = 0
    private val encodeTime = 0


    private  var mCameraInfo: CameraInfo? = null
    private val mCameraInfoes = CopyOnWriteArrayList<CameraInfo>()//assume it thread-safe

    private fun handleCameraInfoesInSpinner() {
        mCameraInfoes.clear()
        mCameraInfoes.addAll(Camera2Helper.obtainAllSelectedCameraInfoes(getCamera2Manager()))

        val spinnerAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, mCameraInfoes.map { it.name })

        binding.cameraSelectionSpinner.adapter = spinnerAdapter
        binding.cameraSelectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        handleCameraInfoesInSpinner()
        
        mPreviewHolder = binding.surfaceView.holder
        mPreviewHolder!!.addCallback(this)

        binding.editUrl.setText(mRtmpPushUrl)
        binding.editUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                mRtmpPushUrl = editable.toString()
            }
        })


        binding.connect.setOnClickListener { v ->

            if(mCameraInfo==null) {
                Toast.makeText(this,"You has not selected any camera size.",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            v.isEnabled = false
            binding.editUrl.isEnabled = false
            if (mRtmpHelper.rtmpOpen(mRtmpPushUrl) > 0) {
                Log.d(TAG, getString(R.string.tips_connect_successfully, mRtmpPushUrl))
                mCurrentTime = System.currentTimeMillis().toInt()
                binding.start.isEnabled = true
                binding.cameraSelectionSpinner.isEnabled = false
            } else {
                v.isEnabled = true
                binding.editUrl.isEnabled = true
                Toast.makeText(
                    this@MainActivity, getString(
                        R.string.tips_cannt_connect,
                        mRtmpPushUrl
                    ), Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.start.setOnClickListener { v ->
            v.isEnabled = false
            startCamera()
            mRtmpHelper.startRecordeAudio(mCurrentTime)
            binding.stop.isEnabled = true
            binding.cameraSelectionSpinner.isEnabled = true
        }
        binding.stop.setOnClickListener { v ->
            stopStreaming()
            mRtmpHelper.stopRecordeAudio()
            v.isEnabled = false
            binding.connect.isEnabled = true
            binding.cameraSelectionSpinner.isEnabled = false
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
        val layoutParams = binding.surfaceView!!.layoutParams
        layoutParams.width = height
        layoutParams.height = width
        binding.surfaceView!!.layoutParams = layoutParams
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
        assert(i420bytes.size== data.size)

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
        binding.stop.performClick()
        super.onPause()
    }

    companion object {
        private const val TAG = "MainActivity"
    }


    private fun getCamera2Manager(): CameraManager =
        getSystemService(Context.CAMERA_SERVICE) as CameraManager
}