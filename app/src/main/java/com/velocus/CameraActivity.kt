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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*

class CameraActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var myContext: Context? = null
    private var cameraPreview: LinearLayout? = null

    private val ASR_PERMISSION_REQUEST_CODE = 0

    lateinit var cameraView : CameraView

    var gps : Gps? = null

    var stations : MutableList<Station> = MutableList<Station>(0){index -> Station(0,0.0,0.0,"",0,0)}

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

        /*
        stations = MutableList<Station>(1){Station(0,50.62723799221388,3.109268199357267,"Mairie d'Hellemmes",24,8) }
        stations!!.add(Station(1,50.619122956331886,3.1264709816213587,"Villeneuve-d'Ascq",27,22))
        stations!!.add(Station(2,50.63701166075154,3.0707240415241044,"Gare Lille Flandres",19,0))*/

        if (databaseManager.nb_stations()==0){
            getCode("https://www.ilevia.fr/cms/institutionnel/velo/stations-vlille/")
            /*
            for (i in 1..225){
                databaseManager.insert_station(stations!![i])
            }*/
        }else{
            stations = databaseManager.genrerate_stations()!!
        }

        cameraView.a=this

        verifyAudioPermissions()

        val btnMicroCam = findViewById<ImageButton>(R.id.btn_micro_cam)

        btnMicroCam.setOnClickListener {
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
            val tvText = findViewById<TextView>(R.id.tv_text_cam)

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
        if (vocal.lowercase() == "arrêter") {
            Toast.makeText(this, "Et puis l'affichage s'arrête !", Toast.LENGTH_SHORT).show()

            this.finish()
        } else {
            var compt = 0
            for (i in this.stations!!) {
                if (i.nom.equals(vocal.lowercase(), true)) {
                    Toast.makeText(this, "Mode station unique activé !", Toast.LENGTH_SHORT).show()
                    compt = 1
                    this.cameraView.nom_station_select = i.nom
                    this.cameraView.nb_station_affichage = 1
                    cameraView.invalidate()
                }
            }
            if (compt == 0) {
                Toast.makeText(this, "Désolé, je ne comprends pas cette commande : ${vocal.lowercase()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getCode(url: String?) {
        var thread = Thread {
            try {
                var code = ""
                val site = URL(url)
                var buff = BufferedReader(InputStreamReader(site.openStream()))
                for (i in 1..942){
                    buff.readLine()
                }


                for (i in 1..225){
                    buff.readLine()
                    var ligne2 = buff.readLine()
                    var nb = ligne2.slice(22 until (ligne2.indexOf("</td>"))).toInt()

                    var ligne3 = buff.readLine()
                    var nom = ligne3.slice(18 until (ligne3.indexOf("</td>")))

                    buff.readLine()
                    buff.readLine()
                    buff.readLine()
                    
                    var ligne7 = buff.readLine()
                    var nb_velo_dispo = ligne7.slice(33 until (ligne7.indexOf("</span>"))).toInt()
                    
                    var ligne8 = buff.readLine()
                    var nb_place_dispo = ligne8.slice(34 until (ligne8.indexOf("</span>"))).toInt()

                    buff.readLine()

                    var ligne10  = buff.readLine()
                    var lon = ligne10.slice(35 until (ligne10.indexOf("data-latitude")-2)).toDouble()
                    var lat = ligne10.slice((ligne10.indexOf("data-latitude")+15) until (ligne10.indexOf("><span")-1)).toDouble()

                    buff.readLine()
                    buff.readLine()

                    stations.add(Station(nb,lat,lon,nom,nb_velo_dispo+nb_place_dispo,nb_place_dispo))
                }

                for (i in 0 until stations!!.size){
                    Log.d("patate", stations!![i].toString())
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        thread.start()
    }
}