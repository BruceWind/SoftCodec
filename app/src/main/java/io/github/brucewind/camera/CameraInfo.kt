package io.github.brucewind.camera
import android.util.Log
import android.util.Size

/**
 * Few properties of camera is used to obtain frames from camera.
 */
data class CameraInfo(
    val name: String,
    val cameraId: String,
    val size: Size,
    val fps: Int)


/**
 * Obtain BitRate of recommend which its unit is byte.
 * It will deal with fps which must be in range of 10~120.
 * It according to this link ： “https://support.google.com/youtube/answer/2853702?hl=zh-Hans#zippy=%2Ckp-fps”
 */
fun CameraInfo.recommendBitRateByte():Long{
    val DELTA = 20L
    return  (size.width * size.height * fps / DELTA).also {

        Log.i("XXA","biterate with bit is $it.")
    }
}