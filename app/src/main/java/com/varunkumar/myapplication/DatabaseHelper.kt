package com.varunkumar.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "biometrics.db"
        private const val DATABASE_VERSION = 2 // Incremented version
        const val TABLE_NAME = "samples"
        const val COLUMN_ID = "id"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_TOUCH_X = "touch_x"
        const val COLUMN_TOUCH_Y = "touch_y"
        const val COLUMN_PRESSURE = "pressure"
        const val COLUMN_KEY_INTERVAL = "key_interval"
        const val COLUMN_DWELL_TIME = "dwell_time"
        const val COLUMN_TOUCH_SIZE = "touch_size"
        const val COLUMN_ACCEL_X = "accel_x"
        const val COLUMN_ACCEL_Y = "accel_y"
        const val COLUMN_ACCEL_Z = "accel_z"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_TIMESTAMP INTEGER, " +
                "$COLUMN_TOUCH_X REAL, " +
                "$COLUMN_TOUCH_Y REAL, " +
                "$COLUMN_PRESSURE REAL, " +
                "$COLUMN_KEY_INTERVAL INTEGER, " +
                "$COLUMN_DWELL_TIME INTEGER, " +
                "$COLUMN_TOUCH_SIZE REAL, " +
                "$COLUMN_ACCEL_X REAL, " +
                "$COLUMN_ACCEL_Y REAL, " +
                "$COLUMN_ACCEL_Z REAL)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertSample(sample: BiometricSample) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIMESTAMP, sample.timestamp)
            put(COLUMN_TOUCH_X, sample.touchX)
            put(COLUMN_TOUCH_Y, sample.touchY)
            put(COLUMN_PRESSURE, sample.pressure)
            put(COLUMN_KEY_INTERVAL, sample.keyInterval)
            put(COLUMN_DWELL_TIME, sample.dwellTime)
            put(COLUMN_TOUCH_SIZE, sample.touchSize)
            put(COLUMN_ACCEL_X, sample.accelX)
            put(COLUMN_ACCEL_Y, sample.accelY)
            put(COLUMN_ACCEL_Z, sample.accelZ)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllSamples(): List<BiometricSample> {
        val samples = mutableListOf<BiometricSample>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val sample = BiometricSample(
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    touchX = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_TOUCH_X))) null else cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOUCH_X)),
                    touchY = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_TOUCH_Y))) null else cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOUCH_Y)),
                    pressure = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PRESSURE))) null else cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_PRESSURE)),
                    keyInterval = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_KEY_INTERVAL))) null else cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_KEY_INTERVAL)),
                    dwellTime = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_DWELL_TIME))) null else cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DWELL_TIME)),
                    touchSize = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_TOUCH_SIZE))) null else cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOUCH_SIZE)),
                    accelX = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_ACCEL_X))) null else cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ACCEL_X)),
                    accelY = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_ACCEL_Y))) null else cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ACCEL_Y)),
                    accelZ = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_ACCEL_Z))) null else cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ACCEL_Z)),
                )
                samples.add(sample)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return samples
    }

    fun clearAllData() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }
}
