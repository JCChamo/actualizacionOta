package com.example.actualizacionota

import android.app.Activity
import no.nordicsemi.android.dfu.DfuBaseService

class NordicDfuService : DfuBaseService() {
    override fun getNotificationTarget(): Class<out Activity>? {
        return null
    }

}