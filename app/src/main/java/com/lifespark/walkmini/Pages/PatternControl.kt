package com.lifespark.walkmini.Pages

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.lifespark.walkmini.R
import com.lifespark.walkmini.connectdevice.getValued
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternControl(navController: NavController) {
    val initialItems = List(7) { "Motor ${it + 1}" }
    var items by remember { mutableStateOf(initialItems) }
    val magnitudes = remember { mutableStateListOf(*Array(items.size) { 1 }) }
    val timers = remember { mutableStateListOf<String>("", "", "", "", "", "", "") }
    val timersend = remember { mutableStateListOf<String>("", "", "", "", "", "", "") }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    val focusManager = LocalFocusManager.current
    var showResults by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var isRunning by remember { mutableStateOf(false) }
    var isLoopEntered by remember {mutableStateOf(false)}
    val interactionSource = remember { MutableInteractionSource() }
    var looptext by remember { mutableStateOf("") }
    val motorActivation = remember { mutableStateOf(CharArray(7) { '0' }) }
    val magnitudeString = remember { mutableStateOf(CharArray(7) { '0' }) }
    var showDialog by remember { mutableStateOf(false) }
    BackHandler {
        showDialog = true
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Exit") },
            text = { Text("Are you sure you want to go back?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    looptext = ""
                    items = initialItems
                    magnitudes.clear()
                    magnitudes.addAll(List(items.size) { 1 })
                    timers.clear()
                    timers.addAll(List(items.size) { "" })
                    timersend.clear()
                    timersend.addAll(List(items.size)  { "" })
                    isRunning = false
                    navController.popBackStack()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            items =  items.toMutableList().apply {
                if (from.index in indices && to.index in indices) {
                    add(to.index, removeAt(from.index))
                }
            }
            magnitudes.apply {
                if (from.index in indices && to.index in indices) {
                    add(to.index, removeAt(from.index))
                }
            }

            timers.apply {
                if (from.index in indices && to.index in indices) {
                    add(to.index, removeAt(from.index))
                }
            }

            timersend.apply {
                if (from.index in indices && to.index in indices) {
                    add(to.index, removeAt(from.index))
                }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("Set the pattern", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Button(
                        onClick = {
                            looptext = ""
                            items = initialItems
                            magnitudes.clear()
                            magnitudes.addAll(List(items.size) { 1 })
                            timers.clear()
                            timers.addAll(List(items.size) { "" })
                            timersend.clear()
                            timersend.addAll(List(items.size)  { "" })
                            isRunning = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF960019)
                        )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.restart),
                            contentDescription = "",
                            modifier = Modifier
                                .size(18.dp)
                        )
                    }
                }
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
                        if (newValue.matches(Regex("^\\d*\\.?\\d{0,2}\$")) && newValue.length <= 4) {
                            looptext = newValue
                            isLoopEntered = looptext.isNotEmpty()
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    colors =  TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFf2f4f5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    // readOnly = isLoopEntered,
                    placeholder = { Text("Seconds") },
                    modifier = Modifier
//                        .fillMaxWidth(0.45f)
                        .focusable(enabled = !isLoopEntered, interactionSource = interactionSource),
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
                                            magnitudes.removeAt(index)
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
                                                value = timers.getOrElse(index) { "" },
                                                onValueChange = { newValue ->
                                                    val loopMax = looptext.toFloatOrNull() ?: Float.MAX_VALUE
                                                    if (newValue.matches(Regex("^\\d*\\.?\\d{0,2}\$")) && newValue.length <= 4) {
                                                        if (newValue == "." || newValue == "") {
                                                            if (index in timers.indices) {
                                                                timers[index] = newValue // Allow "." but don't convert to Float
                                                            }
                                                        } else {
                                                            val newTimer = newValue.toFloatOrNull()
                                                            if (newTimer != null && newTimer in 0f..loopMax) {
                                                                if (index in timers.indices) {
                                                                    timers[index] = newValue
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                keyboardOptions = KeyboardOptions.Default.copy(
                                                    imeAction = ImeAction.Done,
                                                    keyboardType = KeyboardType.Number
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = { focusManager.clearFocus() }
                                                ),
                                                colors = TextFieldDefaults.textFieldColors(
                                                    containerColor = Color(0xFFf2f4f5),
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent
                                                ),
                                                placeholder = { Text("Seconds") },
                                                modifier = Modifier
                                                    .fillMaxWidth(0.45f)
                                                    .focusable(enabled = isLoopEntered, interactionSource = interactionSource),
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
                                                value = timersend.getOrElse(index) { "" },
                                                onValueChange = { newValue ->
                                                    val loopMax = looptext.toFloatOrNull() ?: Float.MAX_VALUE
                                                    if (newValue.matches(Regex("^\\d*\\.?\\d{0,2}\$")) && newValue.length <= 4) {
                                                        if (newValue == "." || newValue == "") {
                                                            if (index in timersend.indices) {
                                                                timersend[index] = newValue // Allow "." but don't convert to Float
                                                            }
                                                        } else {
                                                            val newTimer = newValue.toFloatOrNull()
                                                            if (newTimer != null && newTimer in 0f..loopMax) {
                                                                if (index in timersend.indices) {
                                                                    timersend[index] = newValue
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                //  readOnly = !isLoopEntered,
                                                keyboardOptions = KeyboardOptions.Default.copy(
                                                    imeAction = ImeAction.Done,
                                                    keyboardType = KeyboardType.Number
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = { focusManager.clearFocus() } // Remove focus when "Done" is pressed
                                                ),
                                                colors = TextFieldDefaults.textFieldColors(
                                                    containerColor = Color(0xFFf2f4f5),
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent
                                                ),
                                                placeholder = { Text("Seconds") },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .focusable(enabled = isLoopEntered, interactionSource = interactionSource),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }
                                val startTime = timers.getOrElse(index) { "0" }.toFloatOrNull() ?: 0f
                                val endTime = timersend.getOrElse(index) { "0" }.toFloatOrNull() ?: 0f
                                if (startTime > endTime) {
                                    isLoopEntered = false
                                    Text(
                                        text = "Error: Start time cannot be greater than end time",
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    isLoopEntered = true
                                }
                            }
                        }

                    }
                }
            }
            item{
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
                                mainScope.launch {
                                    isRunning = true
                                    showResults = true
                                    val sortedIndices = timers.indices.sortedBy { timers[it] }
                                    val sortedIndicesend = timersend.indices.sortedBy { timersend[it] }
                                    println("this sorted list: ${sortedIndices}")
                                    println("this sorted list end: ${sortedIndicesend}")
                                 //   var looptext = looptext.toFloat()
                                    val motorStartTimes =
                                        sortedIndices.associateWith { timers[it] }
                                    val motorEndTimes =
                                        sortedIndicesend.associateWith { timersend[it] }
                                    println("this motorStartTimes list: ${motorStartTimes}")
                                    println("this motorEndTimes list: ${motorEndTimes}")
                                    while (isRunning) {
                                        var time = 0.0

                                        val loopTime = looptext.toFloat()

                                        while (time < loopTime) {
                                            println("Time: ${"%.1f".format(time)}s")
                                            motorStartTimes.forEach { (index, start) ->
                                                if (time.toFloat() == start!!.toFloat()) { // Handling floating-point comparison
                                                    val motorIndex = items[index].split(" ").last().toIntOrNull()?.minus(1) ?: 0
                                                    val magnitude = magnitudes.getOrNull(index) ?: 1
                                                    magnitudeString.value[motorIndex] = magnitude.digitToChar()
                                                    val startCommand = String(magnitudeString.value)
                                                    println("${items[index]} starts")
                                                    println("Write command: $startCommand")
                                                    writeCommand(startCommand)
                                                }
                                            }

                                            motorEndTimes.forEach { (index, stop) ->
                                                if (time.toFloat() == stop!!.toFloat()) { // Handling floating-point comparison
                                                    val motorIndex = items[index].split(" ").last().toIntOrNull()?.minus(1) ?: 0
                                                    magnitudeString.value[motorIndex] = '0' // Reset motor to '0'
                                                    val stopCommand = String(magnitudeString.value)
                                                    println("${items[index]} stops")
                                                    println("Write stop command: $stopCommand")
                                                    writeCommand(stopCommand)
                                                }
                                            }
                                            delay(100)
                                            time += 0.1
                                        }
                                    }
                                }
                            }
                        },
                        enabled =
                            isLoopEntered &&
                                    items.isNotEmpty() &&
                                    timers.all { it.trim() != null && it.toString() != "." } &&
                                    timersend.all {
                                        val value = it.trim()
                                        value.isNotEmpty() && value != "." && value.toFloatOrNull() != null && value.toFloat() != 0f
                                    } &&
                                    looptext.trim().isNotEmpty() && looptext != "." && looptext.toFloatOrNull() != null && looptext.toFloat() != 0f,
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
//onValueChange = { newValue ->
//                                                    val loopMax = looptext.toFloatOrNull() ?: Float.MAX_VALUE
//                                                    if (newValue.matches(Regex("^\\d*\\.?\\d{0,2}\$")) && newValue.length <= 4) {
//                                                        val newTimer = newValue
//                                                       /// if (newTimer != null && newTimer in 0f..loopMax) {
//                                                      //      if (index in timers.indices) {
//                                                                timers[index] = newTimer
//                                                       ///     }
////                                                        } else if (newValue.isEmpty() && index in timers.indices) {
////                                                            timers[index] = 0f
////                                                        }
//                                                    }
//                                                },