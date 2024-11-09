package com.example.bluetooth.ui.theme

import android.os.Message

interface MessageReceived {
    fun MessageReceived(message: String)
    fun connected()
}