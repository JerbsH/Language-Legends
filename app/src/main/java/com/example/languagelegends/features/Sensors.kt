package com.example.languagelegends.features

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import kotlin.math.sqrt

class SensorHelper(private val context: Context) : SensorEventListener {
    // Define the shake threshold
    private val SHAKE_THRESHOLD = 800

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private var accelerometer: Sensor? = null
    private var isShaking = mutableStateOf(false)
    private var isTiltedLeft = mutableStateOf(false)
    private var isTiltedRight = mutableStateOf(false)
    private var sensorValues: FloatArray = FloatArray(3)

    // Shake listener callback
    private var shakeListener: (() -> Unit)? = null

    init {
        // Initialize the accelerometer sensor
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // Register sensor listener
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                sensorValues = it.values.clone()
                val acceleration = calculateAccelerationMagnitude(it.values)
                isShaking.value = isDeviceShaking(acceleration)
                isTiltedLeft.value = isDeviceTiltedLeft(it.values)
                isTiltedRight.value = isDeviceTiltedRight(it.values)

                // Calculate the acceleration magnitude
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                val accelerationMagnitude = sqrt(x * x + y * y + z * z)

                // Check if the device is shaken
                if (accelerationMagnitude > SHAKE_THRESHOLD) {
                    shakeListener?.invoke()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing for now
    }


    private fun calculateAccelerationMagnitude(values: FloatArray): Float {
        return sqrt(values.map { it * it }.sum())
    }

    private fun isDeviceShaking(acceleration: Float): Boolean {
        // Define a threshold for shaking detection
        val threshold = 10f
        return acceleration > threshold
    }

    // Register a listener to be called when the device is shaken
    fun setShakeListener(listener: () -> Unit) {
        shakeListener = listener
    }


    fun isDeviceTiltedLeft(values: FloatArray): Boolean {
        // Tilt left detection logic with a smaller threshold
        return values[0] < -2.0f // Adjust threshold value to make it less sensitive
    }

    fun isDeviceTiltedRight(values: FloatArray): Boolean {
        // Tilt right detection logic with a smaller threshold
        return values[0] > 2.0f // Adjust threshold value to make it less sensitive
    }
    fun getCurrentSensorValues(): FloatArray {
        return sensorValues
    }

    /*fun resetTiltDetection() {
        isTiltedLeft.value = false
        isTiltedRight.value = false
    }*/



    fun unregisterSensorListener() {
        accelerometer?.let { sensorManager.unregisterListener(this, it) }
    }
}
