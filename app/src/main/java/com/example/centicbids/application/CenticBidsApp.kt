package com.example.centicbids.application

import android.app.Application
import com.example.centicbids.util.createNotificationChannel

class CenticBidsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }
}