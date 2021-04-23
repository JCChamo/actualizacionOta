package com.example.actualizacionota

import android.app.Activity
import android.util.Log
import no.nordicsemi.android.dfu.DfuBaseService

class DfuService : DfuBaseService() {
    override fun getNotificationTarget(): Class<out Activity>? {
        Log.d(":::", "getNotificationTarget de DfuService")
        return UploadZip::class.java
    }

    override fun isDebug(): Boolean = true
}