package com.example.actualizacionota.adapter

import android.bluetooth.BluetoothGattCharacteristic
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.actualizacionota.R

class CharacteristicAdapter (var listener: OnItemClickListener) : RecyclerView.Adapter<CharacteristicAdapter.ViewHolder>() {


    private var characteristicsList = mutableListOf<BluetoothGattCharacteristic>()

    inner class ViewHolder (view : View) : RecyclerView.ViewHolder(view), View.OnClickListener{

        val characteristicName : TextView = view.findViewById(R.id.name2)
        val characteristicUuid : TextView = view.findViewById(R.id.uuid2)
        val characteristicProperties : TextView = view.findViewById(R.id.properties)


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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.characteristic, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = characteristicsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.characteristicUuid.append(Html.fromHtml("<b><font color=#000>0x${characteristicsList[position].uuid.toString().split("-")[0].substringAfter("0000").toUpperCase()}</b>"))
        var property = characteristicsList[position].properties
        when(property) {
              2 -> holder.characteristicProperties.append(Html.fromHtml("<b><font color=#000>READ</b>"))
              4 -> {
                  holder.characteristicProperties.append(Html.fromHtml("<b><font color=#000>WRITE NO RESPONSE</b>"))
                  holder.characteristicName.text = "DFU Packet"
              }
              10 -> holder.characteristicProperties.append(Html.fromHtml("<b><font color=#000>READ, WRITE</b>"))
              12 -> holder.characteristicProperties.append(Html.fromHtml("<b><font color=#000>WRITE, WRITE NO RESPONSE</b>"))
              16 -> holder.characteristicProperties.append(Html.fromHtml("<b><font color=#000>NOTIFY</b>"))
              18 -> holder.characteristicProperties.append(Html.fromHtml("<b><font color=#000>NOTIFY, READ</b>"))
              24 -> {
                  holder.characteristicProperties.append(Html.fromHtml("<b><font color=#000>NOTIFY, WRITE</b>"))
                  holder.characteristicName.text = "DFU Control Point"
              }
              26 -> holder.characteristicProperties.append(Html.fromHtml("<b><font color=#000>NOTIFY, READ, WRITE</b>"))
              32 -> holder.characteristicProperties.append(Html.fromHtml("<b><font color=#000>INDICATE</b>"))
        }
    }

    fun setData(characteristicsList : MutableList<BluetoothGattCharacteristic>) {
        this.characteristicsList = characteristicsList
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
}