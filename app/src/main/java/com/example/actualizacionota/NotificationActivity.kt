package com.example.actualizacionota

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

class NotificationActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isTaskRoot){
            val startAppIntent = Intent(this, DfuService::class.java)
            if (intent != null)
                intent.extras?.let { startAppIntent.putExtras(it) }
            startActivity(startAppIntent)
        }
        finish()
    }
}