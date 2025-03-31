package com.lifespark.walkmini.Pages

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.lifespark.walkmini.Controller.fetchPatients
import com.lifespark.walkmini.Data.Patient
import com.lifespark.walkmini.R
import com.lifespark.walkmini.connectdevice.getValued
import com.lifesparktech.lsphysio.android.pages.PeripheralManager
import kotlinx.coroutines.delay
import com.google.firebase.firestore.FirebaseFirestore
import com.lifespark.walkmini.Controller.Pattern
import com.lifesparktech.lsphysio.android.pages.PeripheralManager.charRead
import com.lifesparktech.lsphysio.android.pages.PeripheralManager.charWrite
import com.lifesparktech.lsphysio.android.pages.PeripheralManager.peripheral
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
    var expanded by remember { mutableStateOf(false) }
    var expandedpattern by remember { mutableStateOf(false) }
    var patient by remember { mutableStateOf(Patient()) }
    var pattern by remember { mutableStateOf(Pattern()) }
    val patients = remember { mutableStateOf<List<Patient>>(emptyList()) }
    var selectedOption by remember { mutableStateOf("") }
    var selectedOptionpattern by remember { mutableStateOf("") }
    var nameofMode by remember { mutableStateOf("") }
    var patterns = remember { mutableStateOf<List<Pattern>>(emptyList()) }
    var context = LocalContext.current
    var BandStatus by remember { mutableStateOf("no Status") }
    var BandStatusBool by remember { mutableStateOf(false) }
    BackHandler {
        showDialog = true
    }
    val db = FirebaseFirestore.getInstance()
    fun fetchPatterns(patientId: String, onPatternsFetched: (List<Pattern>) -> Unit) {
        db.collection("patients").document(patientId)
            .collection("patterns")
            .orderBy("timestamp") // Orders by time if needed
            .get()
            .addOnSuccessListener { documents ->
                val patterns = documents.map { doc ->
                    Pattern(
                        motors = doc["motors"] as List<Map<String, Any>>,
                        loopTime = doc["loopTime"] as String,
                        nameofMode = doc["nameofMode"] as? String ?: "",
                        timestamp = doc["timestamp"]
                    )
                }
                onPatternsFetched(patterns) // Pass fetched data to the callback
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching patterns: ${e.message}")
            }
    }
    fun savePattern() {
        val patientId = patient.id // Assuming your Patient object has an 'id' field
        val patientName = patient.name
        val motorsList = items.mapIndexed { index, motor ->
            mapOf(
                "motorName" to motor,
                "startTime" to timers[index],
                "endTime" to timersend[index],
                "magnitude" to magnitudes[index]
            )
        }

        val patternData = mapOf(
            "motors" to motorsList,
            "loopTime" to looptext,
            "nameofMode" to nameofMode,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("patients").document(patientId)
            .collection("patterns")
            .add(patternData)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Pattern saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                println("Error saving pattern: ${e.message}")
            }
        if (patient.id.isNotEmpty()) { // Ensure patient ID is valid before fetching patterns
            fetchPatterns(patient.id) { fetchedPatterns ->
                patterns.value = fetchedPatterns
            }
        }
    }
    fun trackDeviceStatus() {
        peripheral?.state?.onEach { state ->
            println("Band State: $state")
            BandStatus = state.toString()
            if(BandStatus == "Disconnected(Timeout)"){
                BandStatusBool = true
                peripheral = null
                charWrite = null
                charRead = null
                navController.navigate("device_connection"){
                    popUpTo(0) { inclusive = true } // Pops all destinations
                }
            }
        }?.launchIn(mainScope)
    }
    LaunchedEffect(patients.value, patient.id) {
        trackDeviceStatus()
        if (patients.value.isEmpty()) {
            patients.value = fetchPatients()
        }
        if (patient.id.isNotEmpty()) { // Ensure patient ID is valid before fetching patterns
            fetchPatterns(patient.id) { fetchedPatterns ->
                patterns.value = fetchedPatterns
            }
        }
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
                            nameofMode = ""
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
            item{
                Card(
                    Modifier.fillMaxWidth().padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp)
                    ){
                        Column(
                        ) {
                            Text(text = "Enter a patient", style = TextStyle(fontSize = 16.sp), fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedOption,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    colors =  TextFieldDefaults.textFieldColors(
                                        containerColor = Color(0xffEBEBEB),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth( if(selectedOption.isNotEmpty()){  0.5f  } else { 1f } )
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(color = Color(0xFFf2f4f5))
                                ) {
                                    patients.value.forEach{option ->
                                        DropdownMenuItem(
                                            text = { Text(option.name) },
                                            onClick = {
                                                patient = option
                                                selectedOption = option.name
                                                expanded = false
                                            }
                                        )

                                    }
                                }
                            }

                        }
                        if(selectedOption.isNotEmpty()){
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                            ) {
                                Text(text = "Enter a pattern", style = TextStyle(fontSize = 16.sp), fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                ExposedDropdownMenuBox(
                                    expanded = expandedpattern,
                                    onExpandedChange = { expandedpattern = !expandedpattern }
                                ) {
                                    OutlinedTextField(
                                        value = selectedOptionpattern,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedpattern)
                                        },
                                        colors =  TextFieldDefaults.textFieldColors(
                                            containerColor = Color(0xffEBEBEB),
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedpattern,
                                        onDismissRequest = { expandedpattern = false },
                                        modifier = Modifier.background(color = Color(0xFFf2f4f5))
                                    ) {
                                        patterns.value.forEach{option ->
                                            DropdownMenuItem(
                                                text = { Text(option.nameofMode) },
                                                onClick = {
                                                    pattern = option
                                                    selectedOptionpattern = option.nameofMode
                                                    expandedpattern = false
                                                    looptext = option.loopTime
                                                    nameofMode = option.nameofMode
                                                    items = option.motors.map { it["motorName"] as? String ?: "Unknown Motor" }
                                                    magnitudes.clear()
                                                    magnitudes.addAll(option.motors.map {
                                                        when (val magnitudeValue = it["magnitude"]) {
                                                            is Long -> magnitudeValue.toInt() // Convert Long to Int
                                                            is Int -> magnitudeValue
                                                            else -> 1 // Default if missing
                                                        }
                                                    })
                                                    timers.clear()
                                                    timers.addAll(option.motors.map { (it["startTime"] as? String) ?: "" })
                                                    timersend.clear()
                                                    timersend.addAll(option.motors.map { (it["endTime"] as? String) ?: "" })
                                                }
                                            )

                                        }
                                    }
                                }
                            } }
                    }
                }
            }

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
                                mainScope.launch{
                                    isRunning = false
                                    val startCommand = "0000000"
                                    writeCommand(startCommand)
                                }
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
                                    val loopTime = looptext.toFloat()

                                    while (isRunning) { // Keep looping until stopped manually
                                        var time = 0.0 // Reset time at the beginning of each cycle

                                        while (time < loopTime && isRunning) {
                                            println("Time: ${"%.1f".format(time)}s")

                                            // Start motors at their scheduled times
                                            motorStartTimes.forEach { (index, start) ->
                                                if (time.toFloat() == start!!.toFloat()) {
                                                    val motorIndex = items[index].split(" ").last().toIntOrNull()?.minus(1) ?: 0
                                                    val magnitude = magnitudes.getOrNull(index) ?: 1
                                                    magnitudeString.value[motorIndex] = magnitude.digitToChar()
                                                    val startCommand = String(magnitudeString.value)
                                                    println("${items[index]} starts")
                                                    writeCommand(startCommand)
                                                }
                                            }

                                            // Stop motors at their scheduled stop times
                                            motorEndTimes.forEach { (index, stop) ->
                                                if (time.toFloat() == stop!!.toFloat()) {
                                                    val motorIndex = items[index].split(" ").last().toIntOrNull()?.minus(1) ?: 0
                                                    magnitudeString.value[motorIndex] = '0' // Reset motor to '0'
                                                    val stopCommand = String(magnitudeString.value)
                                                    println("${items[index]} stops")
                                                    writeCommand(stopCommand)
                                                }
                                            }

                                            delay(100) // Wait for 100ms before incrementing time
                                            time += 0.1
                                        }

                                        println("Loop cycle completed. Restarting time from 0.")

                                        // Reset time to restart the loop
                                        time = 0.0
                                    }
                                         // Ensure loop stops immediately when button is clicked
                                   // }
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
                            containerColor = if (isRunning) Color(0xFF960019) else Color(0xFF005749), // Normal state color
                            contentColor = Color.White, // Normal text color
                            disabledContainerColor = Color(0xFFCCCCCC), // Background color when disabled
                            disabledContentColor = Color.Gray // Text color when disabled
                        ),
                    ) {
                        Text(if (isRunning) "Stop" else "Start", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            item{
                Card(
                    Modifier.fillMaxWidth().padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "Mode:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedTextField(
                            value = nameofMode,
                            onValueChange = { nameofMode = it },
                            singleLine = true,
                            placeholder = { Text("Enter Mode") },
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color(0xFFf2f4f5),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            item{
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Button(
                        onClick = {
                            if (selectedOption.isEmpty() || nameofMode.isEmpty()) {
                                val message = if (selectedOption.isEmpty()) "Please select a patient" else "Please Enter the mode name"
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            savePattern()
                        },
                        modifier = Modifier.fillMaxWidth(0.925f).height(45.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color(0xFF005749), // Normal state color
                            contentColor = Color.White, // Normal text color
                            disabledContainerColor = Color(0xFFCCCCCC), // Background color when disabled
                            disabledContentColor = Color.Gray // Text color when disabled
                        ),
                    ) {
                        Text("Save the Pattern", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

/*
1. number of motor
    start and end time
    it's magnitude.
2. time of loop.
with patient name
 */