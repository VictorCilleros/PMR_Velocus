package com.velocus.model

data class Station(
    var numero : Int = 0,
    var latitude : Double = 0.0,
    var longitude : Double = 0.0,
    var nom : String = "",
    var nb_place_tot : Int = 0,
    var nb_place_dispo : Int = 0
    ) {

    // Data class pour stocker dans la mémoire vive les carrastéristiques des stations

    override fun toString(): String {
        return "Station(latitude=$latitude, longitude=$longitude, nom='$nom', nb_place_tot=$nb_place_tot, nb_place_dispo=$nb_place_dispo)"
    }
}