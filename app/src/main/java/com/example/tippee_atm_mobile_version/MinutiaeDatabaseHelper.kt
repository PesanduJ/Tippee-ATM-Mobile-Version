package com.example.tippee_atm_mobile_version

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import org.opencv.core.Point

class MinutiaeDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "minutiae_database"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "minutiae"
        const val COLUMN_ID = "id"
        const val COLUMN_ACCOUNT = "accountno"
        const val COLUMN_NAME = "name"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_LAST_TRANSACTION = "lasttransaction"
        const val COLUMN_MINUTIAE_DATA = "minutiae_data"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COLUMN_ACCOUNT INTEGER," +
                    "$COLUMN_NAME TEXT," +
                    "$COLUMN_AMOUNT INTEGER," +
                    "$COLUMN_LAST_TRANSACTION INTEGER," +
                    "$COLUMN_MINUTIAE_DATA TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertMinutiaeData(id: String, name: String, amount: Int, lastTransaction: Int, minutiaeData: String): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ACCOUNT, id)
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_AMOUNT, amount)
        values.put(COLUMN_LAST_TRANSACTION, lastTransaction)
        values.put(COLUMN_MINUTIAE_DATA, minutiaeData)
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllMinutiaeData(): List<Pair<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        val minutiaeDataList = mutableListOf<Pair<String, String>>()
        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_ACCOUNT))
                val minutiaeData = getString(getColumnIndexOrThrow(COLUMN_MINUTIAE_DATA))
                minutiaeDataList.add(Pair(name, minutiaeData))
            }
        }
        cursor.close()
        return minutiaeDataList
    }
    fun compareFingerprints(minutiaeData: List<Point>): Pair<String, Double>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        var bestMatch: Pair<String, Double>? = null
        val jaccardSimilarity = JaccardSimilarity()

        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_ACCOUNT))
                val dbMinutiaeData = getString(getColumnIndexOrThrow(COLUMN_MINUTIAE_DATA))

                val dbPoints = parseMinutiaeData(dbMinutiaeData)
                val similarity = jaccardSimilarity.index(minutiaeData, dbPoints)

                if (bestMatch == null || similarity > bestMatch!!.second) {
                    bestMatch = Pair(name, similarity)
                }
            }
        }
        cursor.close()

        return bestMatch
    }

    fun parseMinutiaeData(minutiaeDataString: String): List<Point> {
        val points = mutableListOf<Point>()
        val regex = """\{([0-9]+(\.[0-9]+)?),\s+([0-9]+(\.[0-9]+)?)\}""".toRegex()
        regex.findAll(minutiaeDataString).forEach {
            val (x, _, y, _) = it.destructured
            points.add(Point(x.toDouble(), y.toDouble()))
        }
        return points
    }

    class JaccardSimilarity {
        fun index(set1: List<Point>, set2: List<Point>): Double {
            val intersection = set1.intersect(set2).size.toDouble()
            val union = set1.union(set2).size.toDouble()
            return intersection / union
        }
    }

    fun getUserDataByAccountNo(accountNo: Int): Pair<String, Pair<Int, Int>>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_NAME, $COLUMN_AMOUNT, $COLUMN_LAST_TRANSACTION FROM $TABLE_NAME WHERE $COLUMN_ACCOUNT = ?", arrayOf(accountNo.toString()))

        var userData: Pair<String, Pair<Int, Int>>? = null

        with(cursor) {
            if (moveToFirst()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val amount = getInt(getColumnIndexOrThrow(COLUMN_AMOUNT))
                val lastTransaction = getInt(getColumnIndexOrThrow(COLUMN_LAST_TRANSACTION))
                userData = Pair(name, Pair(amount, lastTransaction))
            }
        }
        cursor.close()

        return userData
    }

    fun transferAmount(fromAccount: Int, toAccount: Int, amount: Int): Boolean {
        val db = writableDatabase
        val userData = getUserDataByAccountNo(fromAccount)
        if (userData == null || userData.second.first < amount) {
            // User account does not exist or insufficient balance
            return false
        }

        // Deduct amount from fromAccount
        val newAmount = userData.second.first - amount
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, newAmount)
            put(COLUMN_LAST_TRANSACTION, -amount)
        }
        val whereClause = "$COLUMN_ACCOUNT = ?"
        val whereArgs = arrayOf(fromAccount.toString())
        db.update(TABLE_NAME, values, whereClause, whereArgs)

        // Add amount to toAccount
        val toUserData = getUserDataByAccountNo(toAccount)
        if (toUserData == null) {
            // To account does not exist
            return false
        }
        val toNewAmount = toUserData.second.first + amount
        val toValues = ContentValues().apply {
            put(COLUMN_AMOUNT, toNewAmount)
        }
        val toWhereClause = "$COLUMN_ACCOUNT = ?"
        val toWhereArgs = arrayOf(toAccount.toString())
        db.update(TABLE_NAME, toValues, toWhereClause, toWhereArgs)

        return true
    }

    fun addAmountByAccountNo(accountNo: Int, amount: Int): Boolean {
        val db = writableDatabase
        val userData = getUserDataByAccountNo(accountNo)

        if (userData == null) {
            // User account does not exist
            return false
        }

        val newAmount = userData.second.first + amount
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, newAmount)
            put(COLUMN_LAST_TRANSACTION, amount)
        }

        val selection = "$COLUMN_ACCOUNT = ?"
        val selectionArgs = arrayOf(accountNo.toString())

        db.update(TABLE_NAME, values, selection, selectionArgs)

        return true
    }

    fun getAllUserDataByAccountNo(accountNo: Int): Triple<Int, String, Pair<Int, Int>>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_ACCOUNT, $COLUMN_NAME, $COLUMN_AMOUNT, $COLUMN_LAST_TRANSACTION FROM $TABLE_NAME WHERE $COLUMN_ACCOUNT = ?", arrayOf(accountNo.toString()))

        var userData: Triple<Int, String, Pair<Int, Int>>? = null

        with(cursor) {
            if (moveToFirst()) {
                val account = getInt(getColumnIndexOrThrow(COLUMN_ACCOUNT))
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val amount = getInt(getColumnIndexOrThrow(COLUMN_AMOUNT))
                val lastTransaction = getInt(getColumnIndexOrThrow(COLUMN_LAST_TRANSACTION))
                userData = Triple(account, name, Pair(amount, lastTransaction))
            }
        }
        cursor.close()

        return userData
    }

    fun deductAmountByAccountNo(accountNo: Int, amount: Int): Boolean {
        val db = writableDatabase
        val userData = getUserDataByAccountNo(accountNo)

        if (userData == null) {
            // User account does not exist
            return false
        }

        val currentAmount = userData.second.first
        if (currentAmount < amount) {
            // User does not have enough funds to deduct the amount
            return false
        }

        val newAmount = currentAmount - amount
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, newAmount)
            put(COLUMN_LAST_TRANSACTION, -amount) // Negative amount to represent a deduction
        }

        val selection = "$COLUMN_ACCOUNT = ?"
        val selectionArgs = arrayOf(accountNo.toString())

        db.update(TABLE_NAME, values, selection, selectionArgs)

        return true
    }
}