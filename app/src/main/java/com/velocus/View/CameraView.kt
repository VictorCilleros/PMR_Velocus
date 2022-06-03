package com.velocus.View

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import com.velocus.CameraActivity
import com.velocus.R
import com.velocus.model.Station

/**
 * Created by 2poiz' on 31/05/2022
 */
class CameraView : SuperView {

    var a : CameraActivity? = null
    var imgCursor: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.pointeur) //Récupération de l'image du pointeur

    var nb_station_affichage : Int = 3
    var nom_station_select : String = ""
    var distance_search : Int = 2000 // en mètre

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
            // On affiche ce qu'on veux ici :

            // Parramétrage de pint pour les éléments textuels :
            paint.setColor(red)
            paint.setTextAlign(Paint.Align.CENTER)
            paint.setTextSize((width / 20).toFloat())

            var direction : String = Orientaion_boussole(this.orientation)
            //Log.d("patate", "onSensorChanged: $direction   ${this.orientation}")
            canvas.drawText(direction, (width/2).toFloat(), (height*3/4).toFloat(),paint)

            var orientation_rad = Math.toRadians(this.orientation.toDouble())

            if (this.a?.stations!=null){

                if (nb_station_affichage==0){
                    for (i in 0 until this.a?.stations!!.size){
                        if (distance(Math.toRadians(this.mLatitudeTextView),Math.toRadians(this.mLongitudeTextView),Math.toRadians(a!!.stations?.get(i)!!.latitude),Math.toRadians(a!!.stations?.get(i)!!.longitude))<distance_search){
                            var teta__ =angle_vecteur(this.mLatitudeTextView,this.mLongitudeTextView, this.a?.stations!![i].latitude, this.a?.stations!![i].longitude)
                            if (teta__ > orientation_rad-(a!!.thetaH)/2 && teta__ < orientation_rad+(a!!.thetaH)/2){
                                var x =width*(((teta__-orientation_rad+(a!!.thetaH)/2)/ a!!.thetaH).toFloat())
                                Draw_station(a!!.stations?.get(i)!!, canvas, x)
                            }
                        }
                    }
                }else if(nb_station_affichage==1 && nom_station_select!=""){
                    for (i in 0 until this.a?.stations!!.size){
                        if (a!!.stations?.get(i)!!.nom.equals(nom_station_select)){
                            var teta__ =angle_vecteur(this.mLatitudeTextView,this.mLongitudeTextView, this.a?.stations!![i].latitude, this.a?.stations!![i].longitude)
                            if (teta__ > orientation_rad-(a!!.thetaH)/2 && teta__ < orientation_rad+(a!!.thetaH)/2){
                                var x =width*(((teta__-orientation_rad+(a!!.thetaH)/2)/ a!!.thetaH).toFloat())
                                Draw_station(a!!.stations?.get(i)!!, canvas, x)
                            }
                        }
                    }
                }else{
                    var list_indice_station_plus_proche = MutableList(nb_station_affichage){index ->  0}
                    var list_distance_station_plus_proche = MutableList(nb_station_affichage){index ->  1000000000}
                    for (i in 0 until this.a?.stations!!.size){
                        var d = distance(Math.toRadians(this.mLatitudeTextView),Math.toRadians(this.mLongitudeTextView),Math.toRadians(a!!.stations?.get(i)!!.latitude),Math.toRadians(a!!.stations?.get(i)!!.longitude))
                        for (j in 0 until nb_station_affichage){
                            if (d<list_distance_station_plus_proche[j]){
                                list_indice_station_plus_proche[j]=i
                                list_distance_station_plus_proche[j]=d
                                d = 1000000000
                            }
                        }
                    }

                    for (i in 0 until nb_station_affichage){
                        if (list_distance_station_plus_proche[i]!=1000000000){
                            var teta__ =angle_vecteur(this.mLatitudeTextView,this.mLongitudeTextView, this.a?.stations!![i].latitude, this.a?.stations!![i].longitude)
                            //Log.d("patate", "onDraw: $teta__ + $orientation_rad + ${a!!.thetaH}  +  $i")
                            if (teta__ > orientation_rad-(a!!.thetaH)/2 && teta__ < orientation_rad+(a!!.thetaH)/2){

                                //Log.d("patate", "Guess what ? $i + ${width*(((teta__-orientation_rad+(a!!.thetaH)/2)/ a!!.thetaH).toFloat())}  +  $width")
                                var x =width*(((teta__-orientation_rad+(a!!.thetaH)/2)/ a!!.thetaH).toFloat())

                                Draw_station(a!!.stations?.get(i)!!, canvas, x)
                            }
                        }
                    }
                }
            }

        }

        //invalidate()
    }

    fun Draw_station(s : Station, canvas : Canvas, x :Float){
        var distance_ = distance(Math.toRadians(this.mLatitudeTextView),Math.toRadians(this.mLongitudeTextView),Math.toRadians(s.latitude),Math.toRadians(s.longitude))
        // 135 m ; 2850 m ; 1500 m

        if (distance_>1000){
            var filter = LightingColorFilter(-0x808081, 0x0000000)
            paint.setColorFilter(filter)
        }

        paint.setColor(red)
        paint.setTextAlign(Paint.Align.CENTER)
        paint.setTextSize((width / 15).toFloat())
        canvas.drawText(s.nom,x, (height/4).toFloat()-f(200),paint)

        paint.setTextSize((width / 20).toFloat())
        canvas.drawText("Nb place total : "+s.nb_place_tot.toString(),x, (height/4).toFloat()-f(50),paint)

        if (distance_>1000){
            canvas.drawText("Distance : ${((distance_).toFloat()/1000)} km",x, (height/4).toFloat()+f(25),paint)
        }else{
            canvas.drawText("Distance : $distance_ m",x, (height/4).toFloat()+f(25),paint)
        }

        canvas.drawBitmap(imgCursor,x-f(100),(height/4).toFloat()+f(100),paint)
        if (s.nb_place_dispo!! >=1){
            paint.setColor(orange)
        }
        if (s.nb_place_dispo!! > s.nb_place_tot!! -8){
            paint.setColor(green)
        }
        canvas.drawText("Nb place dispo : "+s.nb_place_dispo.toString(),x, (height/4).toFloat()-f(125),paint)

        paint.setColorFilter(null)
    }

    fun angle_vecteur(X_u : Double, Y_u : Double, X : Double, Y : Double):Double{

        var cos_ = (X-X_u)/(Math.sqrt((X-X_u)*(X-X_u)+(Y-Y_u)*(Y-Y_u)))
        if (Y>Y_u){
            return Math.acos(cos_)
        }else {
            return Math.acos(cos_)+Math.PI
        }

    }

    fun distance(LatA:Double, LogA:Double , LatB:Double , LogB:Double):Int{ //retourne la distance en mètre
        return (Math.acos(Math.sin(LatA)*Math.sin(LatB)+Math.cos(LatA)*Math.cos(LatB)*Math.cos(LogA-LogB))*6378137).toInt() //Rayon de la terre = 6378127
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