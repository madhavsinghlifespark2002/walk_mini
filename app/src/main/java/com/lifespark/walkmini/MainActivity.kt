package com.lifespark.walkmini

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifespark.walkmini.Pages.DeviceControlScreen
import com.lifespark.walkmini.Pages.ModeDevice
import com.lifespark.walkmini.Pages.PatternControl
import com.lifespark.walkmini.ui.theme.WalkminiTheme
import com.lifesparktech.lsphysio.android.pages.DeviceConnectionScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == RESULT_OK) {
            println("Bluetooth enabled successfully")
        } else {
            println("Bluetooth enabling failed")
            requestBluetoothPermissions()
        }
    }
    private val enableLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            println("Location enabled successfully")
        } else {
            println("Location enabling failed")
            requestLocationPermissions()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestBluetoothPermissions()
        requestLocationPermissions()
        setContent {
           // WalkminiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation()
                }
           // }
        }
    }
    internal fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Check if BLUETOOTH_CONNECT and BLUETOOTH_SCAN permissions are granted
            val permissionsToRequest = mutableListOf<String>()
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_SCAN)
            }

            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), BLUETOOTH_PERMISSION_REQUEST_CODE)
            } else {
                enableBluetoothAndScan()
            }
        } else {
            enableBluetoothAndScan()
        }
    }
    internal fun enableBluetoothAndScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Bluetooth is not available on the device
            println("Bluetooth not supported on this device.")
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            // Bluetooth is disabled, request enabling
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            // Bluetooth is already enabled, proceed with scanning or other actions
            println("Bluetooth is already enabled.")
        }
    }
    internal fun requestLocationPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            enableLocation()
        }
    }
    @SuppressLint("ServiceCast")
    private fun enableLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            enableLocationLauncher.launch(intent)
        }
    }
    companion object {
        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 1
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "device_connection") {
        composable("device_connection") {
            DeviceConnectionScreen(navController)
        }
        composable("DeviceControlScreen") {
            DeviceControlScreen(navController)
        }
        composable("ModeDevice") {
            ModeDevice(navController)
        }
        composable("PatternDevice") {
            PatternControl()
        }
    }
}
