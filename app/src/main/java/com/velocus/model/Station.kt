package com.velocus.model

/**
 * Created by 2poiz' on 01/06/2022
 */
data class Station(var latitude:Double,var longitude:Double,var nom:String = "",var nb_place_tot:Int=0,var nb_place_dispo:Int=0) {

    // Class temporaire !
    // Data class pour stocker dans la mémoir vive les carrastéristiques primaire des stations

    override fun toString(): String {
        return "Station(latitude=$latitude, longitude=$longitude, nom='$nom', nb_place_tot=$nb_place_tot, nb_place_dispo=$nb_place_dispo)"
    }
}