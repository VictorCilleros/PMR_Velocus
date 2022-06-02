package com.velocus.View

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import com.velocus.R

/**
 * Created by 2poiz' on 18/05/2022
 */
class TestGpsView : SuperView {

    // Récupération du png pour l'image de fond d'écran :
    var imgFond: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.png)

    constructor(context: Context?) : super(context) {
        //postConstruct()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        //postConstruct()
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
            canvas.drawText("Longitude = "+this.mLongitudeTextView, (width/2).toFloat(), (height/7).toFloat()+f(100), paint)
            canvas.drawText(this.orientation.toString(), (width/2).toFloat(), (height/2).toFloat()-f(60), paint)

            // Affichage de l'angle magnétique par une lettre correspondant à l'orientation :
            var direction : String = Orientaion_boussole(this.orientation)
            paint.setColor(red)
            canvas.drawText(direction, (width/2).toFloat(), (height/2).toFloat()-f(160), paint)
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