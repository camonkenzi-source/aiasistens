package com.ziimodsx.ai

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: ImageView
    private lateinit var params: WindowManager.LayoutParams
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = ImageView(this)
        floatingView.setImageResource(R.mipmap.ic_launcher)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        params = WindowManager.LayoutParams(150, 150, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 300
        windowManager.addView(floatingView, params)
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        floatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = (event.rawX - initialTouchX).toInt()
                    val diffY = (event.rawY - initialTouchY).toInt()
                    if (kotlin.math.abs(diffX) < 10 && kotlin.math.abs(diffY) < 10) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }
}
