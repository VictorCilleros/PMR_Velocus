package com.velocus.View

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import com.velocus.CameraActivity
import com.velocus.R

/**
 * Created by 2poiz' on 31/05/2022
 */
class CameraView : SuperView {

    var a : CameraActivity? = null
    var imgCursor: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.pointeur)

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
            imgCursor = BitmapFactory.decodeResource(resources, R.drawable.pointeur)
            imgCursor = Bitmap.createScaledBitmap(imgCursor, f(200).toInt(), f(200).toInt(), true)
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null) {
            // On affiche ce qu'on veux ici
            paint.setColor(red)
            paint.setTextAlign(Paint.Align.CENTER)
            paint.setTextSize((width / 20).toFloat())

            var direction : String = Orientaion_boussole(this.orientation)
            //Log.d("patate", "onSensorChanged: $direction   ${this.orientation}")
            canvas.drawText(direction, (width/2).toFloat(), (height*3/4).toFloat(),paint)



            var orientation_rad = Math.toRadians(this.orientation.toDouble())

            if (this.a?.stations!=null){
                for (i in 0 until this.a?.stations!!.size){
                    var teta__ =angle_vecteur(this.mLatitudeTextView,this.mLongitudeTextView, this.a?.stations!![i].latitude, this.a?.stations!![i].longitude)
                    //Log.d("patate", "onDraw: $teta__ + $orientation_rad + ${a!!.thetaH}  +  $i")
                    if (teta__ > orientation_rad-(a!!.thetaH)/2 && teta__ < orientation_rad+(a!!.thetaH)/2){
                        //Log.d("patate", "Guess what ? $i + ${width*(((teta__-orientation_rad+(a!!.thetaH)/2)/ a!!.thetaH).toFloat())}  +  $width")
                        var x =width*(((teta__-orientation_rad+(a!!.thetaH)/2)/ a!!.thetaH).toFloat())

                        paint.setColor(red)
                        paint.setTextAlign(Paint.Align.CENTER)
                        paint.setTextSize((width / 15).toFloat())
                        canvas.drawText(a!!.stations?.get(i)?.nom.toString(),x, (height/4).toFloat()-f(200),paint)

                        paint.setTextSize((width / 20).toFloat())
                        canvas.drawText("Nb place total : "+a!!.stations?.get(i)?.nb_place_tot.toString(),x, (height/4).toFloat()-f(50),paint)
                        canvas.drawText("Distance : ?",x, (height/4).toFloat()+f(25),paint)
                        canvas.drawBitmap(imgCursor,x-f(100),(height/4).toFloat()+f(100),paint)
                        if (a!!.stations?.get(i)?.nb_place_dispo!! >=1){
                            paint.setColor(orange)
                        }
                        if (a!!.stations?.get(i)?.nb_place_dispo!! > a!!.stations?.get(i)?.nb_place_tot!! -8){
                            paint.setColor(green)
                        }
                        canvas.drawText("Nb place dispo : "+a!!.stations?.get(i)?.nb_place_dispo.toString(),x, (height/4).toFloat()-f(125),paint)

                    }
                }
            }

        }

        //invalidate()
    }

    fun angle_vecteur(X_u : Double, Y_u : Double, X : Double, Y : Double):Double{

        var cos_ = (X-X_u)/(Math.sqrt((X-X_u)*(X-X_u)+(Y-Y_u)*(Y-Y_u)))
        if (Y>Y_u){
            return Math.acos(cos_)
        }else {
            return Math.acos(cos_)+Math.PI
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