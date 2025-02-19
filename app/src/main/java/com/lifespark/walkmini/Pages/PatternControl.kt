package com.lifespark.walkmini.Pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternControl() {
    val initialItems = List(7) { "Motor ${it + 1}" }
    var items by remember { mutableStateOf(initialItems) }
    val magnitudes = remember { mutableStateListOf(*Array(items.size) { 1 }) }
    val timers = remember { mutableStateListOf<Int?>(null, null, null, null, null, null, null) } // Initial timers set to null
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    val focusManager = LocalFocusManager.current // Manages focus
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            items = items.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            magnitudes.add(to.index, magnitudes.removeAt(from.index))
            if (from.index < timers.size && to.index < timers.size) {
                timers.add(to.index, timers.removeAt(from.index))
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
        Text("Set the pattern", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Ordered Items: ${items.joinToString()}", color = Color.Black)

        LazyColumn(
            modifier = Modifier
                .reorderable(reorderState)
                .detectReorderAfterLongPress(reorderState),
            state = reorderState.listState
        ) {
            itemsIndexed(items, key = { index, _ -> index }) { index, item ->
                val isSelected = item in selectedItems

                ReorderableItem(reorderState, key = index) { isDragging ->
                    val elevation = if (isDragging) 4.dp else 0.dp
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(if (isSelected) Color.Green.copy(alpha = 0.3f) else Color.LightGray),
                        shadowElevation = elevation
                    ) {
                        Column(modifier = Modifier.background(color = Color.White)) {
                            // ✅ Motor Item Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    modifier = Modifier.clickable {
                                        selectedItems = if (isSelected) {
                                            selectedItems - item
                                        } else {
                                            selectedItems + item
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                                Button(
                                    onClick = {
                                        items = items.filter { it != item }
                                        if (index < timers.size) timers.removeAt(index)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Remove", color = Color.White)
                                }
                            }

                            // ✅ Select Magnitude
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
                                            onClick = { magnitudes[index] = magnitude }
                                        )
                                        Text(text = magnitude.toString(), fontSize = 16.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                // ✅ Timer Section (Between Motors) - Using TextField
                if (index < items.size - 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Delay:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = timers.getOrElse(index) { null }?.toString() ?: "",
                            onValueChange = { newValue ->
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
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Number
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() } // Remove focus when "Done" is pressed
                            ),
                            label = { Text("Seconds") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}