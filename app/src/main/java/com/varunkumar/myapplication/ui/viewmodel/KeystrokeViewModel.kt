package com.varunkumar.myapplication.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.varunkumar.myapplication.data.BiometricSample
import com.varunkumar.myapplication.data.DatabaseHelper

class KeystrokeViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {
    var text by mutableStateOf("")
        private set

    var savedCount by mutableIntStateOf(0)
        private set

    private var lastTime by mutableLongStateOf(0L)

    // Sensor state
    private var accelX = 0f
    private var accelY = 0f
    private var accelZ = 0f

    fun updateAccelerometer(x: Float, y: Float, z: Float) {
        accelX = x
        accelY = y
        accelZ = z
    }

    fun onTextChanged(newText: String) {
        val currentTime = System.currentTimeMillis()

        if (lastTime != 0L) {
            val interval = currentTime - lastTime

            val sample = BiometricSample(
                timestamp = currentTime,
                touchX = null,
                touchY = null,
                pressure = null,
                keyInterval = interval,
                accelX = accelX,
                accelY = accelY,
                accelZ = accelZ
            )

            dbHelper.insertSample(sample)
            savedCount++
        }

        lastTime = currentTime
        text = newText
    }

    fun retryTask() {
        text = ""
        savedCount = 0
        lastTime = 0L
    }
}
