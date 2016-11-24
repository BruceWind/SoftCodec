package com.androidyuan.softcodec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


public class AudioRecorder {

    static final String TAG = "AudioRecorder";

    private static AudioRecord audioRecord = null;
    private static boolean isRecording = false;// 录音标志位
    private static int frequency = 44100;
    private static int channel = AudioFormat.CHANNEL_IN_STEREO;// 设置声道,立体声，双声道
    private static int encodingBitRate = AudioFormat.ENCODING_PCM_16BIT;// 设置编码。脉冲编码调制（PCM）每个样品16位
    private static int recBufSize = 0;
    private static Thread recThread = null;// 录音线程
    private int playBufSize = 0;

    static void startRecorde(final int currenttime) {
        int initSuccess = initAAC(64000, frequency, 2);

        Log.d(TAG, "initSuccess" + ":" + initSuccess);
        if (initSuccess > 0) {
            startRec();
            Log.d(TAG, "初始化成功");
        } else {
            Log.e(TAG, "初始化失败");
        }

    }

    // 开始录音
    private static void startRec() {
        // 获取最小buf大小

        recBufSize = getbuffersize();
        Log.v(TAG, "fdkaac需要输入的buffer大小：" + recBufSize);
        recBufSize = AudioRecord.getMinBufferSize(frequency, channel, encodingBitRate);

        Log.v(TAG, "系统支持的最小buffer大小：" + recBufSize);

        // 初始化录音机
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                frequency,
                channel,
                encodingBitRate,
                recBufSize);

        audioRecord.startRecording();

        isRecording = true;

        recThread = new Thread(new Runnable() {
            public void run() {
                byte data[] = new byte[recBufSize];
                int read = 0;
                while (isRecording) {
                    read = audioRecord.read(data, 0, 4096);

                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        boolean success = encodeFrame(data);

                        if (success) {
//							Log.d(TAG, "写入文件。。");
                        } else {
                            Log.e(TAG, "aac写入文件失败");
                        }
                    }
                }
            }
        }, "AudioRecorder Thread");

        recThread.start();
    }


    static void stopRecorde() {
        if (null != audioRecord) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            recThread = null;
        }
    }

    public native static int initAAC(int bitrate, int samplerate, int channels);

    public native static boolean encodeFrame(byte[] data);

    public native static int getbuffersize();
}
