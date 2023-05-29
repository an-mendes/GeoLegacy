package com.example.geolegacy
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MapDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "map_database"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create your database tables here
        val createLocationsTableQuery = "CREATE TABLE locations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "latitude REAL," +
                "longitude REAL," +
                "name TEXT)"
        db.execSQL(createLocationsTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database schema upgrades here if needed
    }
}
