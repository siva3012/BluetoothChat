package com.example.bluetooth.ui.theme

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.bluetooth.BluetoothConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class messageFormat(
    val message: String,
    val state : messageType
)
enum class messageType{
    Received,Sent
}

class DisplayViewModel:ViewModel() {
    private val _uistate1= MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val uistate:StateFlow<List<BluetoothDevice>> get() =_uistate1

    private val _messageList= MutableStateFlow<List<messageFormat>>(emptyList())
    val messageList:StateFlow<List<messageFormat>> get() =_messageList
    val list = ArrayList<messageFormat>()
    val receivedMessage = mutableStateOf("")
    val connectedDevice = mutableStateOf("")
   private val bluetoothConnection = BluetoothConnection.getInstance()

    fun addvalues(list:BluetoothDevice){
        Log.d("list",list.toString())
        _uistate1.value=_uistate1.value+list
    }

    fun connectToDevice(device: BluetoothDevice){
        connectedDevice.value=device.address
        CoroutineScope(Dispatchers.IO).launch {
            bluetoothConnection.connect(device)
        }
    }

    fun sendMessage(message: String){
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothConnection.sendMessage(message)
        }
        viewModelScope.launch(Dispatchers.Main) {
            val messageFormat = messageFormat(message, messageType.Sent)
            val currentMessage = _messageList.value
            _messageList.value = currentMessage + messageFormat
        }
    }

    fun ReceiveMessage(message: String){
        Log.d("Viewmodel","${message}")
        viewModelScope.launch(Dispatchers.Main) {
            receivedMessage.value = message
            val messageFormat = messageFormat(message, messageType.Received)
            val currentMessage = _messageList.value
            _messageList.value = currentMessage + messageFormat
        }
    }


}


