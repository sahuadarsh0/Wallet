package com.technitedminds.wallet.presentation.screens.carddetail

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Roll/pitch in degrees, derived from ROTATION_VECTOR.
 *  - rollDeg > 0  → device tilted right
 *  - pitchDeg > 0 → device tipped toward user
 *
 * Baseline is captured on first sample so the "neutral" pose is wherever the
 * user is currently holding the phone — no sudden swing on enter.
 *
 * Output is clamped to ±[maxDeg] and lightly low-passed.
 */
@Composable
fun rememberDeviceTilt(maxDeg: Float = 14f): State<Pair<Float, Float>> {
    val context = LocalContext.current
    val tilt = remember { mutableStateOf(0f to 0f) }

    DisposableEffect(context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sm == null || sensor == null) {
            return@DisposableEffect onDispose { }
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display = wm?.defaultDisplay

        var baselineRoll = Float.NaN
        var baselinePitch = Float.NaN

        // One-pole low-pass; alpha small = smoother, larger = snappier.
        val alpha = 0.20f
        var smRoll = 0f
        var smPitch = 0f

        val listener = object : SensorEventListener {
            private val rotMatrix = FloatArray(9)
            private val remapped = FloatArray(9)
            private val orient = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
                SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)

                val rotation = display?.rotation ?: Surface.ROTATION_0
                val (axisX, axisY) = when (rotation) {
                    Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
                }
                SensorManager.remapCoordinateSystem(rotMatrix, axisX, axisY, remapped)
                SensorManager.getOrientation(remapped, orient)

                // orient[1] = pitch (rad), orient[2] = roll (rad). Convert to deg.
                val rollDegRaw = Math.toDegrees(orient[2].toDouble()).toFloat()
                val pitchDegRaw = Math.toDegrees(orient[1].toDouble()).toFloat()

                if (baselineRoll.isNaN()) {
                    baselineRoll = rollDegRaw
                    baselinePitch = pitchDegRaw
                }

                val rRel = (rollDegRaw - baselineRoll).coerceIn(-maxDeg, maxDeg)
                val pRel = (pitchDegRaw - baselinePitch).coerceIn(-maxDeg, maxDeg)

                smRoll = smRoll + alpha * (rRel - smRoll)
                smPitch = smPitch + alpha * (pRel - smPitch)

                tilt.value = smRoll to smPitch
            }

            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
        }

        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sm.unregisterListener(listener) }
    }
    return tilt
}
