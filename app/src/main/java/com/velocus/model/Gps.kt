package com.velocus.model

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.velocus.View.SuperView
import java.lang.Math.abs


/**
 * Created by 2poiz' on 20/05/2022
 */
class Gps( var activity_ : Activity, var superView : SuperView) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000          /* 2 sec */

    private var mLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private var locationManager: LocationManager? = null

    private val isLocationEnabled: Boolean
        get() {
            locationManager = activity_.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER)
        }

    var mGoogleApiClient = GoogleApiClient.Builder(activity_)
    .addConnectionCallbacks(this)
    .addOnConnectionFailedListener(this)
    .addApi(LocationServices.API)
    .build()

    var mLocationManager = activity_.getSystemService( Context.LOCATION_SERVICE ) as LocationManager
    var sensorManager = activity_.getSystemService( Context.SENSOR_SERVICE ) as SensorManager

    var gyroListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, acc: Int) {}// Fonction appelé lorsque qu'un changement sur les vitesse de changement des capteurs initialisés est détecté
        override fun onSensorChanged(event: SensorEvent) {
            // Fonction appelé lorsque qu'un changement sur les valeurs des capteurs initialisés est détecté :

            synchronized(this){
                if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) { // Si plusieurs capteur son utilis sur le même object sensor on ne s'intéresse ici qu'au TYPE_ROTATION_VECTOR

                    var mRotation = FloatArray(3)

                    // Conversion du vecteur de rotation en une matrice 4x4.
                    val mRotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values)
                    val adjustedRotationMatrix = FloatArray(9)
                    SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix)
                    SensorManager.getOrientation(adjustedRotationMatrix, mRotation)

                    // Transformation des 3 angles radian relatif en dégrès positif :
                    mRotation[0] = (Math.toDegrees(mRotation[0].toDouble()).toFloat()+360)%360
                    mRotation[1] = (Math.toDegrees(mRotation[1].toDouble()).toFloat()+360)%360
                    mRotation[2] = (Math.toDegrees(mRotation[2].toDouble()).toFloat()+360)%360

                    // Moyenne glissante (on ne considère le changement d'orientation que si cette dernière est significative) :
                    if (abs(superView?.orientation - mRotation[0])>=330){ // Cas ou on passe entre 0 et 360
                        if (abs(superView?.orientation - abs(mRotation[0]-359))>=3){
                            superView?.orientation = mRotation[0] // Récupération de l'orientation magnétique
                            superView?.invalidate() // Actualisation de l'affichage
                        }
                    }else{
                        if (abs(superView?.orientation - mRotation[0])>=3){
                            superView?.orientation = mRotation[0] // Récupération de l'orientation magnétique
                            superView?.invalidate() // Actualisation de l'affichage
                        }
                    }
                }
            }
        }
    }

    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(activity_, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity_, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // : Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            // Should we show an explanation?

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this.activity_,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this.activity_)
                    .setTitle("Demande d'accès à la localisation :")
                    .setMessage("L'application nécessite l'accès à la localisation")
                    .setPositiveButton(R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i -> //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(
                                this.activity_,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                99
                            )
                        })
                    .create()
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this.activity_, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    99
                )
            }
        }

        // initialisation des parramètre de localisation :
        startLocationUpdates()

        // Récupération des information de localisation :
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

        if (mLocation == null) {
            startLocationUpdates()
        }
        if (mLocation != null) {
            //activity_.testGpsView?.mLatitudeTextView = mlocation.latitude.toString()
            //activity_.testGpsView?.mLongitudeTextView = mlocation.longitude.toString()
        } else {
            Toast.makeText(activity_, "Location not Detected", Toast.LENGTH_SHORT).show() // Message de prévention pour l'utilisateur pour lui indiqué que sa localisation ne fonctionne pas
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        // Si la connection à été suspendue, un message est écris dans les Logcats et on essaie de récupérer la connection
        Log.i("gps", "Connection Suspended")
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // Si la connection est perdu, un message est écris dans les Logcats :
        Log.i("gps", "Connection failed. Error: " + connectionResult.getErrorCode())
    }

    @SuppressLint("MissingPermission")
    protected fun startLocationUpdates() {
        // Création de la demande de localisation :
        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL)

        // Demande d'actualisation de la localisation :
        if (ActivityCompat.checkSelfPermission(activity_, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity_, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
            mLocationRequest, this)
        Log.d("reque", "--->>>>")
    }

    override fun onLocationChanged(location: Location?) {
        // Fonction appelé lorsque la localisation change :

        val msg = "Updated Location: " +
                location?.let { java.lang.Double.toString(it.latitude) } + "," +
                location?.let { java.lang.Double.toString(it.longitude) }
        if (location != null) {
            // Moyenne glissante (on ne considère le changement d'orientation que si cette dernière est significative) :
            if (abs((superView?.mLatitudeTextView*10000).toInt() - (location.latitude*10000).toInt())>=2){
                superView?.mLatitudeTextView = location.latitude // Récupération de la latitude
                superView?.invalidate() // Actualisation de l'affichage
            }
            if (abs((superView?.mLongitudeTextView*10000).toInt() - (location.longitude*10000).toInt())>=2){
                superView?.mLongitudeTextView = location.longitude // Récupération de la longitude
                superView?.invalidate() // Actualisation de l'affichage
            }
        }
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun checkLocation(): Boolean {
        // Vérification que le service de localisation est activé ou non sur votre téléphone
        if (!isLocationEnabled)
            showAlert()
        return isLocationEnabled
    }

    private fun showAlert() {
        // Message informant l'utilisateur que le service de localisation n'est pas activé :
        val dialog = AlertDialog.Builder(activity_)
        dialog.setTitle("Enable Location")
            .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
            .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity_.startActivity(myIntent)
            }
            .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }
}