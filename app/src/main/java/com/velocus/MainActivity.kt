package com.velocus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var mStartButton: Button? = null
    private var mCameraButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bouton pour passer au test du GPS:

        mStartButton = findViewById<View>(R.id.button2) as Button

        mStartButton!!.setOnClickListener(View.OnClickListener {
            val MainActivityIntent = Intent(this@MainActivity, TestGpsActivity::class.java)
            startActivity(MainActivityIntent)
        })

        // Bouton pour passer au test de la Caméra:

        mCameraButton = findViewById<View>(R.id.camerabutton) as Button

        mCameraButton!!.setOnClickListener(View.OnClickListener {
            val MainActivityIntent = Intent(this@MainActivity, CameraActivity::class.java)
            startActivity(MainActivityIntent)
        })
    }
}