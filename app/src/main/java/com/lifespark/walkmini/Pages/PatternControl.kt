package com.lifespark.walkmini.Pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifespark.walkmini.connectdevice.writeCommand
import com.lifesparktech.lsphysio.android.pages.PeripheralManager.mainScope
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.res.painterResource
import com.lifespark.walkmini.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternControl() {
    val initialItems = List(7) { "Motor ${it + 1}" }
    var items by remember { mutableStateOf(initialItems) }
    val magnitudes = remember { mutableStateListOf(*Array(items.size) { 1 }) }
    val timers = remember { mutableStateListOf<Int?>(null, null, null, null, null, null, null) } // Initial timers set to null
    val timersend = remember { mutableStateListOf<Int?>(null, null, null, null, null, null, null) } // Initial timers set to null
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    val focusManager = LocalFocusManager.current
    var showResults by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var isRunning by remember { mutableStateOf(false) }
    var isLoopEntered by remember {mutableStateOf(false)}
    val interactionSource = remember { MutableInteractionSource() }
    var looptext by remember { mutableStateOf("") }
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            items = items.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            magnitudes.add(to.index, magnitudes.removeAt(from.index))
            if (from.index < timers.size && to.index < timers.size && from.index < timersend.size && to.index < timersend.size) {
                timers.add(to.index, timers.removeAt(from.index))
                timersend.add(to.index, timersend.removeAt(from.index))
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xfff4f4f4))
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(12.dp)
            ){
                Text("Set the pattern", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Ordered Items:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${items.joinToString()}", color = Color.Black)
            }
        }
        Card(
            Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(text = "Set the Loop Time:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = looptext,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                            looptext = newValue
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            isLoopEntered = true} // Remove focus when "Done" is pressed
                    ),
                    colors =  TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFf2f4f5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    readOnly = isLoopEntered,
                    placeholder = { Text("Seconds") },
                    modifier = Modifier
