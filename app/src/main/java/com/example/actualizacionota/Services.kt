package com.example.actualizacionota

import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actualizacionota.adapter.Characteristics
import com.example.actualizacionota.adapter.Characteristics.Companion.adapter
import com.example.actualizacionota.adapter.Characteristics.Companion.messageList
import com.example.actualizacionota.adapter.ServiceAdapter

class Services : AppCompatActivity(), ServiceAdapter.OnItemClickListener{
    lateinit var recyclerView : RecyclerView
    lateinit var serviceAdapter: ServiceAdapter
    private var gattServiceList = arrayListOf<BluetoothGattService>()
    private lateinit var bluetoothDevice : BluetoothDevice
    private lateinit var bluetoothGattCallback: BluetoothGattCallback
    private lateinit var context: Context
    private lateinit var listener: ServiceAdapter.OnItemClickListener
    private lateinit var mProgressBar: ProgressBar
    private lateinit var spinner: Spinner

    companion object {
        lateinit var bluetoothGatt: BluetoothGatt
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.services)

        context = applicationContext
        listener = this
        bluetoothDevice = MainActivity.bluetoothDevice

        mProgressBar = findViewById(R.id.progressbar2)
        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)


        mProgressBar.visibility = View.GONE
        progressBarAction()

        connectDevice()

    }
    override fun onItemClick(position: Int) {
//        var adapter = SpinnerAdapter(context, R.layout.spinner_list, listOfCharacteristicMap[position][position]!!)
//        spinner.adapter = adapter
        val intent = Intent(this, Characteristics::class.java)
        intent.putExtra("position", position)
        startActivity(intent)
    }

    private fun connectDevice(){
        bluetoothGattCallback = object : BluetoothGattCallback(){
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                when {
                    status == BluetoothGatt.GATT_SUCCESS -> {
                        Log.d(":::", "Conectado a ${gatt?.device?.address}")
                        gatt?.discoverServices()

                    }
                    newState == BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d(":::", "Desconectado de ${gatt?.device?.address}")
                        gatt?.close()
                    }
                    else -> {
                        Log.d(":::", "Error $status encontrado con ${gatt?.device?.address}. Desconectando...")
                        gatt?.close()
                    }
                }

            }
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                gattServiceList.addAll(bluetoothGatt.services)
                if (gattServiceList.isEmpty()){
                    Log.d(":::", "No se han detectado servicios")
                } else {
                    Log.d(":::", "Se han detectado servicios")
                    serviceAdapter = ServiceAdapter(listener)
                    serviceAdapter.setData(gattServiceList)
                    runOnUiThread {
                        recyclerView.adapter = serviceAdapter
                        recyclerView.setHasFixedSize(true)
                        recyclerView.layoutManager = LinearLayoutManager(context)
                        serviceAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        bluetoothGatt = bluetoothDevice.connectGatt(applicationContext, false, bluetoothGattCallback)

        Toast.makeText(applicationContext, "CONECTADO", Toast.LENGTH_SHORT).show()
    }

    private fun progressBarAction(){
        mProgressBar.visibility = View.VISIBLE
        Handler().postDelayed({
            mProgressBar.visibility = View.GONE
        }, 1500)
    }
}