package com.example.leanmasscalculator.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.leanmasscalculator.model.CalculationResult

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "lean_mass.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE = "calculations"
        private const val COL_ID = "_id"
        private const val COL_USER_ID = "user_id"
        private const val COL_WEIGHT = "weight"
        private const val COL_HEIGHT = "height"
        private const val COL_SEX = "sex"
        private const val COL_LBM = "lbm"
        private const val COL_DATE = "date"
        private const val COL_IS_SATISFACTORY = "is_satisfactory"
        private const val COL_FIRESTORE_ID = "firestore_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID TEXT NOT NULL,
                $COL_WEIGHT REAL NOT NULL,
                $COL_HEIGHT REAL NOT NULL,
                $COL_SEX TEXT NOT NULL,
                $COL_LBM REAL NOT NULL,
                $COL_DATE INTEGER NOT NULL,
                $COL_IS_SATISFACTORY INTEGER NOT NULL,
                $COL_FIRESTORE_ID TEXT DEFAULT ''
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(db)
    }

    fun insert(result: CalculationResult): Long {
        val values = ContentValues().apply {
            put(COL_USER_ID, result.userId)
            put(COL_WEIGHT, result.weight)
            put(COL_HEIGHT, result.height)
            put(COL_SEX, result.sex)
            put(COL_LBM, result.lbm)
            put(COL_DATE, result.date)
            put(COL_IS_SATISFACTORY, if (result.isSatisfactory) 1 else 0)
            put(COL_FIRESTORE_ID, result.firestoreId)
        }
        return writableDatabase.insert(TABLE, null, values)
    }

    fun updateFirestoreId(localId: Long, firestoreId: String) {
        val values = ContentValues().apply { put(COL_FIRESTORE_ID, firestoreId) }
        writableDatabase.update(TABLE, values, "$COL_ID = ?", arrayOf(localId.toString()))
    }

    fun getByUser(userId: String): List<CalculationResult> {
        val list = mutableListOf<CalculationResult>()
        val cursor = readableDatabase.query(
            TABLE, null,
            "$COL_USER_ID = ?", arrayOf(userId),
            null, null, "$COL_DATE DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    CalculationResult(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                        firestoreId = it.getString(it.getColumnIndexOrThrow(COL_FIRESTORE_ID)) ?: "",
                        userId = it.getString(it.getColumnIndexOrThrow(COL_USER_ID)),
                        weight = it.getDouble(it.getColumnIndexOrThrow(COL_WEIGHT)),
                        height = it.getDouble(it.getColumnIndexOrThrow(COL_HEIGHT)),
                        sex = it.getString(it.getColumnIndexOrThrow(COL_SEX)),
                        lbm = it.getDouble(it.getColumnIndexOrThrow(COL_LBM)),
                        date = it.getLong(it.getColumnIndexOrThrow(COL_DATE)),
                        isSatisfactory = it.getInt(it.getColumnIndexOrThrow(COL_IS_SATISFACTORY)) == 1
                    )
                )
            }
        }
        return list
    }

    fun delete(id: Long): Int =
        writableDatabase.delete(TABLE, "$COL_ID = ?", arrayOf(id.toString()))
}
