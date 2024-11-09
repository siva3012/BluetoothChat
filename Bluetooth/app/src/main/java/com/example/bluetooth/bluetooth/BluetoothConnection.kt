package com.example.bluetooth.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.bluetooth.ui.theme.MessageReceived
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.concurrent.thread

class BluetoothConnection() {
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID
     var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var dataListener: ((ByteArray) -> Unit)? = null
    private var messageReceived : MessageReceived? =null
    companion object {

        @Volatile
        private var instance: BluetoothConnection? = null

        fun getInstance(): BluetoothConnection {
            return instance ?: synchronized(this) {
                instance ?: BluetoothConnection().also { instance = it }
            }
        }
    }

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
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream

            startListening() // Start listening for incoming data

            Log.d("Connected","Connected")
            Log.d("BluetoothConnection","$messageReceived")
            messageReceived?.connected()
            sendMessage("this is connected device ${device.address}")
            true
        } catch (e: Exception) {
            Log.d("BluetoothHelper", "Connection failed: $e")
            false
        }
    }

    // For server connection (Phone 2)
    @SuppressLint("MissingPermission")
    fun startServer(): Boolean {
                val adapter = BluetoothAdapter.getDefaultAdapter() ?: return false
                return try {
                    val serverSocket = adapter.listenUsingRfcommWithServiceRecord("MyBluetoothApp", uuid)
                    Log.d("BluetoothHelper", "Socket Accepting ${serverSocket}")
                    socket = serverSocket.accept() // Server accepts connection from client

                    // Set up streams for communication
                    inputStream = socket?.inputStream
                    outputStream = socket?.outputStream

                    Log.d("BluetoothHelper", "Connection accepted from: ${socket?.remoteDevice?.name}")
                    startListening() // Start listening for incoming data
                    Log.d("BluetoothConnection","$messageReceived")
            messageReceived?.connected()
            //serverSocket.close() // Close server socket after accepting
            true
        } catch (e: Exception) {
            Log.d("BluetoothHelper", "Server start failed: $e")
            false
        }
    }

    // Common function to send messages for both client and server
    fun sendMessage(message: String) {
        val data = message.toByteArray()
        try {
            outputStream?.write(data)
            Log.d("BluetoothHelper", "Sent: $message")
        } catch (e: Exception) {
            Log.d("BluetoothHelper", "Failed to send message: $e")
        }
    }

    // Start listening for incoming messages
    private fun startListening() {
        thread {
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (true) {
                try {
                    bytesRead = inputStream?.read(buffer) ?: -1
                    if (bytesRead == -1) {
                        Log.d("BluetoothHelper", "Connection lost")
                        break
                    }
                    val data = buffer.copyOf(bytesRead)
                    Log.d("BluetoothHelper", "Received: ${String(data)}")
                    messageReceived?.MessageReceived(String(data))
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
            inputStream?.close()
            outputStream?.close()
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
}
