package com.varunkumar.myapplication.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.varunkumar.myapplication.data.BiometricSample
import com.varunkumar.myapplication.data.DatabaseHelper

class TouchViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {
    val collectedData = mutableStateListOf<BiometricSample>()

    // Sensor state
    var accelX by mutableFloatStateOf(0f)
        private set
    var accelY by mutableFloatStateOf(0f)
        private set
    var accelZ by mutableFloatStateOf(0f)
        private set

    // Tapping Task State
    var targetPosition by mutableStateOf(Offset(0.5f, 0.5f))
        private set

    fun updateAccelerometer(x: Float, y: Float, z: Float) {
        accelX = x
        accelY = y
        accelZ = z
    }

    fun addSample(sample: BiometricSample) {
        collectedData.add(sample)
        dbHelper.insertSample(sample)
    }

    fun updateTargetPosition(newPosition: Offset) {
        targetPosition = newPosition
    }

    fun clearCollectedData() {
        collectedData.clear()
    }
}
