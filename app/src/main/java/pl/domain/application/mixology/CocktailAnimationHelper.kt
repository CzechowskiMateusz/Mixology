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
import android.view.ViewGroup
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

fun startConfettiAnimation(activity: Activity, parentView: ViewGroup) {
    val colors = listOf(
        Color.parseColor("#DDA0DD"), // Plum
        Color.parseColor("#DA70D6"), // Orchid
        Color.parseColor("#EE82EE"), // Violet
        Color.parseColor("#FF69B4"), // Hot Pink
        Color.parseColor("#FF1493"), // Deep Pink
        Color.parseColor("#BA55D3"), // Medium Orchid
        Color.parseColor("#C71585"), // Medium Violet Red
        Color.parseColor("#8A2BE2")  // Blue Violet
    )

    val confettiCount = 100

    repeat(confettiCount) {
        val confetti = View(activity).apply {
            setBackgroundColor(colors.random())
            layoutParams = RelativeLayout.LayoutParams(20, 20)
            x = (0..parentView.width).random().toFloat()
            y = -50f
        }

        parentView.addView(confetti)

        val fallDuration = (1000..2500).random().toLong()
        val fallAnimator = ObjectAnimator.ofFloat(confetti, "translationY", -50f, parentView.height + 100f).apply {
            duration = fallDuration
            interpolator = AccelerateDecelerateInterpolator()
        }

        val rotationAnimator = ObjectAnimator.ofFloat(confetti, "rotation", 0f, (360..1440).random().toFloat()).apply {
            duration = fallDuration
        }

        AnimatorSet().apply {
            playTogether(fallAnimator, rotationAnimator)
            doOnEnd {
                parentView.removeView(confetti)
            }
            start()
        }
    }
}
