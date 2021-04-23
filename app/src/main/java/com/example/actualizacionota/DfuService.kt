package com.example.actualizacionota

import android.app.Activity
import android.util.Log
import no.nordicsemi.android.dfu.DfuBaseService

class DfuService : DfuBaseService() {
    override fun getNotificationTarget(): Class<out Activity>? {
        return NotificationActivity::class.java
    }

    override fun isDebug(): Boolean = true
}