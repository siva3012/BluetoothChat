package com.example.bluetooth.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluetooth.ui.theme.messageType

@Entity(tableName = "ChatData")
data class EntityClass(
    @PrimaryKey(autoGenerate = true)
    val id : Int=0,
    val deviceAddress: String,
    val deviceName :String="",
    val message : String,
    val timeStamp : String,
    val MessageType : messageType
)