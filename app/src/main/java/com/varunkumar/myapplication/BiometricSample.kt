package com.varunkumar.myapplication

data class BiometricSample(
    val timestamp: Long,
    val touchX: Float?,
    val touchY: Float?,
    val pressure: Float?,
    val keyInterval: Long?,
    val dwellTime: Long? = null,
    val touchSize: Float? = null,
    val accelX: Float? = null,
    val accelY: Float? = null,
    val accelZ: Float? = null
)
