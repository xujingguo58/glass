package ar.demo.com.ar

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import org.opencv.android.CameraBridgeViewBase
import org.opencv.objdetect.HOGDescriptor
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX
import java.nio.charset.Charset


class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    lateinit var hog: HOGDescriptor
    lateinit var cvCameraBridgeViewBase: CameraBridgeViewBase
    var mMat: Mat? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cvCameraBridgeViewBase = findViewById(R.id.camera_view)
        cvCameraBridgeViewBase.visibility = View.VISIBLE
        cvCameraBridgeViewBase.setCameraPermissionGranted()
        cvCameraBridgeViewBase.setCvCameraViewListener(this)


    }

    /**通过OpenCV管理Android服务，初始化OpenCV */
    var mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    cvCameraBridgeViewBase.enableView()
                    hog = HOGDescriptor()
                    hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector())
                }
                else -> {
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        val res = OpenCVLoader.initDebug()
        if (res) {
            Log.i(TAG, "init success")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
//            cvCameraBridgeViewBase.enableView()
        } else {
            Log.e(TAG, "init fail")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cvCameraBridgeViewBase.disableView()
    }


    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.d(TAG, "onCameraViewStarted")
        mMat = Mat(height, width, CvType.CV_8UC4)
    }


    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        Log.d(TAG, "onCameraFrame")
        mMat = inputFrame?.rgba()
        val temp = Mat(mMat!!.width(), mMat!!.height(), CvType.CV_8UC4)
        Imgproc.cvtColor(mMat, temp, Imgproc.COLOR_RGBA2BGR)
//        return detectMulti(temp)
        return detect(temp)
    }

    fun detectMulti(mat: Mat): Mat {
        val foundLocations: MatOfRect = MatOfRect()
        val foundWeights: MatOfDouble = MatOfDouble()
        Log.i(TAG, "start")
        hog.detectMultiScale(mat, foundLocations, foundWeights, 0.0, Size(4.0, 4.0), Size(8.0, 8.0), 1.05, 2.0, false)
        Log.i(TAG, "stop")
        hog.detectMultiScale(mat, foundLocations, foundWeights)
        if (foundLocations.toArray().isNotEmpty()) {
            Log.i(TAG, "found target")
            for (rect: Rect in foundLocations.toArray()) {
                rect.x += Math.round(rect.width * 0.1).toInt()
                rect.width = Math.round(rect.width * 0.8).toInt()
                rect.y = Math.round(rect.height * 0.045).toInt()
                rect.height = Math.round(rect.height * 0.85).toInt()
                Imgproc.rectangle(mat, rect.tl(), rect.br(), Scalar(0.toDouble(), 0.toDouble(), 255.toDouble()), 2)
            }
            Log.i(TAG, "矩形绘制完毕！正在输出...")
        }
        return mat
    }

    fun detect(mat: Mat): Mat {
        val foundLocation: MatOfPoint = MatOfPoint()
        val foundWeights: MatOfDouble = MatOfDouble()
        Log.d(TAG, "start")
        hog.detect(mat, foundLocation, foundWeights)
        Log.d(TAG, "stop")
        if (foundLocation.toArray().isNotEmpty()) {
            for (point: Point in foundLocation.toArray()) {
                Log.i(TAG, point.toString() + ". ")
                val rect: Rect = Rect()
                rect.x = Math.round(point.x).toInt()
                rect.width = 48 * 2
                rect.y = Math.round(point.y).toInt()
                rect.height = 96 * 3
//                Imgproc . rectangle (mat, rect.tl(), rect.br(), Scalar(0.toDouble(), 0.toDouble(), 255.toDouble()), 2)
                Imgproc.putText(mat, "power:5", point, FONT_HERSHEY_SIMPLEX, 1.0, Scalar(173.0, 255.0, 47.0), 4)

            }
            Log.i(TAG, "矩形绘制完毕！正在输出...")
        }
        return mat
    }

    override fun onCameraViewStopped() {
        mMat?.release()
    }


    companion object {
        const val TAG = "MainActivity"
    }
}
