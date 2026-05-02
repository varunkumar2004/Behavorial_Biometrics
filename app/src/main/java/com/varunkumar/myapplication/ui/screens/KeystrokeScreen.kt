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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.varunkumar.myapplication.data.DatabaseHelper
import com.varunkumar.myapplication.ui.viewmodel.KeystrokeViewModel
import com.varunkumar.myapplication.ui.viewmodel.ViewModelFactory

@Composable
fun KeystrokeScreen(
    modifier: Modifier = Modifier,
    dbHelper: DatabaseHelper,
    viewModel: KeystrokeViewModel = viewModel(factory = ViewModelFactory(dbHelper)),
    onNext: () -> Unit,
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = if (sensorManager != null && accelerometer != null) {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        viewModel.updateAccelerometer(it.values[0], it.values[1], it.values[2])
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
            value = viewModel.text,
            onValueChange = { viewModel.onTextChanged(it) },
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
                onClick = { viewModel.retryTask() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Text("Retry Task")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = viewModel.text.length > 5 // Allow next after some typing
            ) {
                Text("Finish Enrollment")
            }
        }
    }
}
