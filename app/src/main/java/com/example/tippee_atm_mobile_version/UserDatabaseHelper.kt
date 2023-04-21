package com.example.tippee_atm_mobile_version

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, UserDatabaseHelper.DATABASE_NAME, null, UserDatabaseHelper.DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "minutiae_database"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "user"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_LAST_TRANSACTION = "lasttransaction"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE ${UserDatabaseHelper.TABLE_NAME} (" +
                    "${UserDatabaseHelper.COLUMN_ID} INTEGER," +
                    "${UserDatabaseHelper.COLUMN_NAME} TEXT," +
                    "${UserDatabaseHelper.COLUMN_AMOUNT} INTEGER," +
                    "${UserDatabaseHelper.COLUMN_LAST_TRANSACTION} INTEGER)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${UserDatabaseHelper.TABLE_NAME}")
        onCreate(db)
    }

    fun insertMinutiaeData(id: Int, name: String, amount: Int, lasttransaction: Int,): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put(UserDatabaseHelper.COLUMN_ID, id)
        values.put(UserDatabaseHelper.COLUMN_NAME, name)
        values.put(UserDatabaseHelper.COLUMN_AMOUNT, amount)
        values.put(UserDatabaseHelper.COLUMN_LAST_TRANSACTION, lasttransaction)
        return db.insert(UserDatabaseHelper.TABLE_NAME, null, values)
    }
}