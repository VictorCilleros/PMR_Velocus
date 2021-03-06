package com.velocus

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mCameraButton: Button? = null
    private var imgButtonSettings: ImageButton?= null

    private val ASR_PERMISSION_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bouton pour passer à l'activité principale (Caméra) :

        mCameraButton = findViewById<View>(R.id.camerabutton) as Button

        mCameraButton!!.setOnClickListener(View.OnClickListener {
            val mainActivityIntent = Intent(this@MainActivity, CameraActivity::class.java)
            startActivity(mainActivityIntent)
        })

        // Bouton pour passer au settings :

        imgButtonSettings = findViewById(R.id.settings) as ImageButton

        imgButtonSettings!!.setOnClickListener (View.OnClickListener {
            val mainActivityIntent = Intent(this@MainActivity, Settings::class.java)
            startActivity(mainActivityIntent)
        })

        // On vérifie si l'utilisateur à les permission nécessaire :

        verifyCameraPermissions()
        verifyAudioPermissions()

        // Bouton pour lancer la lecture micro :

        val btnMicro = findViewById<ImageButton>(R.id.btn_micro)

        btnMicro.setOnClickListener {
            askSpeechInput()
        }
    }

    private fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition is not available", Toast.LENGTH_SHORT).show()
        } else {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!")

            getResult.launch(i)
        }
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {

            if (it.resultCode == Activity.RESULT_OK) {
                val result = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
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

    private fun verifyCameraPermissions() {
        if (checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                ASR_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun handleCommands(vocal: String) {
        if (vocal.lowercase() == "démarrer") {
            Toast.makeText(this, "Et l'affichage démarre !", Toast.LENGTH_SHORT).show()

            // Passage à l'activité caméra
            val mainActivityIntent = Intent(this@MainActivity, CameraActivity::class.java)
            startActivity(mainActivityIntent)
        }
        else if (vocal.lowercase() == "arrêter") {
            Toast.makeText(this, "Et puis l'affichage s'arrête !", Toast.LENGTH_SHORT).show()
            this.finish() // On ferme l'appli
        }
        else {Toast.makeText(this, "Désolé, je ne comprends pas cette commande", Toast.LENGTH_SHORT).show()}

    }
}