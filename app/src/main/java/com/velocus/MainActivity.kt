package com.velocus

import android.Manifest.permission.RECORD_AUDIO
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.ActivityCompat
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mStartButton: Button? = null
    private var mCameraButton: Button? = null

    private val ASR_PERMISSION_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bouton pour passer au test du GPS:

        mStartButton = findViewById<View>(R.id.button2) as Button

        mStartButton!!.setOnClickListener(View.OnClickListener {
            val mainActivityIntent = Intent(this@MainActivity, TestGpsActivity::class.java)
            startActivity(mainActivityIntent)
        })

        verifyAudioPermissions()

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
        if (checkCallingOrSelfPermission(RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(RECORD_AUDIO),
                ASR_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun handleCommands(vocal: String) {
        if (vocal.lowercase() == "démarrer") {
            Toast.makeText(this, "Et l'affichage démarre !", Toast.LENGTH_SHORT).show()
            // TODO implémenter l'affichage
        }
        else if (vocal.lowercase() == "arrêter") {
            Toast.makeText(this, "Et puis l'affichage s'arrête !", Toast.LENGTH_SHORT).show()
            // TODO arrêter l'affichage
        }
        else {Toast.makeText(this, "Désolé, je ne comprends pas cette commande", Toast.LENGTH_SHORT).show()}

        // Bouton pour passer au test de la Caméra:

        mCameraButton = findViewById<View>(R.id.camerabutton) as Button

        mCameraButton!!.setOnClickListener(View.OnClickListener {
            val mainActivityIntent = Intent(this@MainActivity, CameraActivity::class.java)
            startActivity(mainActivityIntent)
        })
    }
}