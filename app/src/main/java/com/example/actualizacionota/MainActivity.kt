package com.example.actualizacionota

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import no.nordicsemi.android.dfu.DfuServiceInitiator
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), View.OnClickListener {

    var actionBar: ActionBar? = null
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mac: TextView
    var colorDrawable: ColorDrawable? = null
    private lateinit var scanButton: Button
    private lateinit var connectButton: Button
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var scanning = false
    private lateinit var mLeScanCallback : ScanCallback
    private lateinit var bluetoothManager : BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var scanResult : ScanResult


    companion object {
        lateinit var bluetoothDevice: BluetoothDevice
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mac = findViewById(R.id.mac)
        scanButton = findViewById(R.id.scanButton)
        connectButton = findViewById(R.id.connectButton)
        mProgressBar = findViewById(R.id.progressbar)

        ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
        )

        actionBar = supportActionBar
        colorDrawable = ColorDrawable(Color.parseColor("#d1c4e9"))
        actionBar!!.setBackgroundDrawable(colorDrawable)
        mProgressBar.visibility = View.GONE

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter


        scanButton.setOnClickListener(this)
        connectButton.setOnClickListener(this)
    }

    private fun checkBluetoothConnectivity() : Boolean {
        if(!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return false
        }
        return true
    }

    private fun progressBarAction(){
        mProgressBar.visibility = View.VISIBLE
        Handler().postDelayed({
            mProgressBar.visibility = View.GONE
        }, 2000)
    }

    private fun searchDevice() {
        mLeScanCallback = object : ScanCallback(){
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e(":::", "ERROR: $errorCode")
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                if (result?.device?.address == "D4:50:A8:29:5E:50"){
                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(result.device?.address)

                    mac.text = bluetoothDevice.address

                    scanResult = result

                    Log.d(":::", "Dispositivo bluetooth inicializado")
                }
            }
        }
        scanLeDevice()
    }

    private fun scanLeDevice() {
        val SCAN_PERIOD = 2000L
        bluetoothLeScanner.let { scanner ->
            if (!scanning) {
                Handler().postDelayed({
                    scanning = false
                    scanner.stopScan(mLeScanCallback)
                }, SCAN_PERIOD)
                scanning = true
                scanner.startScan(mLeScanCallback)
            } else {
                scanning = false
                scanner.stopScan(mLeScanCallback)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.scanButton ->{
                if (checkBluetoothConnectivity()) {
                    bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
                    progressBarAction()
                    searchDevice()
                    connectButton.visibility = View.VISIBLE
                }
            }
            R.id.connectButton -> {
                if (mac.length() != 0)
                    getServices()
            }
        }
    }

    private fun getServices() {
        val intent = Intent(this, Services::class.java)
        startActivity(intent)
    }
}