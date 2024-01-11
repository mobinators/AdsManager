package com.mobinators.ads.managers

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.mobinators.ads.managers.databinding.ActivitySplashBinding
import pak.developer.app.managers.ui.commons.base.BaseActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun getActivityView() = ActivitySplashBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
        }, 5000)
    }
}