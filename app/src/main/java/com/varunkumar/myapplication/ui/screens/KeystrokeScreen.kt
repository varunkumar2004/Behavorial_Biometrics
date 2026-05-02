package com.varunkumar.myapplication.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.varunkumar.myapplication.data.BiometricSample
import com.varunkumar.myapplication.data.DatabaseHelper

@Composable
fun KeystrokeScreen(
    modifier: Modifier = Modifier,
    dbHelper: DatabaseHelper,
    onNext: () -> Unit,
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var lastTime by remember { mutableLongStateOf(0L) }
    var savedCount by remember { mutableIntStateOf(0) }

    // Sensor state
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = if (sensorManager != null && accelerometer != null) {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        accelX = it.values[0]
                        accelY = it.values[1]
                        accelZ = it.values[2]
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }.also {
                sensorManager.registerListener(it, accelerometer, SensorManager.SENSOR_DELAY_UI)
            }
        } else null
        
        onDispose {
            listener?.let { sensorManager?.unregisterListener(it) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Keystroke Profiling",
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Text(
            text = "Please type the sentence below to create your profile:\n\n'The quick brown fox jumps over the lazy dog'",
            modifier = Modifier.padding(bottom = 10.dp)
        )

        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
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
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Start typing here...") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    text = ""
                    savedCount = 0
                    lastTime = 0L
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Text("Retry Task")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = text.length > 5 // Allow next after some typing
            ) {
                Text("Finish Enrollment")
            }
        }
    }
}
