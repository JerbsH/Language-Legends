package com.example.languagelegends.features

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableStateOf
import kotlin.math.sqrt

/**
 * This class helps in handling sensor events, specifically for accelerometer sensor.
 * It provides functionalities to detect device tilt and shake.
 */
class SensorHelper(private val context: Context) : SensorEventListener {
    private val shakeTreshold = 15.0f

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    // Define a variable to hold the accelerometer sensor
    private var accelerometer: Sensor? = null
    // Define states for tilt detection
    var isTiltedLeft = mutableStateOf(false)
    var isTiltedRight = mutableStateOf(false)

    // Define a listener for shake events
    private var shakeListener: (() -> Unit)? = null

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val values = it.values
                isTiltedLeft.value = isDeviceTiltedLeft(values)
                isTiltedRight.value = isDeviceTiltedRight(values)

                // Calculate the acceleration
                val x = values[0]
                val y = values[1]
                val z = values[2]
                val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH


                if (acceleration > shakeTreshold) {
                    shakeListener?.invoke()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing for now
    }

    // Check if the device is tilted to the left
    private fun isDeviceTiltedLeft(values: FloatArray): Boolean {
        return values[0] < -5.0f
    }

    // Check if the device is tilted to the right
    private fun isDeviceTiltedRight(values: FloatArray): Boolean {
        return values[0] > 5.0f
    }

    // Set the shake listener
    fun setShakeListener(listener: () -> Unit) {
        shakeListener = listener
    }

    // Unregister the sensor listener
    fun unregisterSensorListener() {
        accelerometer?.let { sensorManager.unregisterListener(this, it) }
    }
}