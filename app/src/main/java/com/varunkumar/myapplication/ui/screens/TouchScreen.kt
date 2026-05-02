package com.varunkumar.myapplication.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.varunkumar.myapplication.data.BiometricSample
import com.varunkumar.myapplication.data.DatabaseHelper
import com.varunkumar.myapplication.ui.viewmodel.TouchViewModel
import com.varunkumar.myapplication.ui.viewmodel.ViewModelFactory
import kotlin.random.Random

enum class TouchTaskMode { TRACING, TAPPING }
enum class TracingPattern { S_CURVE, CIRCLE }

@Composable
fun TouchScreen(
    modifier: Modifier = Modifier,
    dbHelper: DatabaseHelper,
    mode: TouchTaskMode,
    pattern: TracingPattern,
    viewModel: TouchViewModel = viewModel(factory = ViewModelFactory(dbHelper)),
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TouchPointComponent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            touchSamples = viewModel.collectedData.filter { it.touchX != null && it.touchY != null },
            accelData = Triple(viewModel.accelX, viewModel.accelY, viewModel.accelZ),
            mode = mode,
            pattern = pattern,
            targetPosition = viewModel.targetPosition,
            onTargetHit = {
                viewModel.updateTargetPosition(
                    Offset(
                        Random.nextFloat().coerceIn(0.1f, 0.9f),
                        Random.nextFloat().coerceIn(0.1f, 0.9f)
                    )
                )
            }
        ) { sample: BiometricSample ->
            viewModel.addSample(sample)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.clearCollectedData()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Text("Retry Task")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = viewModel.collectedData.isNotEmpty()
            ) {
                Text("Next Step")
            }
        }
    }
}

@Composable
fun TouchPointComponent(
    modifier: Modifier = Modifier,
    touchSamples: List<BiometricSample>,
    accelData: Triple<Float, Float, Float>,
    mode: TouchTaskMode,
    pattern: TracingPattern,
    targetPosition: Offset,
    onTargetHit: () -> Unit,
    addCollectedData: (BiometricSample) -> Unit
) {
    var lastSample by remember { mutableStateOf<BiometricSample?>(null) }
    var startTime by remember { mutableLongStateOf(0L) }
    
    val tapRadius = 40.dp

    val (targetColor, heatMapColor) = when {
        mode == TouchTaskMode.TAPPING -> Color(0xFFFF9800) to Color(0xFFFFEB3B)
        pattern == TracingPattern.S_CURVE -> Color(0xFF2196F3) to Color(0xFF00BCD4)
        else -> Color(0xFF4CAF50) to Color(0xFF8BC34A)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = targetColor.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (mode == TouchTaskMode.TRACING) "Task: Trace the Path" else "Task: Tap the Target",
                    style = MaterialTheme.typography.labelLarge,
                    color = targetColor
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "Dwell: ${lastSample?.dwellTime ?: 0}ms", style = MaterialTheme.typography.labelMedium)
                    Text(text = "Pressure: ${lastSample?.pressure?.let { "%.2f".format(it) } ?: "0.00"}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(color = Color(0xFFF0F0F0))
                .pointerInput(mode, pattern) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.first()

                            if (change.changedToDown()) {
                                startTime = System.currentTimeMillis()
                                
                                // In Tapping mode, check if target was hit
                                if (mode == TouchTaskMode.TAPPING) {
                                    val px = targetPosition.x * size.width
                                    val py = targetPosition.y * size.height
                                    val dist = Offset(change.position.x - px, change.position.y - py).getDistance()
                                    
                                    if (dist < tapRadius.toPx() * 2) {
                                        onTargetHit()
                                    }
                                }
                            }

                            val currentTime = System.currentTimeMillis()
                            val dwell = if (startTime != 0L) currentTime - startTime else 0L

                            val sample = BiometricSample(
                                timestamp = currentTime,
                                touchX = change.position.x,
                                touchY = change.position.y,
                                pressure = change.pressure,
                                keyInterval = null,
                                dwellTime = dwell,
                                accelX = accelData.first,
                                accelY = accelData.second,
                                accelZ = accelData.third
                            )
                            
                            if (mode == TouchTaskMode.TRACING) {
                                if (change.pressed) {
                                    addCollectedData(sample)
                                    lastSample = sample
                                }
                            } else {
                                // Tapping mode
                                if (change.pressed) {
                                    lastSample = sample
                                } else if (change.changedToUp()) {
                                    addCollectedData(sample)
                                    lastSample = sample
                                }
                            }

                            if (change.changedToUp()) {
                                startTime = 0L
                            }
                            
                            if (change.pressed || change.changedToUp()) {
                                change.consume()
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                if (mode == TouchTaskMode.TRACING) {
                    val targetPath = when (pattern) {
                        TracingPattern.S_CURVE -> Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.2f)
                            quadraticTo(size.width * 0.8f, size.height * 0.5f, size.width * 0.2f, size.height * 0.8f)
                        }
                        TracingPattern.CIRCLE -> Path().apply {
                            addOval(Rect(center, size.minDimension * 0.35f))
                        }
                    }
                    
                    drawPath(
                        path = targetPath,
                        color = targetColor.copy(alpha = 0.2f),
                        style = Stroke(width = 50f)
                    )
                } else {
                    // Tapping Mode Target
                    drawCircle(
                        color = targetColor.copy(alpha = 0.4f),
                        radius = tapRadius.toPx(),
                        center = Offset(targetPosition.x * size.width, targetPosition.y * size.height)
                    )
                    drawCircle(
                        color = targetColor,
                        radius = 10f,
                        center = Offset(targetPosition.x * size.width, targetPosition.y * size.height)
                    )
                }

                // Heat map for current session
                touchSamples.forEach { sample ->
                    val offset = Offset(sample.touchX ?: 0f, sample.touchY ?: 0f)
                    val pressure = sample.pressure ?: 0.5f
                    val radius = 25f + (pressure * 25f)
                    
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(heatMapColor.copy(alpha = 0.2f), Color.Transparent),
                            center = offset,
                            radius = radius
                        ),
                        radius = radius,
                        center = offset
                    )
                }
            }
            
            Text(
                text = if (mode == TouchTaskMode.TRACING) "Trace carefully" else "Tap the target",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                color = Color.Gray
            )
        }
    }
}
