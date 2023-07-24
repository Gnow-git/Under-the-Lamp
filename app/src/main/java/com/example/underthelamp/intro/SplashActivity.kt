package com.example.underthelamp.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.underthelamp.MainActivity
import com.example.underthelamp.R

class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // val intent = Intent(this, StartActivity::class.java)
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            finish()
        }, 1000)
    }
}