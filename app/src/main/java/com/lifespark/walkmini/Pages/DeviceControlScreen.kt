package com.lifespark.walkmini.Pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.lifespark.walkmini.connectdevice.writeCommand
import com.lifesparktech.lsphysio.android.pages.PeripheralManager.mainScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Switch
import androidx.compose.ui.graphics.Color
import com.lifesparktech.lsphysio.android.pages.PeripheralManager
import com.lifesparktech.lsphysio.android.pages.PeripheralManager.Command

@Composable
fun DeviceControlScreen(navController: NavController){
    val toggleStates = remember { mutableStateListOf(*Array(7) { false }) }
    val magnitudes = remember { mutableStateListOf(*Array(7) { 1 }) }
//    var command by remember { mutableStateOf("1111111") }

    Column(
        modifier = Modifier.background(color = Color.White)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Mode Selection", fontSize = 20.sp, color = Color.Black, modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.Bold)

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(7) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Motor ${index + 1}", fontSize = 18.sp, color = Color.Black)
                    Switch(
                        checked = toggleStates[index],
                        onCheckedChange = { isChecked ->
                            toggleStates[index] = isChecked
                            sendBinaryCommand(toggleStates)
                            println("isChecked : $isChecked")
                            if (isChecked) {
                                sendMagnitudeCommand(index, 1)
                            }
                            else{
                                sendMagnitudeCommand(index, 0)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select Magnitude",
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..4).forEach { magnitude ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = magnitudes[index] == magnitude,
                                onClick = {
                                    magnitudes[index] = magnitude
                                    if (!toggleStates[index]) {
                                        toggleStates[index] = true
                                        sendBinaryCommand(toggleStates)
                                    }
                                    sendMagnitudeCommand(index, magnitude)
                                }
                            )
                            Text(text = magnitude.toString(), fontSize = 16.sp, color = Color.Black)
                        }
                    }
                }
            }
            item{
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
fun sendBinaryCommand(toggleStates: List<Boolean>) {
    val binaryString = toggleStates.joinToString("") { if (it) "1" else "0" }
    println("this is binaryString: $binaryString")
    mainScope.launch {
        writeCommand(binaryString)
    }
}
fun sendMagnitudeCommand(motorIndex: Int, magnitude: Int): String {
    Command = buildCommand(motorIndex, magnitude)
    println("this is command: $Command")
    mainScope.launch {
        writeCommand(Command)
    }
    return Command
}
fun buildCommand(motorIndex: Int, magnitude: Int): String {
    if (motorIndex !in 0..6) {
        throw IllegalArgumentException("Motor index must be between 0 and 6")
    }
    val validMagnitudes = listOf(0, 1, 2, 3, 4)
    if (magnitude !in validMagnitudes) {
        throw IllegalArgumentException("Magnitude must be one of: ${validMagnitudes.joinToString()}")
    }
    val commandArray = PeripheralManager.Command.toCharArray()
    commandArray[motorIndex] = ('0' + magnitude)
    println("this is commandArray: ${commandArray.toString()}")
    return String(commandArray)
}