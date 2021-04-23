package com.example.actualizacionota

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.actualizacionota.MainActivity.Companion.bluetoothDevice
import no.nordicsemi.android.dfu.DfuBaseService
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import java.io.File

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class UploadZip : AppCompatActivity(), View.OnClickListener {

    private var actionBar : ActionBar? = null
    private var resumed = false
    private var dfuCompleted = false
    private var dfuError: String? = null

    private lateinit var progressBar : ProgressBar
    private lateinit var exploreButton : Button
    private lateinit var updateButton : Button
    private lateinit var textPercentage : TextView
    private lateinit var zipName : TextView
    private var uri : Uri? = null
    private lateinit var path : String
    private lateinit var initiator : DfuServiceInitiator


    private var progressListener = object : DfuProgressListenerAdapter() {

        override fun onDeviceConnecting(deviceAddress: String) {
            progressBar.isIndeterminate = true
            textPercentage.setText(R.string.dfu_status_connecting)
            Log.d(":::", "Conectando a dispositivo")
        }

        override fun onDfuProcessStarting(deviceAddress: String) {
            progressBar.isIndeterminate = true
            textPercentage.setText(R.string.dfu_status_starting)
            Log.d(":::", "Proceso DFU iniciado")
        }

        override fun onEnablingDfuMode(deviceAddress: String) {
            progressBar.isIndeterminate = true
            textPercentage.setText(R.string.dfu_status_switching_to_dfu)
            Log.d(":::", "Habilitando modo DFU")
        }

        override fun onFirmwareValidating(deviceAddress: String) {
            progressBar.isIndeterminate = true
            textPercentage.setText(R.string.dfu_status_validating)
            Log.d(":::", "Validando firmware")
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            progressBar.isIndeterminate = true
            textPercentage.setText(R.string.dfu_status_disconnecting)
            Log.d(":::", "Desconectando dispositivo")
        }

        override fun onDfuCompleted(deviceAddress: String) {
            textPercentage.setText(R.string.dfu_status_completed)
            if (resumed){
                clearUI()
                Handler().postDelayed({
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(DfuBaseService.NOTIFICATION_ID)
                }, 200)
            } else {
                dfuCompleted = true
                Log.d(":::", "Proceso DFU finalizado")
                Toast.makeText(applicationContext, "Proceso DFU finalizado", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onDfuAborted(deviceAddress: String) {
            textPercentage.setText(R.string.dfu_status_aborted)
            onUploadCanceled()
            Log.d(":::", "DFU abortado")
            Handler().postDelayed({
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(DfuBaseService.NOTIFICATION_ID)
            }, 200)
        }

        override fun onProgressChanged(deviceAddress: String, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            progressBar.isIndeterminate = false
            progressBar.progress = percent
            textPercentage.text = getString(R.string.dfu_uploading_percentage, percent)
            Log.d(":::", "Progreso cambiado")
        }

        override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String?) {
            if (resumed) {
                Log.e(":::", "Subida fallida: $message")
                Handler().postDelayed({
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(DfuBaseService.NOTIFICATION_ID)
                }, 200)
            } else
                dfuError = message
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_zip)

        progressBar = findViewById(R.id.dfuProgressBar)
        exploreButton = findViewById(R.id.exploreButton)
        updateButton = findViewById(R.id.updateButton)
        textPercentage = findViewById(R.id.textPercentage)
        zipName = findViewById(R.id.zipName)

        actionBar = supportActionBar
        MainActivity.Companion.ActionBarStyle.changeActionBarColor(actionBar!!)

        exploreButton.setOnClickListener(this)
        updateButton.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        DfuServiceListenerHelper.registerProgressListener(this, progressListener)
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        if (dfuCompleted)
            Toast.makeText(applicationContext, "ACTUALIZACIÓN COMPLETADA", Toast.LENGTH_SHORT).show()
        if (dfuError != null)
            Toast.makeText(applicationContext, "ERROR EN LA ACTUALIZACIÓN", Toast.LENGTH_SHORT).show()
        if (dfuCompleted || dfuError != null) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(DfuBaseService.NOTIFICATION_ID)
            dfuCompleted = false
            dfuError = null
        }
    }

    override fun onPause() {
        super.onPause()
        resumed = false
    }

    private fun openFileExplorer() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, 1001)
    }

    private fun initializeDFU() {
            initiator = DfuServiceInitiator(bluetoothDevice.address)
                    .setKeepBond(true)
                    .setDisableNotification(true)
                    .setForeground(true)
                    .setPrepareDataObjectDelay(400)
                    .setZip(uri, path)

            DfuServiceListenerHelper.registerProgressListener(this, progressListener)
            Toast.makeText(applicationContext, "ENVIANDO ZIP", Toast.LENGTH_SHORT).show()

            initiator.start(this, DfuService::class.java)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            uri = data?.data
            path = uri!!.path!!
            zipName.text = "Fichero: $path"
            zipName.visibility = View.VISIBLE
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id){
            R.id.exploreButton -> openFileExplorer()

            R.id.updateButton -> {
                showProgressBar()
                initializeDFU()
            }
        }
    }

    private fun showProgressBar(){
            progressBar.visibility = View.VISIBLE
            textPercentage.visibility = View.VISIBLE
            exploreButton.isEnabled = false
    }

    private fun clearUI(){
        progressBar.visibility = View.INVISIBLE
        textPercentage.visibility = View.INVISIBLE
        exploreButton.isEnabled = true
    }

    private fun onUploadCanceled() {
        clearUI()
        Toast.makeText(applicationContext, R.string.dfu_aborted, Toast.LENGTH_SHORT).show()
    }
}