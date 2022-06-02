package com.velocus

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.velocus.View.TestGpsView
import com.velocus.model.Gps

class TestGpsActivity : AppCompatActivity() {

    lateinit var testGpsView : TestGpsView

    var gps : Gps? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_gps)

        // Initialisation de GameViewTestGps :
        testGpsView = TestGpsView(this)
        testGpsView = findViewById(R.id.gameViewTestGps)

        // Initialisation du GPS :
        gps = Gps(this,testGpsView)
        gps!!.checkLocation() // vérifiez si le service de localisation est activé ou non sur votre téléphone
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

    override fun onResume() {
        super.onResume()
        // Attribution/rattachement, des capteurs ROTATION_VECTEUR à l'attribut "gyroListener" :
        gps?.sensorManager!!.registerListener(gps?.gyroListener, gps?.sensorManager!!.getDefaultSensor( Sensor.TYPE_ROTATION_VECTOR ), SensorManager.SENSOR_DELAY_GAME)
    }
}