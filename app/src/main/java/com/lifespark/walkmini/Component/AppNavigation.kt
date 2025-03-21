package com.lifespark.walkmini.Component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifespark.walkmini.Pages.DeviceControlScreen
import com.lifespark.walkmini.Pages.ModeDevice
import com.lifespark.walkmini.Pages.NewDeviceControlScreen
import com.lifespark.walkmini.Pages.PatternControl
import com.lifesparktech.lsphysio.android.pages.DeviceConnectionScreen
import com.lifesparktech.lsphysio.android.pages.PeripheralManager

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "device_connection") {
        composable("device_connection") {
            if (PeripheralManager.peripheral != null) {
                LaunchedEffect(Unit) {
                    navController.navigate("ModeDevice") {
                        popUpTo("device_connection")  { inclusive = true }
                    }
                }
            }
            else{
                DeviceConnectionScreen(navController)
            }

        }
        composable("DeviceControlScreen") {
            DeviceControlScreen()
        }
        composable("newDeviceControlScreen") {
            NewDeviceControlScreen()
        }

        composable("ModeDevice") {
            ModeDevice(navController)
        }
        composable("PatternDevice") {
            PatternControl(navController)
        }
    }
}