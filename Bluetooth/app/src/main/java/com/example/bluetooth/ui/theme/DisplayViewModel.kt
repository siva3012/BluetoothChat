package com.example.bluetooth.ui.theme

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.bluetooth.AddedBY
import com.example.bluetooth.bluetooth.BluetoothConnection
import com.example.bluetooth.bluetooth.BluetoothServerService
import com.example.bluetooth.database.ChatDatabase
import com.example.bluetooth.database.EntityClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

class DisplayViewModel(private val context :Context) : ViewModel() {
    val database = ChatDatabase.getDatabase(context)
    val dao=database.chatDao()
    private val _uistate1= MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val uistate:StateFlow<List<BluetoothDevice>> get() =_uistate1

    private val _chatData= MutableStateFlow<List<EntityClass>>(emptyList())
    val chatData:StateFlow<List<EntityClass>> get() =_chatData
    val availableDevicesInDb : Flow<List<String>> = dao.getAllMAcAddress()
    private val _messageList= MutableStateFlow<List<messageFormat>>(emptyList())
    val messageList:StateFlow<List<messageFormat>> get() =_messageList
    val list = ArrayList<messageFormat>()
    val receivedMessage = mutableStateOf("")
    var bluetoothConnection:BluetoothConnection= BluetoothConnection()
    var messageReceived :MessageReceived? =null
    var connectedDeviceAddress = mutableStateOf("")
    var socket :BluetoothSocket? =null


    fun addvalues(list:BluetoothDevice){
        Log.d("list",list.toString())
        _uistate1.value=_uistate1.value+list
    }

     fun connectToDevice(device: BluetoothDevice){
        connectedDeviceAddress.value=device.address
         val connectionMap = BluetoothServerService.getBluetoothServerServiceInstance().connectionMap
         for ((key, value) in connectionMap) {
             Log.d("MapLog", "Key: $key, Value: ${value.socket.isConnected}")
         }
        if (connectionMap.containsKey(connectedDeviceAddress.value)){
            val socket =connectionMap[connectedDeviceAddress.value]?.socket
            if (socket?.isConnected==true){
                this.socket=socket
                bluetoothConnection.socket=socket
                messageReceived?.connected(macAddress = device.address,true)
                return
            }
        }
        bluetoothConnection=BluetoothConnection()
        bluetoothConnection.context=context
        messageReceived?.let { bluetoothConnection.registerMessageReceiver(it) }
        CoroutineScope(Dispatchers.IO).launch {
            bluetoothConnection.connect(device)
        }
    }

//    fun sendMessage(message: String){
//        viewModelScope.launch(Dispatchers.IO) {
//            bluetoothConnection.sendMessage(message)
//            withContext(Dispatchers.Main) {
//                val messageFormat = messageFormat(message, messageType.Sent)
//                val currentMessage = _messageList.value
//                _messageList.value = currentMessage + messageFormat
//            }
//        }
//    }

    fun ReceiveMessage(message: String){
        Log.d("Viewmodel","${message}")
        viewModelScope.launch(Dispatchers.Main) {
            receivedMessage.value = message
            val messageFormat = messageFormat(message, messageType.Received)
            val currentMessage = _messageList.value
            _messageList.value = currentMessage + messageFormat
        }
    }

    fun registerMessageReceived(messageReceived: MessageReceived){
       this.messageReceived=messageReceived
        messageReceived?.let { bluetoothConnection?.registerMessageReceiver(it) }
    }

    fun connected(macAddress: String){
        bluetoothConnection.let { it.socket?.let { it1 ->
            BluetoothServerService.getBluetoothServerServiceInstance().connectionMap.put(macAddress,
                AddedBY(it1,false)
            )
        } }
        viewModelScope.launch(Dispatchers.Default) {
            dao.getDataByMAcAddress(connectedDeviceAddress.value).collect{
                _chatData.value=it
            }
        }
    }

    fun receivedMessageAddToDB(message: String,macAddress:String){
        val entity  = EntityClass(deviceAddress = macAddress,
            message = message,
            MessageType = messageType.Received,
            timeStamp = System.currentTimeMillis().toString())
        viewModelScope.launch(Dispatchers.IO) {
            dao.Insert(entity)
        }
    }


    fun sendMessageAddToDB(message: String){
        val connectionMap = BluetoothServerService.getBluetoothServerServiceInstance().connectionMap
        for ((key, value) in connectionMap) {
            Log.d("MapLog", "Key: $key, Value: ${value.socket.isConnected}")
        }
        if (connectionMap.containsKey(connectedDeviceAddress.value)){
            val socket =connectionMap[connectedDeviceAddress.value]?.socket
            if (socket?.isConnected==true){
                this.socket=socket
                val entity  = EntityClass(deviceAddress = connectedDeviceAddress.value,
                message = message,
                MessageType = messageType.Sent,
                timeStamp = System.currentTimeMillis().toString())
                viewModelScope.launch(Dispatchers.IO) {
                    bluetoothConnection.sendMessage(message,socket)
                    dao.Insert(entity)
                }
            }
        }

    }
}


