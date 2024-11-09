package com.example.bluetooth

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bluetooth.ui.theme.ChatScreen
import com.example.bluetooth.ui.theme.DisplayViewModel
import com.example.bluetooth.ui.theme.PairingScreen

@Composable
fun Navigation(navController :NavHostController,viewModel: DisplayViewModel) {
    Log.w("Navigation","I am at navigation")
    NavHost(navController = navController, startDestination = "PairingScreen" ){
        composable(route="PairingScreen"){
            PairingScreen(viewModel)
        }
        composable(route="ChatScreen"){
            ChatScreen(viewModel)
        }
    }
}