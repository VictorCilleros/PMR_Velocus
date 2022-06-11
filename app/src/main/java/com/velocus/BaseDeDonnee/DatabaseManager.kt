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
class DatabaseManager(context: Context?) : SQLiteOpenHelper(context, "Stations.db", null, 1){

    override fun onCreate(db: SQLiteDatabase) {
        var strSql = "create table Station (" +
                " idStation integer primary key autoincrement," +
                " latitude real not null," +
                " longitude real not null," +
                " nom text not null," +
                " nb_place_tot integer not null," +
                " nb_place_dispo integer not null" +
                ")"
        db.execSQL(strSql)
        Log.i("DATABASE", "oncreat")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        var strSql = "drop table if exists Station"
        db.execSQL(strSql)
        this.onCreate(db)
    }

    fun nb_stations(): Int{
        val cur = this.writableDatabase.rawQuery("Select COUNT(*) FROM Station", null)
        cur.moveToFirst()
        return cur.getInt(0)
    }

    fun insert_station(latitude : Double, longitude : Double, nom : String, nb_place_tot : Int, nb_place_dispo : Int) {
        val strSql = "insert into Station (latitude,longitude,nom,nb_place_tot,nb_place_dispo) values (" +
                    " $latitude ," +
                    " $longitude ," +
                    " $nom ," +
                    " $nb_place_tot ," +
                    " $nb_place_dispo )"
        this.writableDatabase.execSQL(strSql)
    }

    fun update_station(nb_place_dispo : Int, latitude : Double, longitude : Double) {
        val strSql = "Update Station set " +
                "nb_place_dispo = $nb_place_dispo " +
                "where latitude = $latitude and longitude = $longitude"
        this.writableDatabase.execSQL(strSql)
    }

    fun delete_stations() {
        val strSql = "delete from Station"
        this.writableDatabase.execSQL(strSql)
    }

    fun genrerate_stations(): MutableList<Station>? {
        val cursor = this.writableDatabase.rawQuery("select * from Station", null)
        cursor.moveToFirst()
        if (!cursor.isAfterLast){
            var stations = MutableList<Station>(1){Station(cursor.getDouble(1),cursor.getDouble(2),cursor.getString(3),cursor.getInt(4),cursor.getInt(5)) }
            while (!cursor.isAfterLast) {
                stations.add(Station(cursor.getDouble(1),cursor.getDouble(2),cursor.getString(3),cursor.getInt(4),cursor.getInt(5)))
                cursor.moveToNext()
            }
            return stations
        }else{
            return null
        }
    }
}
