package com.velocus.View

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.velocus.CameraActivity
import com.velocus.R
import com.velocus.model.Station
import java.lang.Integer.min
import kotlin.math.PI
import kotlin.math.log
import kotlin.math.max

/**
 * Created by 2poiz' on 31/05/2022
 */
class CameraView : SuperView {

    // Class permettant de traiter l'afficher :

    var a : CameraActivity? = null // Récupération de l'activité context lié à cette view

    var imgCursor: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.pointeur) // Récupération de l'image du pointeur
    var imgVelo: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.velo) // Récupération de l'image du velo
    var imgPieton: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.pieton) // Récupération de l'image du pieton

    // Attribut permettant de gérer les différents mode d'affichage :

    var nb_station_affichage : Int = 6
    var nom_station_select : String = ""
    var distance_search : Int = 3000 // en mètre

    // Si nb_station_affichage == 0 : Mode radius avec distance_search
    // Si nb_station_affichage == 1 et nom_station_select == un nom de station : Mode station unique (affiche uniquement la station enrefgistrer nom_station_select)
    // Sinon affiche les nb_station_affichage les plus proche


    var mode_velo : Boolean = true // true => on affiche les place dispo : false => on affiche les vélos dispo

    constructor(context: Context?) : super(context) {
        //postConstruct()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        //postConstruct()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Fonction appelé lorsque la taille change (y compris au lancement de l'activité)
        // Cela nous permet de redimentionné les images :
        if (BitmapFactory.decodeResource(resources, R.drawable.png) != null) {
            imgCursor = BitmapFactory.decodeResource(resources, R.drawable.pointeur)
            imgCursor = Bitmap.createScaledBitmap(imgCursor, (width/5).toInt(), (width/5).toInt(), true)
            imgVelo = BitmapFactory.decodeResource(resources, R.drawable.velo)
            imgVelo = Bitmap.createScaledBitmap(imgVelo, (width/5).toInt(), (width/10).toInt(), true)
            imgPieton = BitmapFactory.decodeResource(resources, R.drawable.pieton)
            imgPieton = Bitmap.createScaledBitmap(imgPieton, (width/10).toInt(), (width/10).toInt(), true)
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null) {
            // On affiche ce qu'on veux ici :

            // Affichage de l'image en haut au centre indiquant à l'utilisateur s'il est en mode vélo ou en mode piéton :
            if (this.mode_velo){
                canvas.drawBitmap(imgVelo,(width/2-(width/10)).toFloat(),f(50),paint)
            }else{
                canvas.drawBitmap(imgPieton,(width/2-(width/20)).toFloat(),f(50),paint)
            }

            // Parramétrage de paint pour les éléments textuels :
            paint.setColor(red)
            paint.setTextAlign(Paint.Align.CENTER)
            paint.setTextSize((width / 20).toFloat())

            // Affichage de la/les lettres indiquant l'orientation magnétique
            var direction : String = Orientaion_boussole(this.orientation)
            canvas.drawText(direction, (width/2).toFloat(), (height*3/4).toFloat(),paint)

            // Récupération de l'angle entre l'utilisateur et le Nord
            var orientation_rad = Math.toRadians(this.orientation.toDouble())

            //Log.d("orientation", "orientation: ${this.orientation}")

            if (this.a?.stations!=null){

                if (nb_station_affichage==0){ // Mode Radius
                    for (i in 0 until this.a?.stations!!.size){ // Recherche de toutes les station dans le périmètre défini
                        if (distance(Math.toRadians(this.mLatitude),Math.toRadians(this.mLongitude),Math.toRadians(a!!.stations?.get(i)!!.latitude),Math.toRadians(a!!.stations?.get(i)!!.longitude))<distance_search){
                            var teta__ =angle_vecteur(this.mLatitude,this.mLongitude, this.a?.stations!![i].latitude, this.a?.stations!![i].longitude)
                            is_draw_station(teta__,orientation_rad,canvas,i)
                        }
                    }

                }else if(nb_station_affichage==1 && nom_station_select!=""){ // Mode station unique
                    for (i in 0 until this.a?.stations!!.size){
                        if (a!!.stations?.get(i)!!.nom.equals(nom_station_select)){
                            var teta__ =angle_vecteur(this.mLatitude,this.mLongitude, this.a?.stations!![i].latitude, this.a?.stations!![i].longitude)
                            is_draw_station(teta__,orientation_rad,canvas,i)
                        }
                    }

                }else{ // Mode x station plus proche
                    var list_indice_station_plus_proche = MutableList(nb_station_affichage){index ->  0}
                    var list_distance_station_plus_proche = MutableList(nb_station_affichage){index ->  1000000000}
                    for (i in 0 until this.a?.stations!!.size){ // Classement des x stations les plus proches
                        var d = distance(Math.toRadians(this.mLatitude),Math.toRadians(this.mLongitude),Math.toRadians(a!!.stations?.get(i)!!.latitude),Math.toRadians(a!!.stations?.get(i)!!.longitude))

                        for (j in 0 until nb_station_affichage){
                            if (d<list_distance_station_plus_proche[j]){
                                list_indice_station_plus_proche.add(j,i)
                                list_distance_station_plus_proche.add(j,d)
                                list_indice_station_plus_proche.remove(nb_station_affichage-1)
                                list_distance_station_plus_proche.remove(nb_station_affichage-1)
                                d = 1000000000
                            }
                        }
                    }

                    for (i in 0 until nb_station_affichage){
                        if (list_distance_station_plus_proche[nb_station_affichage-i-1]!=1000000000){
                            var teta__ =angle_vecteur(this.mLatitude,this.mLongitude, this.a?.stations!![list_indice_station_plus_proche[nb_station_affichage-i-1]].latitude, this.a?.stations!![list_indice_station_plus_proche[nb_station_affichage-i-1]].longitude)
                            is_draw_station(teta__,orientation_rad,canvas,list_indice_station_plus_proche[nb_station_affichage-i-1])
                        }
                    }
                }
            }
        }
    }

    // Fonction déterminant la position sur l'écran utilisateur des station à afficher :
    fun is_draw_station(teta__ : Double, orientation_rad: Double, canvas: Canvas, i : Int){
        if (is_in_vue(teta__, orientation_rad)){
            var orien = orientation_rad
            var te = teta__

            if (orientation_rad<(a!!.thetaH)/2){
                orien=orientation_rad+(a!!.thetaH)/2
                te=(teta__+(a!!.thetaH)/2)%(2*PI)
            }else if (orientation_rad>(2*PI)-(a!!.thetaH)/2){
                orien=orientation_rad-(a!!.thetaH)/2
                te=((2*PI)+teta__-(a!!.thetaH)/2)%(2*PI)
            }

            var x = width*(((te-orien+(a!!.thetaH)/2)/ a!!.thetaH).toFloat())
            Draw_station(a!!.stations?.get(i)!!, canvas, x)
        }
    }

    // Affichage d'uns station :
    fun Draw_station(s : Station, canvas : Canvas, x :Float){
        // Récupération de la distance en mètre entre l'utilisateur et la station à afficher
        var distance_ = distance(Math.toRadians(this.mLatitude),Math.toRadians(this.mLongitude),Math.toRadians(s.latitude),Math.toRadians(s.longitude))

        // Détermination des facteur de réduction proportionnel à la distance de la station :
        var proportion_distance = min(max(distance_,100),distance_search)-100
        var ecart_entre_les_texts = 75 - proportion_distance*35/(distance_search-100)

        // Initialisation du filtre proportionnel à la distance de la station :
        var filter = LightingColorFilter(0xFFFFFF, 0x000000)

        when(proportion_distance){
            in (distance_search-100)/10..(distance_search-100)*2/10-> filter = LightingColorFilter(0xFFFFFF, 0x101010)
            in (distance_search-100)*2/10..(distance_search-100)*3/10-> filter = LightingColorFilter(0xFFFFFF, 0x202020)
            in (distance_search-100)*3/10..(distance_search-100)*4/10-> filter = LightingColorFilter(0xFFFFFF, 0x303030)
            in (distance_search-100)*4/10..(distance_search-100)*5/10-> filter = LightingColorFilter(0xFFFFFF, 0x404040)
            in (distance_search-100)*5/10..(distance_search-100)*6/10-> filter = LightingColorFilter(0xFFFFFF, 0x505050)
            in (distance_search-100)*6/10..(distance_search-100)*7/10-> filter = LightingColorFilter(0xFFFFFF, 0x606060)
            in (distance_search-100)*7/10..(distance_search-100)*8/10-> filter = LightingColorFilter(0xFFFFFF, 0x707070)
            in (distance_search-100)*8/10..(distance_search-100)*9/10-> filter = LightingColorFilter(0xFFFFFF, 0x808080)
            in (distance_search-100)*9/10..(distance_search-100)-> filter = LightingColorFilter(0xFFFFFF, 0x909090)
        }

        // On applique le filtre à notre paint :
        paint.setColorFilter(filter)

        // Affichage du nom de la station :
        paint.setColor(red)
        paint.setTextAlign(Paint.Align.CENTER)
        paint.setTextSize((width / (15+proportion_distance*15/(distance_search-100))).toFloat())
        canvas.drawText(s.nom,x, (height/4).toFloat()-ecart_entre_les_texts,paint)

        // Affichage du nom de place totale de la station :
        paint.setTextSize((width / (20+proportion_distance*15/(distance_search-100))).toFloat())
        canvas.drawText("Nb place total : "+s.nb_place_tot.toString(),x, (height/4).toFloat()+ecart_entre_les_texts,paint)

        // Affichage de la distance entre l'utilisateur et la station :
        if (distance_>1000){
            canvas.drawText("Distance : ${((distance_).toFloat()/1000)} km",x, (height/4).toFloat()+ecart_entre_les_texts*2,paint)
        }else{
            canvas.drawText("Distance : $distance_ m",x, (height/4).toFloat()+ecart_entre_les_texts*2,paint)
        }

        // Redimentionnemnt du Bitmap du pointeur et affichage de ce dernier :
        var rogner = Bitmap.createScaledBitmap(imgCursor, (width/(5+proportion_distance*10/(distance_search-100))).toInt(), width/(5+proportion_distance*10/(distance_search-100)).toInt(), true)
        canvas.drawBitmap(rogner,x-(width/(5+proportion_distance*10/(distance_search-100)))/2,(height/4).toFloat()+ecart_entre_les_texts*3,paint)

        // On enlève le filtre :
        paint.setColorFilter(null)

        if (this.mode_velo){ // Affichage du nombre de place vide disponible
            if (s.nb_place_dispo >=1){
                paint.setColor(orange)
            }
            if (s.nb_place_dispo >= 5){
                paint.setColor(green)
            }
            canvas.drawText("Nb place dispo : "+s.nb_place_dispo.toString(),x, (height/4).toFloat(),paint)
        }else{ // Affichage du nombre de vélo disponible
            var nb_veo_dispo = s.nb_place_tot-s.nb_place_dispo
            if (nb_veo_dispo >=1){
                paint.setColor(orange)
            }
            if (nb_veo_dispo >= 5){
                paint.setColor(green)
            }
            canvas.drawText("Nb de vélo dispo : $nb_veo_dispo",x, (height/4).toFloat(),paint)
        }
    }

    // Fonction déterminant si l'angle de la station est dans le champ de vision de l'utilisateur :
    fun is_in_vue(teta__:Double, orientation_rad:Double):Boolean{
        if (orientation_rad<(a!!.thetaH)/2){
            return (teta__<orientation_rad+(a!!.thetaH)/2 || teta__>(2*PI)+orientation_rad-(a!!.thetaH)/2)
        }else if (orientation_rad>((2*PI)-((a!!.thetaH)/2))){
            return (teta__<orientation_rad-(2*PI)+(a!!.thetaH)/2  || teta__ > orientation_rad-(a!!.thetaH)/2)
        }else{
            return (teta__ > orientation_rad-(a!!.thetaH)/2 && teta__ < orientation_rad+(a!!.thetaH)/2)
        }
    }

    // Angle entre deux vecteurs :
    fun angle_vecteur(X_u : Double, Y_u : Double, X : Double, Y : Double):Double{
        var cos_ = (X-X_u)/(Math.sqrt((X-X_u)*(X-X_u)+(Y-Y_u)*(Y-Y_u)))

        if (Y>Y_u){
            return Math.acos(cos_)
        }else {
            return Math.PI*2-Math.acos(cos_)
        }
    }

    fun distance(LatA:Double, LogA:Double , LatB:Double , LogB:Double):Int{ //retourne la distance en mètre
        return (Math.acos(Math.sin(LatA)*Math.sin(LatB)+Math.cos(LatA)*Math.cos(LatB)*Math.cos(LogA-LogB))*6378137).toInt() //Rayon de la terre = 6378127
    }



    // Fonctionnalités de View non utilisé :

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onDown(e: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onShowPress(e: MotionEvent?) {
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