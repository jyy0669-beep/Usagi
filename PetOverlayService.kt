package com.example.usagipet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator

class PetOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var petView: ImageView? = null
    private var bobAnimator: ObjectAnimator? = null

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val image = ImageView(this).apply {
            setImageResource(R.drawable.usagi)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "떠다니는 우사기"
        }
        val size = (120 * resources.displayMetrics.density).toInt()
        val params = WindowManager.LayoutParams(
            size, size,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 220
        }
        windowManager.addView(image, params)
        petView = image

        // Drag to move the pet. A gentle bob gives it a little life.
        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        image.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY
                    startX = params.x; startY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (event.rawX - downX).toInt()
                    params.y = startY + (event.rawY - downY).toInt()
                    windowManager.updateViewLayout(image, params)
                    true
                }
                else -> true
            }
        }
        bobAnimator = ObjectAnimator.ofPropertyValuesHolder(
            image,
            PropertyValuesHolder.ofFloat("translationY", 0f, -7f, 0f),
            PropertyValuesHolder.ofFloat("rotation", 0f, 2f, 0f, -2f, 0f)
        ).apply {
            duration = 1800
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun startAsForeground() {
        val channelId = "usagi_pet_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "우사기 펫", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(this)
        }.setContentTitle("우사기 펫 실행 중")
            .setContentText("화면 위의 우사기를 드래그해서 옮길 수 있어요.")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        bobAnimator?.cancel()
        petView?.let { try { windowManager.removeView(it) } catch (_: Exception) {} }
        petView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
