package com.example.weather_app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.Exception

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    companion object{

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "weather.db"
        private const val TBL_WEATHER = "tbl_weather"
        private const val ID = "id"
        private const val CITY = "city"
        private const val COUNTRY = "country"
        private const val TEMP = "currTemp"

    }
    override fun onCreate(db: SQLiteDatabase?) {

        val createTblWeather = ("CREATE TABLE " + TBL_WEATHER + "("
                + ID + " INTEGER PRIMARY KEY," + CITY + " TEXT,"
                + COUNTRY + " TEXT," + TEMP + " TEXT" + ")")

        db?.execSQL(createTblWeather)




    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

        db?.execSQL("DROP TABLE IF EXISTS $TBL_WEATHER")
        onCreate(db)

    }

    fun insertWeather(std: WeatherModel): Long{
         val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID,std.id)
        contentValues.put(CITY,std.city)
        contentValues.put(COUNTRY,std.country)
        contentValues.put(TEMP,std.currTemp)

        val success = db.insert(TBL_WEATHER,null,contentValues)
        db.close()
        return success

    }

    @SuppressLint("Range")
    fun getAllWeather():ArrayList<WeatherModel>{

        val stdList: ArrayList<WeatherModel> = ArrayList()
        val query = "SELECT * FROM $TBL_WEATHER"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {

            cursor = db.rawQuery(query, null)




        }catch (e: Exception){
            e.printStackTrace()
            db.execSQL(query)
            return ArrayList()
        }

        var id: Int
        var city: String
        var country: String
        var currTemp: String

        if(cursor.moveToFirst()){
            do {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                city = cursor.getString(cursor.getColumnIndex("city"))
                country = cursor.getString(cursor.getColumnIndex("country"))
                currTemp = cursor.getString(cursor.getColumnIndex("currTemp"))

                val std = WeatherModel(id=id, city=city, country=country, currTemp=currTemp)
                stdList.add(std)

            }while (cursor.moveToNext())
        }



         return stdList

    }

    fun updateWeather(std: WeatherModel): Int{

        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(ID,std.id)
        contentValues.put(CITY,std.city)
        contentValues.put(COUNTRY,std.country)
        contentValues.put(TEMP,std.currTemp)

        val whereClause = "$CITY = ?"

        // Define the values for the WHERE clause
        val whereArgs = arrayOf(std.city)

        val success = db.update(TBL_WEATHER, contentValues, whereClause, whereArgs)

        db.close()
        return success

    }

    fun deleteWeather(): Int{



        val db = this.writableDatabase
        val tableName = TBL_WEATHER

        // Use the delete method to delete all rows from the table
        val success = db.delete(tableName, null, null)

        db.close()
        return success



    }

    fun deleteWeatherByCity(city: String): Int{

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(CITY,city)

        val whereClause = "$CITY = ?"

        // Define the values for the WHERE clause
        val whereArgs = arrayOf(city)

        val success = db.delete(TBL_WEATHER, whereClause, whereArgs)
        db.close()
        return success
    }


}