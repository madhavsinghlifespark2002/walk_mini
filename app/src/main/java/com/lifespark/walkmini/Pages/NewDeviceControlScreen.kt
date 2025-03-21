package com.lifespark.walkmini.Pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.lifespark.walkmini.connectdevice.getValued
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NewDeviceControlScreen(){
    val isOnList = remember { mutableStateOf(List(7) { false }) }
    var selectedMag by remember { mutableStateOf(-1) }
    val colors = listOf("1", "2", "3", "4")
    val colorsDetails = listOf("70\n0.3g", "100\n0.6g", "140\n0.9g", "180\n1.2g")
    var magnitudes = remember { mutableStateListOf(*Array(7) { 1 }) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedTabMag by remember { mutableStateOf(0) }
    val tabTitles = listOf("1", "2", "3", "4", "5", "6", "7")
    var scope = rememberCoroutineScope()
    var enableAll by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val newValues = getValued()
        if (!newValues.isNullOrEmpty()) {
            selectedTabMag = newValues?.get(0) ?: 0
            selectedMag = if (selectedTabMag in 1..4) selectedTabMag - 1 else -1
            println(newValues)
            newValues.forEachIndexed { i, value ->
                magnitudes[i] = if (value == 0) 1 else value
            }
            isOnList.value = newValues.map { it != 0 }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize().padding(12.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(350.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE5E9E1)
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row {
                                BoxWithNumber(0, isOnList, selectedTabIndex, magnitudes, selectedTabMag, selectedMag, scope) { selectedMag = it }
                                Spacer(modifier = Modifier.width(12.dp))
                                BoxWithNumber(1, isOnList, selectedTabIndex, magnitudes, selectedTabMag, selectedMag, scope) { selectedMag = it }
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                BoxWithNumber(2, isOnList, selectedTabIndex, magnitudes, selectedTabMag, selectedMag, scope, isRectangle = true) { selectedMag = it }
                                Spacer(modifier = Modifier.height(12.dp))
                                BoxWithNumber(3, isOnList, selectedTabIndex, magnitudes, selectedTabMag, selectedMag, scope, isRectangle = true) { selectedMag = it }
                                Spacer(modifier = Modifier.height(12.dp))
                                BoxWithNumber(4, isOnList, selectedTabIndex, magnitudes, selectedTabMag, selectedMag, scope, isRectangle = true) { selectedMag = it }
                            }
                            Row {
                                Spacer(modifier = Modifier.width(12.dp))
                                BoxWithNumber(5, isOnList, selectedTabIndex, magnitudes, selectedTabMag, selectedMag, scope) { selectedMag = it }
                                Spacer(modifier = Modifier.width(12.dp))
                                BoxWithNumber(6, isOnList, selectedTabIndex, magnitudes, selectedTabMag, selectedMag, scope) { selectedMag = it }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        enableAll = !enableAll
                        val enableallBooleans = List(7) { enableAll }
                        //val enableallBooleans = enableall.map { it == 1 }
                        isOnList.value = enableallBooleans
                        sendBinaryCommand(enableallBooleans)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (enableAll || isOnList.value.all { it }) Color(0xff7c0a02) else Color(0xff105749),
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(200.dp),

                    ) {
                    Text(if (enableAll || isOnList.value.all { it }) "Disable All" else "Enable All", textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Text(text = "Magnitude", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xff105749)
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                                scope.launch{
                                    var magn = getValued()
                                    selectedTabMag = magn?.get(index) ?: 0
                                    selectedMag = if (selectedTabMag in 1..4) selectedTabMag - 1 else -1
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTabIndex == index) Color(0xff105749) else Color.Gray
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    colors.zip(colorsDetails).forEachIndexed { index, (text, details) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedMag == index) Color(0xff105749) else Color.Transparent)
                                    .border(2.dp, if (selectedMag == index) Color.Transparent else Color(0xff105749), CircleShape)
                                    .clickable {
                                        selectedMag = index
                                        isOnList.value = isOnList.value.toMutableList().apply {
                                            this[selectedTabIndex] = true
                                        }
                                        sendMagnitudeCommand(selectedTabIndex, index + 1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = text,
                                    color = if (selectedMag == index) Color.White else Color(0xff105749)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp)) // Spacing between box and text
                            Text(text = details, textAlign = TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.width(12.dp)) // Space between items
                    }
                }
            }
        }
    }
}


@Composable
fun BoxWithNumber(
    index: Int,
    isOnList: MutableState<List<Boolean>>,
    selectedTabIndex: Int,
    magnitudes: List<Int>,
    selectedTabMag: Int,
    selectedMag: Int,
    scope: CoroutineScope,
    isRectangle: Boolean = false,
    onSelectedMagChange: (Int) -> Unit
) {
    val shape = if (isRectangle) RectangleShape else CircleShape
    val sizeModifier = if (isRectangle) Modifier.size(width = 75.dp, height = 45.dp) else Modifier.size(50.dp)
    Box(
        modifier = sizeModifier
            .clip(shape)
            .background(if (isOnList.value[index]) Color(0xff105749) else Color.Transparent)
            .border(
                4.dp,
                if (selectedTabIndex == index) Color.Red else if (isOnList.value[index]) Color.Transparent else Color(0xff105749),
                shape
            )
            .clickable {
                val newList = isOnList.value.toMutableList()
                newList[index] = !newList[index]
                isOnList.value = newList
                if (isOnList.value[index]) {
                    sendMagnitudeCommand(index, magnitudes[index])
                    onSelectedMagChange(0)
                } else {
                    sendMagnitudeCommand(index, 0)
                    onSelectedMagChange(-1)
                }

            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = (index + 1).toString(),
            color = if (isOnList.value[index]) Color.White else Color(0xff105749)
        )
    }
}
// 1. on tab change related should be shown mag should show: done.
// 2.