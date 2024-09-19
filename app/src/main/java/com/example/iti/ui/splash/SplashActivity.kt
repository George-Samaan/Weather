package com.example.iti.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.iti.databinding.ActivitySplashBinding
import com.example.iti.ui.homeScreen.view.HomeScreenActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(mainLooper).postDelayed({
            startActivity()
        }, 3000)
    }

    private fun startActivity() {
        val intent = Intent(this, HomeScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

}
