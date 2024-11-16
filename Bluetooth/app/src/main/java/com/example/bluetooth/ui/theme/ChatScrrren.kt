package com.example.bluetooth.ui.theme

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@SuppressLint("MissingPermission")
@Composable
fun ChatScreen(displayViewModel: DisplayViewModel){
    val sent = remember {
        mutableStateOf("")
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(color = Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Green)
                .padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
        ) {
            Row {
                Text(
                    text = "Back"
                )
                Spacer(Modifier.weight(0.5f))
                Text(
                    text = "Bluetooth Chat with ${displayViewModel.connectedDeviceAddress.value}",
                    style = TextStyle(textAlign = TextAlign.Center)
                )
            }
        }
        LazyColumn{
            items(displayViewModel.chatData.value){
                Log.d("ChatScreen","${it.message}")
                if (it.MessageType==messageType.Received){
                    Box(modifier = Modifier
                        .background(color = Color.White)
                        .fillMaxWidth().padding(10.dp) ){
                        Text(text = it.message)
                    }
                }
               else if (it.MessageType==messageType.Sent){
                    Box(modifier = Modifier
                        .background(color = Color.Green)
                        .fillMaxWidth() ){
                        Row {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = it.message)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier= Modifier.padding(10.dp)) {
            TextField(value = sent.value , onValueChange = {sent.value=it}, modifier = Modifier.weight(1f))
            Button(onClick = {
                displayViewModel.sendMessageAddToDB(sent.value)
            }) {
                Text(text = "Send")
            }
        }
    }

}
