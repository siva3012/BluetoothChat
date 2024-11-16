package com.example.bluetooth.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.example.bluetooth.database.ChatDatabase
import com.example.bluetooth.database.EntityClass
import com.example.bluetooth.ui.theme.MessageReceived
import com.example.bluetooth.ui.theme.messageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.concurrent.thread

class BluetoothConnection() {
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID
     var socket: BluetoothSocket? = null
    private var dataListener: ((ByteArray) -> Unit)? = null
    private var messageReceived : MessageReceived? =null
    var context :Context? =null
//    companion object {
//
//        @Volatile
//        private var instance: BluetoothConnection? = null
//
//        fun getInstance(): BluetoothConnection {
//            return instance ?: synchronized(this) {
//                instance ?: BluetoothConnection().also { instance = it }
//            }
//        }
//    }

    // For client connection (Phone 1)
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice): Boolean {
        val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled || device == null) {
            return false
        }
        return try {
            Log.w("Connection","Connection Started")
            socket = device.createRfcommSocketToServiceRecord(uuid)
            Log.w("Connection","socket ${socket}")
            socket?.connect() // Client connects to the server

            // Set up streams for communication
            startListening(socket!!) // Start listening for incoming data

            Log.d("Connected","Connected")
            Log.d("BluetoothConnection","$messageReceived")
            messageReceived?.connected(device.address,true)
            sendMessage("this is connected device ${device.address}",socket!!)
            true
        } catch (e: Exception) {
            Log.d("BluetoothHelper", "Connection failed: $e")
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun startServer(context: Context): Boolean {
                val adapter = BluetoothAdapter.getDefaultAdapter() ?: return false
                return try {
                    CoroutineScope(Dispatchers.IO).launch {
                    val serverSocket = adapter.listenUsingRfcommWithServiceRecord("MyBluetoothApp", uuid)
                    Log.d("BluetoothHelper", "Socket Accepting ${serverSocket}")
                    socket = serverSocket.accept() // Server accepts connection from client
                    this@BluetoothConnection.context=context
                    // Set up streams for communication

                    Log.d("BluetoothHelper", "Connection accepted from: ${socket?.remoteDevice?.name}")
                    startListening(socket!!) // Start listening for incoming data
                    Log.d("BluetoothConnection","$messageReceived")
                    val dao = context.let { ChatDatabase.getDatabase(it).chatDao() }
                        socket?.remoteDevice?.address?.let {
                           BluetoothServerService.getBluetoothServerServiceInstance().connectionMap.put(it, AddedBY(socket!!,true))
                        }
                        serverSocket.close()
                    }
           // serverSocket.close() // Close server socket after accepting
            true
        } catch (e: Exception) {
            Log.d("BluetoothHelper", "Server start failed: $e")
            false
        }
    }

//    @SuppressLint("MissingPermission")
//    fun startServer(context: Context): Boolean {
//        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return false
//        this@BluetoothConnection.context=context
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val serverSocket = adapter.listenUsingRfcommWithServiceRecord("MyBluetoothApp", uuid)
//                Log.d("BluetoothHelper", "Server socket accepting connections")
//
//                while (true) { // Continuously accept new connections
//                    val clientSocket = serverSocket.accept()
//                    val clientAddress = clientSocket.remoteDevice?.address ?: "Unknown"
//
//                    Log.d("BluetoothHelper", "Connection accepted from: $clientAddress")
//                  //  connectionMap[clientAddress] = AddedBY(clientSocket, true) // Add each client socket to the map
//                        BluetoothServerService.getBluetoothServerServiceInstance().connectionMap.put(clientAddress, AddedBY(socket!!,true))
//                    // Start listening on a separate coroutine
//                    CoroutineScope(Dispatchers.IO).launch {
//                        startListening(clientSocket)
//                    }
//                }
//            } catch (e: Exception) {
//                Log.d("BluetoothHelper", "Server start failed: $e")
//            }
//        }
//        return true
//    }


    // Common function to send messages for both client and server
    fun sendMessage(message: String,socket: BluetoothSocket) {
        val data = message.toByteArray()
        try {
         val outputStream=socket?.outputStream
            outputStream?.write(data)
            Log.d("BluetoothHelper", "Sent: $message")
        } catch (e: Exception) {
            Log.d("BluetoothHelper", "Failed to send message: $e")
        }
    }

    // Start listening for incoming messages
    private fun startListening(socket: BluetoothSocket) {
        thread {
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (true) {
                try {
                    val inputStream = socket?.inputStream
                    bytesRead = inputStream?.read(buffer) ?: -1
                    if (bytesRead == -1) {
                        Log.d("BluetoothHelper", "Connection lost")
                        break
                    }
                    val data = buffer.copyOf(bytesRead)
                    Log.d("BluetoothHelper", "Received: ${String(data)}")
                    socket?.remoteDevice?.address?.let {
                        receivedMessageAddToDB(message = String(data),it)
                    }
                    dataListener?.invoke(data)
                } catch (e: Exception) {
                    Log.d("BluetoothHelper", "Error: $e")
                    break
                }
            }
        }
    }

    // Set a callback for receiving data
    fun setOnDataReceivedListener(listener: (ByteArray) -> Unit) {
        dataListener = listener
    }

    // Close connection and cleanup resources
    fun closeConnection() {
        try {
//            inputStream?.close()
//            outputStream?.close()
            socket?.close()
            Log.d("BluetoothHelper", "Connection closed")
        } catch (e: Exception) {
            Log.d("BluetoothHelper", "Failed to close connection: $e")
        }
    }
    fun registerMessageReceiver(messageReceived: MessageReceived){
        Log.d("BluetoothConnection","$messageReceived")
        this.messageReceived=messageReceived
    }

    fun receivedMessageAddToDB(message: String,macAddress:String){
        Log.d("Message Received","Received Message from $macAddress and the message is $message")
        val dao = context?.let { ChatDatabase.getDatabase(it).chatDao() }
        Log.d("Db insert ","${dao}")
        val entity  = EntityClass(deviceAddress = macAddress,
            message = message,
            MessageType = messageType.Received,
            timeStamp = System.currentTimeMillis().toString())
        CoroutineScope(Dispatchers.IO).launch {
            dao?.Insert(entity)
        }
    }

}
