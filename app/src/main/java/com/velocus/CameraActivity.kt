package com.velocus

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.velocus.BaseDeDonnee.DatabaseManager
import com.velocus.View.CameraView
import com.velocus.model.Gps
import com.velocus.model.Station
import java.util.*

class CameraActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var myContext: Context? = null
    private var cameraPreview: LinearLayout? = null

    private val ASR_PERMISSION_REQUEST_CODE = 0

    lateinit var cameraView : CameraView

    var gps : Gps? = null

    var stations : MutableList<Station>? = null

    var thetaH : Double=0.0
    var thetaV : Double=0.0

    lateinit var databaseManager : DatabaseManager

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

        databaseManager = DatabaseManager(this)

        if (databaseManager.nb_stations()==0){
            // appelle de l'API pour récupérer toutes les infos de toutes les stations
        }

        // stations = databaseManager.genrerate_stations()

        stations = MutableList<Station>(1){Station(50.62723799221388,3.109268199357267,"Mairie d'Hellemme",24,8) }
        stations!!.add(Station(50.619122956331886,3.1264709816213587,"Villeneuve-d'Ascq",27,22))
        stations!!.add(Station(50.63701166075154,3.0707240415241044,"Gare Lille Flandres",19,0))

        cameraView.a=this

        verifyAudioPermissions()

        val btnMicro = findViewById<ImageButton>(R.id.btn_micro)

        btnMicro.setOnClickListener {
            askSpeechInput()
        }
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

        gps?.sensorManager!!.registerListener(gps?.gyroListener, gps?.sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME)
        gps?.sensorManager!!.registerListener(gps?.gyroListener, gps?.sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
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

    private fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition is not available", Toast.LENGTH_SHORT).show()
        } else {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!")
            //startActivityForResult(i, RQ_SPEECH_REC)
            getResult.launch(i)
        }
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            val tvText = findViewById<TextView>(R.id.tv_text)

            if (it.resultCode == Activity.RESULT_OK) {
                val result = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                tvText.text = result?.get(0).toString()
                handleCommands(result?.get(0).toString())
            }
        }

    private fun verifyAudioPermissions() {
        if (checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                ASR_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun handleCommands(vocal: String) {
        if (vocal.lowercase() == "démarrer") {
            Toast.makeText(this, "Et l'affichage démarre !", Toast.LENGTH_SHORT).show()
        }
        else if (vocal.lowercase() == "arrêter") {
            Toast.makeText(this, "Et puis l'affichage s'arrête !", Toast.LENGTH_SHORT).show()
            val mainActivityIntent = Intent(this@CameraActivity, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }
        else {
            Toast.makeText(this, "Désolé, je ne comprends pas cette commande", Toast.LENGTH_SHORT).show()}

    }
}