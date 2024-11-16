package com.example.bluetooth.ui.theme

interface MessageReceived {
    fun MessageReceived(message: String,macAddress:String)
    fun connected(macAddress:String,openScreen :Boolean)
}