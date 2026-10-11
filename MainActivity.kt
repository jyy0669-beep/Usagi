package com.example.usagipet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(36, 36, 36, 36)
        }
        layout.addView(TextView(this).apply {
            text = "🐰 우사기 화면 펫\n\n아래 버튼을 누른 뒤 ‘다른 앱 위에 표시’를 허용해 줘!"
            textSize = 19f
            gravity = Gravity.CENTER
        })
        layout.addView(Button(this).apply {
            text = "우사기 띄우기"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                    startActivity(intent)
                } else {
                    val serviceIntent = Intent(this@MainActivity, PetOverlayService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent)
                    else startService(serviceIntent)
                }
            }
        })
        layout.addView(Button(this).apply {
            text = "우사기 숨기기"
            setOnClickListener { stopService(Intent(this@MainActivity, PetOverlayService::class.java)) }
        })
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        // After granting overlay permission, tap “우사기 띄우기” once more.
    }
}
