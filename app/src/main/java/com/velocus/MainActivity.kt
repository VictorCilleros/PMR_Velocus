package com.velocus

import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var mStartButton: Button? = null

    companion object {
        // Constante nécessaire pour vérifier la permission audio
        private const val ASR_PERMISSION_REQUEST_CODE = 0
    }

    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mIsListening = false // this will be needed later
    private var mUserInfoText: TextView? = null
    private var mUserUtteranceOutput: TextView? = null
    private var mCommandsList: MutableList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyAudioPermissions()

        // Bouton pour passer au test du GPS:

        mStartButton = findViewById<View>(R.id.button2) as Button

        mStartButton!!.setOnClickListener(View.OnClickListener {
            val mainActivityIntent = Intent(this@MainActivity, TestGpsActivity::class.java)
            startActivity(mainActivityIntent)
        })

        mUserUtteranceOutput = findViewById(R.id.user_utterance_output)
        mUserInfoText = findViewById(R.id.user_info_text)

        createSpeechRecognizer()

        val trigger = findViewById<ImageView>(R.id.trigger_icon)
        trigger.setOnClickListener {
            // Handle audio sessions here
            if (mIsListening) {
                handleSpeechEnd()
            } else {
                handleSpeechBegin()
            }
        }

        initCommands()

    }

    private fun handleSpeechBegin() {
        // start audio session
        mUserInfoText!!.setText(R.string.listening)
        mIsListening = true
        mSpeechRecognizer!!.startListening(createIntent())
    }

    private fun handleSpeechEnd() {
        // end audio session
        mUserInfoText!!.setText(R.string.detected_speech)
        mIsListening = false
        mSpeechRecognizer!!.cancel()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ASR_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // audio permission granted
                Toast.makeText(this, "Vous pouvez maintenant utiliser la commande vocale !", Toast.LENGTH_LONG).show()
            } else {
                // audio permission denied
                Toast.makeText(this, "Vous devez autoriser Velocus à accéder au micropour utiliser la commande vocale.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createSpeechRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        mSpeechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {
                handleSpeechEnd()
            }

            override fun onError(error: Int) {
                handleSpeechEnd()
            }

            override fun onResults(results: Bundle) {
                // Called when recognition results are ready. This callback will be called when the
                // audio session has been completed and user utterance has been parsed.

                // This ArrayList contains the recognition results, if the list is non-empty,
                // handle the user utterance
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // The results are added in decreasing order of confidence to the list
                    val command = matches[0]
                    mUserUtteranceOutput!!.text = command
                    handleCommand(command)
                }
            }

            override fun onPartialResults(partialResults: Bundle) {
                // Called when partial recognition results are available, this callback will be
                // called each time a partial text result is ready while the user is speaking.
                val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // handle partial speech results
                    val partialText = matches[0]
                    mUserUtteranceOutput!!.text = partialText
                }
            }

            override fun onEvent(eventType: Int, params: Bundle) {}
        })
    }

    private fun createIntent(): Intent {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        return i
    }

    private fun handleCommand(command: String) { // TODO : à compléter, on veut afficher les vélos sur la carte (je crois)
        if (mCommandsList!!.contains(command)) {
            // Successful utterance, notify user
            Toast.makeText(this, "Exécution de : $command", Toast.LENGTH_LONG).show()
        } else {
            // Unsucessful utterance, show failure message on screen
            Toast.makeText(this, "Désolé, je n'ai pas compris la commande.", Toast.LENGTH_LONG).show()
        }
    }

    private fun initCommands() {
        mCommandsList = ArrayList()
        mCommandsList!!.add("Montre-moi où sont les vélos dispos")
    }
}