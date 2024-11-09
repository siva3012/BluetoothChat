package com.example.bluetooth.bluetooth

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothServerService : Service() {
    val bluetoothConnection =BluetoothConnection.getInstance()

    override fun onCreate() {
        super.onCreate()
        Log.d("MyService", "Service Created")
        startForegroundServiceWithNotification()
        startBluetoothServer()
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundServiceWithNotification() {
        val channelId = "BluetoothServerServiceChannel"
        val channelName = "Bluetooth Server Service Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Bluetooth Server Running")
            .setContentText("Listening for incoming connections...")
            .build()

        startForeground(1, notification,FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startBluetoothServer()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
     //  bluetoothConnection.socket?.close()
        Log.d("BluetoothServerService", "Bluetooth Server Stopped")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    fun startBluetoothServer(){
        CoroutineScope(Dispatchers.IO).launch {
            bluetoothConnection.startServer()
        }
        bluetoothConnection.setOnDataReceivedListener { data ->
            Log.d("Received message from ", "${data}")
        }
    }
}
