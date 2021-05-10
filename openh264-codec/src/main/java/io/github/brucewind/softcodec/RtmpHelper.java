package io.github.brucewind.softcodec;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.util.Log;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wei on 16-11-27.
 *
 * It is used to manager picture to be encode, and transfer encoded data to server in {@link ExecutorService}.
 */
public class RtmpHelper {

  private static String LOGTAG="LiveCamera_encoder";

  private ExecutorService mRtmpExecutor = Executors.newSingleThreadExecutor();
  private Timer mTimer;
  private final AtomicInteger mFpsAtomic = new AtomicInteger(0);
  private final StreamHelper mStreamHelper = new StreamHelper();

  /*rtmp*/
  public int rtmpOpen(final String url) {
    mTimer = new Timer();
    mTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        Log.d(LOGTAG, "fps = " + mFpsAtomic.get());
        mFpsAtomic.set(0);
      }
    }, 1000, 1000);

    return mStreamHelper.rtmpOpen(url);
  }

  public int rtmpStop() {
    if (mTimer != null) {
      mTimer.cancel();
    }

    mRtmpExecutor.execute(new Runnable() {
      @Override
      public void run() {
        mStreamHelper.rtmpStop();
        mRtmpExecutor.shutdown();
        try {
          final ExecutorService temp = mRtmpExecutor;
          temp.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        mRtmpExecutor = Executors.newSingleThreadExecutor();

      }
    });
    return 0;
  }

  /*x264 funtion*/
  public int compressBuffer(final long encoder, final byte[] NV12, final int NV12size,
      final byte[] H264) {
    mRtmpExecutor.execute(new Runnable() {
      @Override
      public void run() {

        Log.d(LOGTAG, "compressBuffer("+NV12size+")");
        mStreamHelper.compressBuffer(encoder, NV12, NV12size, H264);
        mFpsAtomic.incrementAndGet();
      }
    });
    return 0;
  }

  public long compressBegin(final int width, int height, int bitrate, int fps) {
    return mStreamHelper.compressBegin(width, height, bitrate, fps);
  }

  public int compressEnd(long encoder) {
    return mStreamHelper.compressEnd(encoder);
  }

  public void startRecordeAudio(int currenttime) {
    AudioRecorder.startRecorde(currenttime);
  }

  public void stopRecordeAudio() {
    AudioRecorder.stopRecorde();
  }
}
