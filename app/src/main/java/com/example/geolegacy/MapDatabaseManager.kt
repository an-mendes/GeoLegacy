package com.example.geolegacy
import android.content.ContentValues
import android.content.Context
import android.database.Cursor

object MapDatabaseManager {
    private lateinit var dbHelper: MapDatabaseHelper

    fun initialize(context: Context) {
        dbHelper = MapDatabaseHelper(context)
    }

    fun insertLocation(latitude: Double, longitude: Double, name: String): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("latitude", latitude)
            put("longitude", longitude)
            put("name", name)
        }

        return db.insert("locations", null, values)
    }

    fun getAllLocations(): List<Location> {
        val db = dbHelper.readableDatabase

        val projection = arrayOf("id", "latitude", "longitude", "name")

        val cursor: Cursor = db.query(
            "locations",
            projection,
            null,
            null,
            null,
            null,
            null
        )

        val locations = mutableListOf<Location>()

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val latitude = getDouble(getColumnIndexOrThrow("latitude"))
                val longitude = getDouble(getColumnIndexOrThrow("longitude"))
                val name = getString(getColumnIndexOrThrow("name"))

                locations.add(Location(id, latitude, longitude, name))
            }
        }

        cursor.close()

        return locations
    }
}
