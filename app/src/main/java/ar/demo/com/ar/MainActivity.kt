package ar.demo.com.ar

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat
import org.opencv.objdetect.HOGDescriptor
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.MatOfDouble
import org.opencv.core.MatOfRect
import org.opencv.imgproc.Imgproc


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
        val foundLocations: MatOfRect = MatOfRect()
        val foundWeights: MatOfDouble = MatOfDouble()
        hog.detectMultiScale(temp, foundLocations, foundWeights)
        Log.i(TAG, "found locations " + foundLocations)
        Log.i(TAG, "found weight " + foundWeights)
        return mMat!!
    }

    override fun onCameraViewStopped() {
        mMat?.release()
    }


    companion object {
        const val TAG = "MainActivity"
    }
}
