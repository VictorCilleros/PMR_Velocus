package com.velocus

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.velocus.View.CameraView
import com.velocus.model.Gps
import com.velocus.model.Station

class CameraActivity : AppCompatActivity() {
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var myContext: Context? = null
    private var cameraPreview: LinearLayout? = null
    private var cameraFront = false

    lateinit var cameraView : CameraView

    var gps : Gps? = null

    var stations : MutableList<Station>? = null

    var thetaH : Double=0.0
    var thetaV : Double=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        myContext = this

        mCamera = Camera.open()
        mCamera?.setDisplayOrientation(90)

        cameraPreview = findViewById<View>(R.id.cPreview) as LinearLayout
        mPreview = CameraPreview(myContext, mCamera)
        cameraPreview!!.addView(mPreview)

        mCamera?.startPreview()

        cameraView = CameraView(this)
        cameraView = findViewById(R.id.CameraViewTestGps)

        // Initialisation du GPS :
        gps = Gps(this,cameraView)
        gps!!.checkLocation() // vérifiez si le service de localisation est activé ou non sur votre téléphone


        val p: Camera.Parameters = mCamera?.getParameters()!!
        thetaV = Math.toRadians(p.verticalViewAngle.toDouble())
        thetaH = Math.toRadians(p.horizontalViewAngle.toDouble())
        Log.d("pato", "onCreate: $thetaV  ;  $thetaH")

        stations = MutableList<Station>(1){Station(50.62723799221388,3.109268199357267,"Mairie d'Hellemme",24,8) }
        stations!!.add(Station(50.619122956331886,3.1264709816213587,"Villeneuve-d'Ascq",27,22))
        stations!!.add(Station(50.63701166075154,3.0707240415241044,"Gare Lille Flandres",19,0))

        cameraView.a=this
    }

    private fun findFrontFacingCamera(): Int {
        var cameraId = -1
        // Search for the front facing camera
        val numberOfCameras = Camera.getNumberOfCameras()
        for (i in 0 until numberOfCameras) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i
                cameraFront = true
                break
            }
        }
        return cameraId
    }

    private fun findBackFacingCamera(): Int {
        var cameraId = -1
        //Search for the back facing camera
        //get the number of cameras
        val numberOfCameras = Camera.getNumberOfCameras()
        //for every camera check
        for (i in 0 until numberOfCameras) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i
                cameraFront = false
                break
            }
        }
        return cameraId
    }

    public override fun onResume() {
        super.onResume()
        if (mCamera == null) {
            mCamera = Camera.open()
            mCamera?.setDisplayOrientation(90)
            mPreview!!.refreshCamera(mCamera)
            Log.d("nu", "null")
        } else {
            Log.d("nu", "no null")
        }

        gps?.sensorManager!!.registerListener(gps?.gyroListener, gps?.sensorManager!!.getDefaultSensor( Sensor.TYPE_ROTATION_VECTOR ), SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onStart() {
        super.onStart()
        // Connection au service de GPS :
        if (gps!!.mGoogleApiClient != null) {
            gps!!.mGoogleApiClient!!.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        // Déconnection du service de GPS :
        if (gps!!.mGoogleApiClient!!.isConnected()) {
            gps!!.mGoogleApiClient!!.disconnect()
        }
        // Déinscription de tout les capteurs, de l'attribut "gyroListener" :
        gps?.sensorManager?.unregisterListener(gps?.gyroListener)
    }

    fun chooseCamera() {
        //if the camera preview is the front
        val cameraId = findFrontFacingCamera()
        if (cameraId >= 0) {
            //open the backFacingCamera
            //set a picture callback
            //refresh the preview
            mCamera = Camera.open(cameraId)
            mCamera?.setDisplayOrientation(90)
            mPreview!!.refreshCamera(mCamera)
        }
    }

    override fun onPause() {
        super.onPause()
        //when on Pause, release camera in order to be used from other applications
        releaseCamera()
    }

    private fun releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.setPreviewCallback(null)
            mCamera!!.release()
            mCamera = null
        }
    }

    companion object {
        var bitmap: Bitmap? = null
    }
}