//                        .fillMaxWidth(0.45f)
                        .focusable(enabled = !isLoopEntered, interactionSource = interactionSource), // Prevent focus when readOnly
                    singleLine = true
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .reorderable(reorderState)
                .imePadding()
                .detectReorderAfterLongPress(reorderState),
            state = reorderState.listState
        ) {
            itemsIndexed(items, key = { index, _ -> index }) { index, item ->
                val isSelected = item in selectedItems

                ReorderableItem(reorderState, key = index) { isDragging ->
                    val elevation = if (isDragging) 8.dp else 4.dp
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .background(if (isSelected) Color.Green.copy(alpha = 0.3f) else Color.Transparent),
                        shadowElevation = elevation,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ){
                            Column(Modifier.padding(12.dp),) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black
                                    )
                                    Button(
                                        onClick = {
                                            items = items.filter { it != item }
                                            if (index < timers.size) timers.removeAt(index)
                                            if (index < timersend.size) timersend.removeAt(index)
                                            magnitudes[index] = 0
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF960019)
                                        )
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.delete),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Select Magnitude",
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    (1..4).forEach { magnitude ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = magnitudes[index] == magnitude,
                                                onClick = { magnitudes[index] = magnitude },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = Color(0xFF005749)
                                                )
                                            )
                                            Text(text = magnitude.toString(), fontSize = 16.sp, color = Color.Black)
                                        }
                                    }
                                }
                                if (index < items.size) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                       // horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column{
                                            Text(text = "Start", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = timers.getOrElse(index) { null }?.toString() ?: "",
                                                onValueChange = { newValue ->
                                                    if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                                        val newTimer = newValue.toIntOrNull()
                                                        if (newTimer != null && newTimer > 0) {
                                                            if (index < timers.size) {
                                                                timers[index] = newTimer
                                                            } else {
                                                                while (timers.size <= index) {
                                                                    timers.add(null) // Fill missing indices with null values
                                                                }
                                                                timers[index] = newTimer
                                                            }
                                                        } else if (newValue.isEmpty()) {
                                                            timers[index] = null // Allow clearing the timer
                                                        }
                                                    }
                                                },
                                                readOnly = !isLoopEntered,
                                                keyboardOptions = KeyboardOptions.Default.copy(
                                                    imeAction = ImeAction.Done,
                                                    keyboardType = KeyboardType.Number
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = { focusManager.clearFocus() } // Remove focus when "Done" is pressed
                                                ),
                                                colors =  TextFieldDefaults.textFieldColors(
                                                    containerColor = Color(0xFFf2f4f5),
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent
                                                ),
                                                placeholder = { Text("Seconds") },
                                                modifier = Modifier
                                                    .fillMaxWidth(0.45f)
                                                    .focusable(enabled = isLoopEntered, interactionSource = interactionSource), // Prevent focus when readOnly
                                                singleLine = true
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column{
                                            Text(
                                                text = "Stop",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = timersend.getOrElse(index) { null }?.toString() ?: "",
                                                onValueChange = { newValue ->
                                                    if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                                        val newTimer = newValue.toIntOrNull()
                                                        if (newTimer != null && newTimer > 0) {
                                                            if (index < timersend.size) {
                                                                timersend[index] = newTimer
                                                            } else {
                                                                while (timersend.size <= index) {
                                                                    timersend.add(null) // Fill missing indices with null values
                                                                }
                                                                timersend[index] = newTimer
                                                            }
                                                        } else if (newValue.isEmpty()) {
                                                            timersend[index] = null // Allow clearing the timer
                                                        }
                                                    }
                                                },
                                                readOnly = !isLoopEntered,
                                                keyboardOptions = KeyboardOptions.Default.copy(
                                                    imeAction = ImeAction.Done,
                                                    keyboardType = KeyboardType.Number
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = { focusManager.clearFocus() } // Remove focus when "Done" is pressed
                                                ),
                                                colors =  TextFieldDefaults.textFieldColors(
                                                    containerColor = Color(0xFFf2f4f5),
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent
                                                ),
                                                placeholder = { Text("Seconds") },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .focusable(enabled = isLoopEntered, interactionSource = interactionSource), // Prevent focus when readOnly
                                                singleLine = true
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                }


            }
            item{
                if (showResults) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Ordered Pattern:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    results.forEach { result ->
                        Text(
                            result,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Button(
                        onClick = {
                            if (isRunning) {
                                isRunning = false // Stop the loop
                            } else {
                                isRunning = true
                                showResults = true
                                val commandList = mutableListOf<String>()
                                results = items.mapIndexed { index, motor ->
                                    val magnitude = magnitudes.getOrNull(index) ?: 1
                                    val delay = timers.getOrNull(index) ?: 0
                                    val motorIndex = motor.split(" ").last().toIntOrNull()?.minus(1) ?: return@mapIndexed ""
                                    val motorActivation = CharArray(7) { '0' }
                                    motorActivation[motorIndex] = '1'
                                    val magnitudeString = CharArray(7) { '0' }
                                    magnitudeString[motorIndex] = magnitude.digitToChar()
                                    commandList.add(String(motorActivation))
                                    commandList.add(String(magnitudeString))
                                    if (delay > 0) {
                                        commandList.add("WAIT $delay")
                                    }
                                    "$motor - $magnitude mag.\n$delay second${if (delay == 1) "" else "s"}"
                                }
                                mainScope.launch {
                                    while (isRunning) { // Loop until stopped
                                        for (command in commandList) {
                                            if (!isRunning) break // Exit if stopped
                                            if (command.startsWith("WAIT")) {
                                                val delayTime = command.split(" ")[1].toLongOrNull()
                                                if (delayTime != null) {
                                                    kotlinx.coroutines.delay(delayTime * 1000) // Just wait, don't send "WAIT"
                                                }
                                            } else {
                                                println("Sending command: $command")
                                                writeCommand(command)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        enabled =  isLoopEntered,
                        modifier = Modifier.fillMaxWidth(0.925f).height(45.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color(0xFF005749), // Normal state color
                            contentColor = Color.White, // Normal text color
                            disabledContainerColor = Color(0xFFCCCCCC), // Background color when disabled
                            disabledContentColor = Color.Gray // Text color when disabled
                        ),
                    ) {
                        Text(if (isRunning) "Stop" else "Start", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}