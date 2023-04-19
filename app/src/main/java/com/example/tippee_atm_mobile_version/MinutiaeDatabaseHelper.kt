package com.example.tippee_atm_mobile_version

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MinutiaeDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "minutiae_database"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "minutiae"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_MINUTIAE_DATA = "minutiae_data"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COLUMN_NAME TEXT," +
                    "$COLUMN_MINUTIAE_DATA TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertMinutiaeData(name: String, minutiaeData: String): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_MINUTIAE_DATA, minutiaeData)
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllMinutiaeData(): List<Pair<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        val minutiaeDataList = mutableListOf<Pair<String, String>>()
        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val minutiaeData = getString(getColumnIndexOrThrow(COLUMN_MINUTIAE_DATA))
                minutiaeDataList.add(Pair(name, minutiaeData))
            }
        }
        cursor.close()
        return minutiaeDataList
    }
}