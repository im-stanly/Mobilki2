package com.example.mobilki2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var ballView: BouncingBallView
    private var sensorManager: SensorManager? = null
    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val ax = -it.values[0]
                    val ay = it.values[1]
                    ballView.updateAcceleration(ax, ay)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ballView = BouncingBallView(this)
        setContentView(ballView)
    }

    override fun onResume() {
        super.onResume()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager?.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(sensorEventListener)
    }
}

class BouncingBallView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED }
    private var xPos = 0f
    private var yPos = 0f
    private var xVel = 8f
    private var yVel = 8f
    private val ballRadius = 200f
    private var lastX = 0f
    private var lastY = 0f
    private var isBallTouched = false

    private val updateRunnable = object : Runnable {
        override fun run() {
            xPos += xVel
            yPos += yVel
            if ((xPos - ballRadius < 0 && xVel < 0) || (xPos + ballRadius > width && xVel > 0)) xVel = -xVel
            if ((yPos - ballRadius < 0 && yVel < 0) || (yPos + ballRadius > height && yVel > 0)) yVel = -yVel
            xVel *= 0.98f
            yVel *= 0.98f
            invalidate() // Request to redraw the view
            postDelayed(this, 16)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        xPos = (w / 2).toFloat()
        yPos = (h / 2).toFloat()
        post(updateRunnable)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(xPos, yPos, ballRadius, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val dist = Math.hypot((event.x - xPos).toDouble(), (event.y - yPos).toDouble()).toFloat()
                isBallTouched = dist <= ballRadius
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (isBallTouched) {
                    xPos = event.x
                    yPos = event.y
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isBallTouched) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    xVel = dx * 0.3f
                    yVel = dy * 0.3f
                }
            }
        }
        return true
    }

    fun updateAcceleration(ax: Float, ay: Float) {
        xVel += ax
        yVel += ay
    }
}