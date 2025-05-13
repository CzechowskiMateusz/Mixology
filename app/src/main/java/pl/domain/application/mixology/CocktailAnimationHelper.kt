package pl.domain.application.mixology.animations

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.graphics.Color
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.animation.doOnEnd
import android.widget.RelativeLayout

fun animateShake(view: View) {
    val animatorX = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
    animatorX.duration = 800
    animatorX.interpolator = AccelerateDecelerateInterpolator()

    val animatorY = ObjectAnimator.ofFloat(view, "rotation", 0f, 3f, -3f, 2f, -2f, 0f)
    animatorY.duration = 800
    animatorY.interpolator = AccelerateDecelerateInterpolator()

    AnimatorSet().apply {
        playTogether(animatorX, animatorY)
        start()
    }
}

fun registerShakeSensor(context: Context, onShake: () -> Unit): SensorEventListener {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val sensorListener = object : SensorEventListener {
        private var shakeThreshold = 12f

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                if (acceleration > shakeThreshold) {
                    onShake()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    return sensorListener
}

fun unregisterShakeSensor(context: Context, listener: SensorEventListener) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    sensorManager.unregisterListener(listener)
}

fun showConfetti(context: Context) {
    val confetti = ImageView(context).apply {
        setImageResource(android.R.drawable.star_big_on)  // Możesz użyć własnego obrazka konfetti
        setColorFilter(Color.YELLOW)
    }

    val animatorX = ObjectAnimator.ofFloat(confetti, "translationX", -1000f, 1000f)
    val animatorY = ObjectAnimator.ofFloat(confetti, "translationY", -1000f, 1000f)
    val rotation = ObjectAnimator.ofFloat(confetti, "rotation", 0f, 360f)

    animatorX.duration = 2000
    animatorY.duration = 2000
    rotation.duration = 2000

    val set = AnimatorSet().apply {
        playTogether(animatorX, animatorY, rotation)
    }

    set.interpolator = AccelerateDecelerateInterpolator()

    set.start()

    val layout = (context as Activity).findViewById<RelativeLayout>(android.R.id.content)
    layout.addView(confetti)

    set.doOnEnd {
        layout.removeView(confetti)
    }
}
