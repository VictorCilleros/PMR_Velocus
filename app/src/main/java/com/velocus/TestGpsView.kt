package com.velocus

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Created by 2poiz' on 18/05/2022
 */
class TestGpsView : View, GestureDetector.OnGestureListener {

    // Couleur :
    private var red: Int=0
    private var black: Int=0
    private var teal_700: Int=0
    private var teal_200: Int=0

    //private val gestureDetector: GestureDetector? = null

    var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Attribues permettant l'affichae :
    var mLatitudeTextView: String ="" //Lattitude
    var mLongitudeTextView: String ="" //Longitude
    var orientation : Float = 0F //Angle magnétique entre 0 et 360°

    // Récupération du png pour l'image de fond d'écran :
    var imgFond: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.png)

    constructor(context: Context?) : super(context) {
        postConstruct()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        postConstruct()
    }

    fun postConstruct(){

        //val res = resources

        // Récupération des valeurs des couleurs depuis le fichier color des ressources :
        black = R.color.black
        red = R.color.red
        teal_200 = R.color.teal_200
        teal_700 = R.color.teal_700
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Fonction appelé lorsque la taille change (y compris au lancement de l'activité)
        // Cela nous permet de redimentionné l'image de fond d'écran :

        if (BitmapFactory.decodeResource(resources, R.drawable.png) != null) {
            imgFond = BitmapFactory.decodeResource(resources, R.drawable.png)
            imgFond = Bitmap.createScaledBitmap(imgFond, width * 5, height, true)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // On détermine quelle est la valeur de la bordure gauche de la partie de l'image du fond d'écran que l'on dois afficher :
        var X : Int = (width*4*(this.orientation.toDouble().div(360))).toInt()

        // On recré un Bitmap au dimension de l'écran, et commençant à X :
        val rogner: Bitmap = Bitmap.createBitmap(this.imgFond, Math.min(X,width * 4), 0, Math.min(width,width * 5-X), height)

        if (canvas != null) {
            // On affiche l'image de fond d'écran :
            canvas.drawBitmap(rogner, 0.toFloat(), 0.toFloat(), paint)

            // Parramétrage des carractéristiques des texts :
            paint.setColor(black)
            paint.setTextAlign(Paint.Align.CENTER)
            paint.setTextSize((width / 20).toFloat())

            // Affichage de la lattitude, longitude et l'angle magnétique :
            canvas.drawText("Latitude = "+this.mLatitudeTextView, (width/2).toFloat(), (height/7).toFloat(), paint)
            canvas.drawText("Longitude = "+this.mLongitudeTextView, (width/2).toFloat(), (height/7).toFloat()+f(100f), paint)
            canvas.drawText(this.orientation.toString(), (width/2).toFloat(), (height/2).toFloat()-f(60f), paint)

            // Affichage de l'angle magnétique par une lettre correspondant à l'orientation :
            var direction : String = orientaion_boussole(this.orientation)
            paint.setColor(red)
            canvas.drawText(direction, (width/2).toFloat(), (height/2).toFloat()-f(160f), paint)
        }
    }

    // Fonction pour remettre à l'echelle la position des texts selon la bonne position déterminé sur un appareil de taille 1080f :
    fun f(x: Float): Float {
        return width * x / 1080f
    }

    // Transformation de l'angle entre 0 et 360° en une indiquation d'orientation String :
    fun orientaion_boussole(num:Float): String {
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