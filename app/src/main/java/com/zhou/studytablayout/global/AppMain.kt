package com.zhou.studytablayout.global

import android.app.Application

class AppMain : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.getInstance().init(this)
    }
}