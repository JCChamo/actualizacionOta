package com.example.actualizacionota.adapter

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.actualizacionota.R
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ServiceAdapter (var listener: OnItemClickListener) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    private lateinit var serviceList : ArrayList<BluetoothGattService>

    companion object {
        var listOfCharacteristicMap = arrayListOf<HashMap<Int, MutableList<BluetoothGattCharacteristic>>>()
    }


    inner class ViewHolder (view : View) : RecyclerView.ViewHolder(view), View.OnClickListener{

        val serviceName : TextView = view.findViewById(R.id.name)
        val serviceUuid : TextView = view.findViewById(R.id.uuid)
        val servicePriority : TextView = view.findViewById(R.id.priority)


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemClick(position)
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.service, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = serviceList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = serviceList[position]
        val serviceNameCodeTokens = service.uuid.toString().split('-')
        val serviceName = serviceNameCodeTokens[0].substringAfter("0000").toUpperCase()
        when(serviceName){
            "1800" -> holder.serviceName.text = "Acceso Genérico"
            "1801" -> holder.serviceName.text = "Atributo Genérico"
            "180D" -> holder.serviceName.text = "Ritmo cardiaco"
            "180F" -> holder.serviceName.text = "Servicio de Batería"
            "181C" -> holder.serviceName.text = "Datos de Usuario"
            "FE59" -> holder.serviceName.text = "Servicio seguro DFU"
            else -> holder.serviceName.text = "Servicio Desconocido"
        }
        holder.serviceUuid.append(Html.fromHtml("<b><font color=#000>0x$serviceName</b>"))
        holder.servicePriority.text = "SERVICIO PRIMARIO"

        var characteristicList = mutableListOf<BluetoothGattCharacteristic>()
        characteristicList = service.characteristics
        var characteristicMap = hashMapOf<Int, MutableList<BluetoothGattCharacteristic>>()
        characteristicMap[position] = characteristicList
        listOfCharacteristicMap.add(characteristicMap)
    }

    fun setData(serviceList : ArrayList<BluetoothGattService>) {
        this.serviceList = serviceList
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

}