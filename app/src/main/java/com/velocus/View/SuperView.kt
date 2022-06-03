package com.velocus.View

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.velocus.R

/**
 * Created by 2poiz' on 01/06/2022
 */
abstract class SuperView: View, GestureDetector.OnGestureListener {

    // Couleur :
    var red: Int=0
    var black: Int=0
    var teal_700: Int=0
    var teal_200: Int=0
    var orange: Int=0
    var green: Int=0

    //private val gestureDetector: GestureDetector? = null

    var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Attribues permettant le traitement des données GPS/orientation :
    var mLatitudeTextView: Double = 0.0 //Lattitude
    var mLongitudeTextView: Double =0.0 //Longitude
    var orientation : Float = 0F //Angle magnétique entre 0 et 360°

    constructor(context: Context?) : super(context) {
        postConstruct()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        postConstruct()
    }

    open fun postConstruct(){

        val res = resources

        // Récupération des valeurs des couleurs depuis le fichier color des ressources :
        black = res.getColor(R.color.black)
        red = res.getColor(R.color.red)
        teal_200 = res.getColor(R.color.teal_200)
        teal_700 = res.getColor(R.color.teal_700)
        orange= res.getColor(R.color.orange)
        green = res.getColor(R.color.green)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

    }

    // Fonction pour remettre à l'echelle la position des texts selon la bonne position déterminé sur un appareil de taille 1080f :
    fun f(x: Int): Float {
        return width * x / 1080f
    }

    // Transformation de l'angle entre 0 et 360° en une indiquation d'orientation String :
    fun Orientaion_boussole(num:Float): String {
        return when(num){
            in 11.25f..33.75f->"NNE"
            in 33.75f..56.25f->"NE"
            in 56.25f..78.75f->"ENE"
            in 78.75f..101.25f->"E"
            in 101.25f..123.75f->"ESE"
            in 123.75f..146.25f->"SE"
            in 146.25f..168.25f->"SSE"
            in 168.25f..191.25f->"S"
            in 191.25f..213.75f->"SSO"
            in 213.75f..236.25f->"SO"
            in 236.25f..258.75f->"OSO"
            in 258.75f..281.25f->"O"
            in 281.25f..303.75f->"ONO"
            in 303.75f..326.25f->"NO"
            in 326.25f..348.75f->"NNO"
            else -> "N"
        }
    }




    // Fonctionnalités de View non utilisé :

    override fun onDown(e: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onShowPress(e: MotionEvent?) {
        TODO("Not yet implemented")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun onLongPress(e: MotionEvent?) {
        TODO("Not yet implemented")
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        TODO("Not yet implemented")
    }
}