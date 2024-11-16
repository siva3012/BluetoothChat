package com.example.bluetooth.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.bluetooth.bluetooth.BluetoothConnection
import com.example.bluetooth.ui.theme.messageType
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder

@Entity(tableName = "ConnectedDevices")
data class ConnectedDevices(
    @PrimaryKey(autoGenerate = true)
    val id : Int=0,
    val deviceAddress: String,
    val Connected : Boolean =true,
    @Transient
    val bluetoothConnection: BluetoothConnection
)


class Converters {
    @TypeConverter
    fun fromBluetoothConnection(value: BluetoothConnection): String {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toBluetoothConnection(value: String): BluetoothConnection {
      val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        val type = object : TypeToken<BluetoothConnection>() {}.type
        return gson.fromJson(value, type)
    }
}
