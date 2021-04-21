package com.example.actualizacionota.adapter


import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actualizacionota.MainActivity
import com.example.actualizacionota.NordicDfuService
import com.example.actualizacionota.R
import com.example.actualizacionota.Services
import no.nordicsemi.android.dfu.DfuServiceInitiator
import java.io.File

import java.util.*

class Characteristics : AppCompatActivity(), CharacteristicAdapter.OnItemClickListener {

    private lateinit var recyclerView : RecyclerView
    private lateinit var characteristicAdapter: CharacteristicAdapter
    private var listOfCharacteristicMap = ServiceAdapter.listOfCharacteristicMap
    private var pos: Int = 0
    private var firstValue: Int = 0
    private lateinit var listView : ListView

    private lateinit var bluetoothGatt : BluetoothGatt
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var id : String
    private lateinit var list : MutableList<BluetoothGattCharacteristic>
    private lateinit var path : String
    private lateinit var initiator : DfuServiceInitiator

    companion object {
        lateinit var messageList : ArrayList<String>
        lateinit var adapter : ArrayAdapter<String>
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.characteristics)

        recyclerView = findViewById(R.id.recycler2)
        listView = findViewById(R.id.listView)
        bluetoothGatt = Services.bluetoothGatt
        bluetoothDevice = MainActivity.bluetoothDevice

        messageList = arrayListOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messageList)
        listView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(this)
        var pos = this.intent.extras?.get("position") as Int
        getCharacteristics()
        list = listOfCharacteristicMap[pos][pos]!!
        characteristicAdapter.setData(list)

    }

    override fun onItemClick(position: Int) {
        val characteristic = list[position]

        if(characteristic.properties == 4){
            initializeDFU()
            sendZip()
        }
    }

    private fun sendZip() {
        initiator.setZip(Uri.fromFile(File(path)))
        initiator.start(this, NordicDfuService::class.java)
        Toast.makeText(applicationContext, "ZIP ENVIADO", Toast.LENGTH_SHORT).show()
    }

    private fun initializeDFU() {
        initiator = DfuServiceInitiator(bluetoothDevice.address).setKeepBond(false)
        initiator.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
        openFileExplorer()
        Log.d (":::", "Setting zip to: $path");
    }

    private fun openFileExplorer() {
        val intent = Intent()
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "DEMO"), 1001)
    }

    private fun getCharacteristics(){
        characteristicAdapter = CharacteristicAdapter(this)
        recyclerView.adapter = characteristicAdapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun String.decodeHex(): ByteArray = chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()

    private fun decimalToHexadecimal(number : Int) : String {
        return number.toString(16).toUpperCase()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val currFileURI = data?.data
            path = currFileURI!!.path!!
        }
    }
}

fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
    return properties and property != 0
}
