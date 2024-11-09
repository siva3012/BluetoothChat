package com.example.bluetooth.ui.theme

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("MissingPermission")
@Composable
fun PairingScreen(viewModel: DisplayViewModel){
    Log.i("pairingScreen","pair")
    val list = viewModel.uistate.collectAsState().value
    Log.d("PAiring","${list.size}")
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(color = Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Green)
                .padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Connect", style = TextStyle(textAlign = TextAlign.Center))
        }
        LazyColumn{
            items(list){
                Button(onClick = { viewModel.connectToDevice(it) }) {
                    Text(text = it.address)
                }
                Log.d("pair", it.name)
                if (it.name!=null){
                    Text(text = it.name)
                }
            }
        }
    }
}
