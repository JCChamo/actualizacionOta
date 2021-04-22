package com.example.actualizacionota

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.actualizacionota.MainActivity.Companion.bluetoothDevice
import no.nordicsemi.android.dfu.*
import java.io.File

class UploadZip : AppCompatActivity(), View.OnClickListener {

    private var actionBar : ActionBar? = null
    private var resumed = false
    private lateinit var progressBar : ProgressBar
    private lateinit var exploreButton : Button
    private lateinit var textPercentage : TextView
    private lateinit var zipName : TextView
    private lateinit var context: Context
    private var uri : Uri? = null
    private var boolean = false
    private lateinit var path : String
    private lateinit var initiator : DfuServiceInitiator
    private lateinit var progressListener : DfuProgressListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_zip)

        progressBar = findViewById(R.id.dfuProgressBar)
        exploreButton = findViewById(R.id.exploreButton)
        textPercentage = findViewById(R.id.textPercentage)
        zipName = findViewById(R.id.zipName)

        actionBar = supportActionBar
        MainActivity.Companion.ActionBarStyle.changeActionBarColor(actionBar!!)

        exploreButton.setOnClickListener(this)

        context = applicationContext


        progressListener = object : DfuProgressListenerAdapter() {
            override fun onProgressChanged(deviceAddress: String, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
                super.onProgressChanged(deviceAddress, percent, speed, avgSpeed, currentPart, partsTotal)
                progressBar.isIndeterminate = false
                progressBar.progress = percent
                textPercentage.text = getString(R.string.dfu_uploading_percentage, percent)
                Log.d(":::", "Progreso cambiado")
            }

            override fun onDeviceDisconnecting(deviceAddress: String?) {
                super.onDeviceDisconnecting(deviceAddress)
                progressBar.isIndeterminate = true
                textPercentage.setText(R.string.dfu_status_disconnecting)
                Log.d(":::", "Desconectando dispositivo")
            }

            override fun onDfuProcessStarting(deviceAddress: String) {
                super.onDfuProcessStarting(deviceAddress)
                progressBar.isIndeterminate = true
                textPercentage.setText(R.string.dfu_status_starting)
                Log.d(":::", "Proceso DFU iniciado")
            }

            override fun onDfuAborted(deviceAddress: String) {
                super.onDfuAborted(deviceAddress)
                textPercentage.setText(R.string.dfu_status_aborted)
                onUploadCanceled()
                Log.d(":::", "DFU abortado")
            }

            override fun onEnablingDfuMode(deviceAddress: String) {
                super.onEnablingDfuMode(deviceAddress)
                progressBar.isIndeterminate = true
                textPercentage.setText(R.string.dfu_status_switching_to_dfu)
                Log.d(":::", "Habilitando modo DFU")
            }

            override fun onDfuCompleted(deviceAddress: String) {
                super.onDfuCompleted(deviceAddress)
                textPercentage.setText(R.string.dfu_status_completed)
                if (!resumed){
                    Log.d(":::", "Proceso DFU finalizado")
                    Toast.makeText(applicationContext, "Proceso DFU finalizado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFirmwareValidating(deviceAddress: String) {
                super.onFirmwareValidating(deviceAddress)
                progressBar.isIndeterminate = true
                textPercentage.setText(R.string.dfu_status_validating)
                Log.d(":::", "Validando firmware")
            }

            override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String?) {
                super.onError(deviceAddress, error, errorType, message)
                if (resumed) {
                    clearUI()
                    Log.e(":::", "Subida fallida: $message")
                }
            }

            override fun onDeviceConnecting(deviceAddress: String) {
                super.onDeviceConnecting(deviceAddress)
                progressBar.isIndeterminate = true
                textPercentage.setText(R.string.dfu_status_connecting)
                Log.d(":::", "Conectando dispositivo")
            }
        }

        DfuServiceListenerHelper.registerProgressListener(this, progressListener)
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        DfuServiceListenerHelper.registerProgressListener(this, progressListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        DfuServiceListenerHelper.registerProgressListener(this, progressListener)
    }

    override fun onPause() {
        super.onPause()
        DfuServiceListenerHelper.registerProgressListener(this, progressListener)
    }

    private fun openFileExplorer() {
        val intent = Intent()
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "DEMO"), 1001)
    }

    private fun initializeDFU() {
        if(boolean) {
            initiator = DfuServiceInitiator(bluetoothDevice.address).setKeepBond(true)
            //Las tres siguientes líneas si las comento, no rompe la aplicación
            initiator.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
            initiator.setPacketsReceiptNotificationsEnabled(true)
            initiator.setPacketsReceiptNotificationsValue(DfuServiceInitiator.DEFAULT_PRN_VALUE)
            Log.d(":::", "Asignando zip en: $path")
        }
    }

    @SuppressLint("WrongConstant")
    private fun sendZip() {
        if(boolean) {
            initiator.setZip(Uri.fromFile(File(path)))
            Log.d(":::", Uri.fromFile(File(path)).toString())
//            initiator.setBinOrHex(DfuBaseService.TYPE_AUTO, uri, path)
            DfuServiceListenerHelper.registerProgressListener(this, progressListener)
            initiator.start(this, DfuService::class.java)
            Toast.makeText(applicationContext, "ENVIANDO ZIP", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            uri = data?.data
            Log.d(":::", data?.dataString.toString())
            path = uri!!.path!!
            val pathTokens = path.split("/")
            zipName.text = "Fichero: ${pathTokens[pathTokens.size - 1]}"
            zipName.visibility = View.VISIBLE
            boolean = true
        }
    }

    override fun onClick(p0: View?) {
        openFileExplorer()
        initializeDFU()
        sendZip()
        showProgressBar()
    }

    private fun showProgressBar(){
            progressBar.visibility = View.VISIBLE
            textPercentage.visibility = View.VISIBLE
//            textPercentage.text = null
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