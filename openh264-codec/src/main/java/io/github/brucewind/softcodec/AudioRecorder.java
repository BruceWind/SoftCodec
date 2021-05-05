package io.github.brucewind.softcodec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

/**
 *  Obviously it is used to record audio.
 */
public class AudioRecorder {

    static final String TAG = "AudioRecorder";

    private static AudioRecord mAudioRecord = null;
    private static boolean isRecording = false;// 录音标志位
    private static final int frequency = 44100;
    private static final  int channel = AudioFormat.CHANNEL_IN_STEREO;// 设置声道,立体声，双声道
    private static final int encodingBitRate = AudioFormat.ENCODING_PCM_16BIT;// 设置编码。脉冲编码调制（PCM）每个样品16位
    private static int recBufSize = 0;
    private static Thread recThread = null;// 录音线程
    private int playBufSize = 0;

    static void startRecorde(final int currenttime) {
        int initSuccess = initAAC(64000, frequency, 2);

        Log.d(TAG, "initSuccess" + ":" + initSuccess);
        if (initSuccess > 0) {
            startRec();
            Log.d(TAG, "inited successfully.");
        } else {
            Log.e(TAG, "inited failed.");
        }

    }

    // start record time
    private static void startRec() {
        // obtain min buf size

        recBufSize = getbuffersize();
        Log.v(TAG, "fdkaac input buffer size:" + recBufSize);
        recBufSize = AudioRecord.getMinBufferSize(frequency, channel, encodingBitRate);

        Log.v(TAG, "device supports minimum buffer size:" + recBufSize);

        // init record.
        mAudioRecord = new AudioRecord(AudioSource.VOICE_COMMUNICATION,//VOICE_COMMUNICATION will eliminate echo or noisy.
                frequency,
                channel,
                encodingBitRate,
                recBufSize);

        mAudioRecord.startRecording();

        isRecording = true;

        recThread = new Thread(new Runnable() {
            public void run() {
                byte data[] = new byte[recBufSize];
                int read = 0;
                while (isRecording) {
                    read = mAudioRecord.read(data, 0, 4096);

                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        boolean success = encodeFrame(data);

                        if (success) {
//							Log.d(TAG, "whiting file..");
                        } else {
                            Log.e(TAG, "aac write into file failure.");
                        }
                    }
                }
            }
        }, "AudioRecorder Thread");

        recThread.start();
    }


    static void stopRecorde() {
        if (null != mAudioRecord) {
            isRecording = false;
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
            recThread = null;
        }
    }

    public native static int initAAC(int bitrate, int samplerate, int channels);

    public native static boolean encodeFrame(byte[] data);

    public native static int getbuffersize();
}
