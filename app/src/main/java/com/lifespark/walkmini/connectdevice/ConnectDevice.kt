package com.lifespark.walkmini.connectdevice

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Advertisement
import com.juul.kable.AndroidPeripheral
import com.juul.kable.ConnectionLostException
import com.juul.kable.Filter
import com.juul.kable.Scanner
import com.juul.kable.peripheral
import com.lifesparktech.lsphysio.android.pages.PeripheralManager
import com.lifesparktech.lsphysio.android.pages.PeripheralManager.mainScope
import com.lifesparktech.lsphysio.android.pages.PeripheralManager.peripheral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

suspend fun writeCommand(command: String) {
    val peripheral = peripheral
    val charWrite = PeripheralManager.charWrite

    if (peripheral != null && charWrite != null) {
        try {
            peripheral.write(charWrite, command.encodeToByteArray())
            println("Command sent: $command")
        } catch (e: Exception) {
            println("Error writing command: ${e.message}")
        }
    } else {
        println("Peripheral or characteristic not initialized.")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun scanBluetoothDevices(context: Context, scope: CoroutineScope, onDevicesFound: (List<Advertisement>) -> Unit) {
    val scanner = Scanner {
        filters = listOf(Filter.Service(uuidFrom("0000acf0-0000-1000-8000-00805f9b34fb"))) // Example UUID
    }
    val devices = mutableListOf<Advertisement>()
    val uniqueAddresses = mutableSetOf<String>() // Track unique device addresses

    scope.launch {
        try {
            withTimeout(10_000) { // Scan for 10 seconds
                scanner.advertisements.collect { advertisement ->
                    if (uniqueAddresses.add(advertisement.address)) {
                        println("Found device: ${advertisement.name ?: "Unknown"} - ${advertisement.address}")
                        devices.add(advertisement)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            println("Scanning finished after 10 seconds.")
        } catch (e: Exception) {
            println("Error during scanning: ${e.message}")
            Toast.makeText(context, "Error during scanning: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            onDevicesFound(devices)
        }
    }
}
fun ConnectDeviced(
    context: Context,
    navController: NavController,
    deviceName: Advertisement
) {
    mainScope.launch {
        try {
            val advertisement = deviceName
            val peripheral = mainScope.peripheral(advertisement)
            peripheral.connect()
            val androidPeripheral = peripheral as AndroidPeripheral
            val service = peripheral.services?.find {
                it.serviceUuid == uuidFrom("0000abf0-0000-1000-8000-00805f9b34fb")
            } ?: throw Exception("Service not found for device")
            var charRead = service.characteristics.find {
                it.characteristicUuid == uuidFrom("0000abf1-0000-1000-8000-00805f9b34fb")
            } ?: throw Exception("Read characteristic not found")

            val charWrite = service.characteristics.find {
                it.characteristicUuid == uuidFrom("0000abf1-0000-1000-8000-00805f9b34fb")
            } ?: throw Exception("Write characteristic not found")
            androidPeripheral.requestMtu(512)
            PeripheralManager.peripheral = peripheral
            PeripheralManager.charWrite = charWrite
            PeripheralManager.charRead = charRead
            val peripheralcon = PeripheralManager.peripheral
            val charWritecon = PeripheralManager.charWrite
            println("peripheralcon value: ${peripheralcon?.name} and charWritecon value: ${charWritecon?.serviceUuid}")
            navController.navigate("ModeDevice")
        } catch (e: ConnectionLostException) {
            println("Connection lost: ${e.message}")
            Toast.makeText(context, "Failed to connect", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            println("Error: ${e.message}")
            Toast.makeText(context, "Failed to connect", Toast.LENGTH_LONG).show()
        } finally {
            println("Cleaning up resources.")
        }
    }
}