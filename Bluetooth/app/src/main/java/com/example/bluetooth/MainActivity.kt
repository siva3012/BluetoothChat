package com.example.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.bluetooth.bluetooth.BluetoothServerService
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.bluetooth.ui.theme.DisplayViewModel
import com.example.bluetooth.ui.theme.MessageReceived
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.IntentFilter as IntentFilter1

class MainActivity : ComponentActivity(),MessageReceived {
    private val REQUEST_ENABLE_BT = 2
    private val PERMISSION_REQUEST_CODE = 1
    private var bluetoothAdapter: BluetoothAdapter? = null
    var selecteddevice by mutableStateOf<BluetoothDevice?>(null)
    var viewmodel: DisplayViewModel ? =null
    private lateinit var navController: NavHostController


    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    //  viewmodel.addvaluestoavailble(device)

                    // Log.d("Devicelist",devicelist.toString())
                       Log.d("BluetoothDiscovery", "Found device: ${device.address}")
                }
            }
        }
    }

    // private var deviceArrayAdapter: ArrayAdapter<String>? = null
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val serviceIntent = Intent(this, BluetoothServerService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            this.startForegroundService(serviceIntent)
//        } else {
//            this.startService(serviceIntent)
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            bluetoothConnection.startServer()
//        }
//        bluetoothConnection.setOnDataReceivedListener { data ->
//            Log.d("Received message from ", "${data}")
//        }
        viewmodel = DisplayViewModel(this)
        viewmodel?.registerMessageReceived(this)
        setContent {
            BluetoothTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    if (bluetoothAdapter == null) {
                        Log.d("Not supporetd", "Bluetooth is not supported in ur phone")
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val permissions = arrayOf(
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION  // Required for Bluetooth discovery
                        )
                        requestPermissions(permissions, PERMISSION_REQUEST_CODE)
                    }

                    Log.d("Permission Granted", (bluetoothAdapter?.isEnabled).toString())
                    if (bluetoothAdapter?.isEnabled == false) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    } else {
                        Log.d("Atactivity", "kjhjh")
                        registerReceiver(receiver, IntentFilter1(BluetoothDevice.ACTION_FOUND))
                        bluetoothAdapter!!.startDiscovery()
                        pairdevice()
                    }
                    navController = rememberNavController()
                    Navigation(navController = navController, viewmodel!!)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth has been enabled, start discovery
                Log.d("at", "jhj")
                pairdevice()
                registerReceiver(receiver, IntentFilter1(BluetoothDevice.ACTION_FOUND))
                bluetoothAdapter!!.startDiscovery()

            } else {
                // Bluetooth enabling was canceled or failed, handle accordingly
                // You may want to show a message or take appropriate action
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("Iam at resume", "Resume")
        // Register the receiver when the activity is resumed
        val filter = IntentFilter1(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        for ((key, value) in BluetoothServerService.getBluetoothServerServiceInstance().connectionMap) {
            Log.d("MapLog", "Key: $key, Value: ${value.socket.isConnected}")
            if (value.socket.isConnected && value.AddedThroughServer==false){
                value.socket.close()
                BluetoothServerService.getBluetoothServerServiceInstance().connectionMap.remove(key)
            }
        }
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

    @SuppressLint("MissingPermission")
    fun pairdevice() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            Log.w("device mac", "${device.address}")
            if (device.name != null) {
                viewmodel?.addvalues(device)
            }
        }

    }

    override fun MessageReceived(message: String,macAddress:String) {
        Log.d("MainActivity", "Received message from connection ")
        viewmodel?.ReceiveMessage(message)
    }

    override fun connected(macAddress: String,openScreen :Boolean) {
        viewmodel?.connected(macAddress)
        Log.d("MainActivity", "Device is Connected")
        if (openScreen) {
            CoroutineScope(Dispatchers.Main).launch {
                navController.navigate("ChatScreen")
            }
        }
    }


}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BluetoothTheme {
        Greeting("Android")
    }
}