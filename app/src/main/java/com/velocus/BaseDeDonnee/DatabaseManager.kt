package com.velocus.BaseDeDonnee

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.velocus.model.Station
import java.lang.String

/**
 * Created by 2poiz' on 11/06/2022
 */
class DatabaseManager(context: Context?) : SQLiteOpenHelper(context, "Stations.db", null, 3){

    // Base de donnée pour stocker dans la mémoir morte toutes les caractéristiques de toutes les stations :

    override fun onCreate(db: SQLiteDatabase) { // Création de la table des Stations
        var strSql = "create table Station (" +
                " idStation integer primary key," +
                " latitude real not null," +
                " longitude real not null," +
                " nom text not null," +
                " nb_place_tot integer not null," +
                " nb_place_dispo integer not null" +
                ")"
        db.execSQL(strSql)
        Log.i("DATABASE", "oncreat")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { // En cas de mise à jour de la base de donnée, on supprime tout et on recrée tout
        var strSql = "drop table if exists Station"
        db.execSQL(strSql)
        this.onCreate(db)
    }

    fun nb_stations(): Int{ // Fonction retournant le nombre de stations enregistré dans la database, pour pouvoir la mettre à jour si besoin
        val cur = this.writableDatabase.rawQuery("Select COUNT(*) FROM Station", null)
        cur.moveToFirst()
        return cur.getInt(0)
    }

    fun is_in_stations(nom_station: kotlin.String): Boolean{ // Fonction retournant si une station dans la base de donnée possède bien ce nom
        var nom_ = nom_station.replace("'","''")
        val cur = this.writableDatabase.rawQuery("Select COUNT(*) FROM Station Where nom = '$nom_' COLLATE NOCASE ", null)
        cur.moveToFirst()
        return cur.getInt(0)!=0
    }

    fun insert_station(station : Station) { // Insertion d'une nouvelle station
        var nom_ = station.nom.replace("'","''")

        val strSql = "insert into Station (idStation,latitude,longitude,nom,nb_place_tot,nb_place_dispo) values (" +
                    " ${station.numero} ," +
                    " ${station.latitude} ," +
                    " ${station.longitude} ," +
                    " '$nom_' ," +
                    " ${station.nb_place_tot} ," +
                    " ${station.nb_place_dispo} )"
        this.writableDatabase.execSQL(strSql)
    }

    fun update_station(nb_place_dispo : Int, numero : Int) {  // Modification du nombre de place disponible d'une station retrouvé selon sa latitude et sa longitude
        val strSql = "Update Station set " +
                "nb_place_dispo = $nb_place_dispo " +
                "where idStation = $numero "
        this.writableDatabase.execSQL(strSql)
    }

    fun delete_stations() { // Supprimer toutes les stations
        val strSql = "delete from Station"
        this.writableDatabase.execSQL(strSql)
    }

    fun genrerate_stations(): MutableList<Station>? { // Récupération de toutes les stations
        val cursor = this.writableDatabase.rawQuery("select * from Station", null)
        cursor.moveToFirst()
        if (!cursor.isAfterLast){
            var stations = MutableList<Station>(1){Station(cursor.getInt(0),cursor.getDouble(1),cursor.getDouble(2),cursor.getString(3),cursor.getInt(4),cursor.getInt(5)) }
            while (!cursor.isAfterLast) {
                stations.add(Station(cursor.getInt(0),cursor.getDouble(1),cursor.getDouble(2),cursor.getString(3),cursor.getInt(4),cursor.getInt(5)))
                cursor.moveToNext()
            }
            return stations
        }else{
            return null
        }
    }
}
