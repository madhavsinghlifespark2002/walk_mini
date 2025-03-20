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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.lifespark.walkmini.connectdevice.getValued

@Composable
fun NewDeviceControlScreen(){
    val isOnList = remember { mutableStateListOf(false, false, false, false, false, false, false) }
    var selectedMag by remember { mutableStateOf(0) }
    val colors = listOf("1", "1", "3", "4")
    val colorsDetails = listOf("70\n0.3g", "100\n0.6g", "140\n0.9g", "180\n1.2g")
    val toggleStates = remember { mutableStateListOf(*Array(7) { false }) }
    var magnitudes = remember { mutableStateListOf(*Array(7) { 1 }) }
    LaunchedEffect(Unit) {
        val newValues = getValued()
        println(newValues)
        if (!newValues.isNullOrEmpty()) {
            newValues.forEachIndexed { i, value ->
                magnitudes[i] = if (value == 0) 1 else value  // Replace 0 with 1
                toggleStates[i] = if (value == 0) false else true
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize().padding(12.dp),
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Card(
                modifier = Modifier.fillMaxWidth().height(350.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE5E9E1)
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ){
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Row{
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isOnList[0]) Color(0xff376A3E) else Color.Transparent)
                                .border(2.dp, if (isOnList[0]) Color.Transparent else Color(0xff376A3E), CircleShape)
                                .clickable {
                                    isOnList[0] = !isOnList[0]
                                    if (isOnList[0]) {
                                        sendMagnitudeCommand(0, magnitudes[0])
                                    }
                                    else{
                                        sendMagnitudeCommand(0, 0)
                                    } },
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = "1",
                                color = if (isOnList[0]) Color.White else Color(0xff376A3E)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isOnList[1]) Color(0xff376A3E) else Color.Transparent)
                                .border(2.dp, if (isOnList[1]) Color.Transparent else Color(0xff376A3E), CircleShape)
                                .clickable {
                                    isOnList[1] = !isOnList[1]
                                    if (isOnList[1]) {
                                        sendMagnitudeCommand(1, magnitudes[1])
                                    }
                                    else{
                                        sendMagnitudeCommand(1, 0)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = "2",
                                color = if (isOnList[1]) Color.White else Color(0xff376A3E)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Box(
                            modifier = Modifier
                                .width(75.dp).height(45.dp)
                                .clip(RectangleShape)
                                .background(if (isOnList[2]) Color(0xff376A3E) else Color.Transparent)
                                .border(
                                    2.dp,
                                    if (isOnList[2]) Color.Transparent
                                    else Color(0xff376A3E),
                                    RectangleShape
                                )
                                .clickable {
                                    isOnList[2] = !isOnList[2]
                                    if (isOnList[2]) {
                                        sendMagnitudeCommand(2, magnitudes[2])
                                    }
                                    else{
                                        sendMagnitudeCommand(2, 0)
                                    } },
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = "3",
                                color = if (isOnList[2]) Color.White else Color(0xff376A3E)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .width(75.dp).height(45.dp)
                                .clip(RectangleShape)
                                .background(if (isOnList[3]) Color(0xff376A3E) else Color.Transparent)
                                .border(
                                    2.dp,
                                    if (isOnList[3]) Color.Transparent
                                    else Color(0xff376A3E),
                                    RectangleShape
                                )
                                .clickable {
                                    isOnList[3] = !isOnList[3]
                                    if (isOnList[3]) {
                                        sendMagnitudeCommand(3, magnitudes[3])
                                    }
                                    else{
                                        sendMagnitudeCommand(3, 0)
                                    } },
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = "4",
                                color = if (isOnList[3]) Color.White else Color(0xff376A3E)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .width(75.dp).height(45.dp)
                                .clip(RectangleShape)
                                .background(if (isOnList[4]) Color(0xff376A3E) else Color.Transparent)
                                .border(
                                    2.dp,
                                    if (isOnList[4]) Color.Transparent
                                    else Color(0xff376A3E),
                                    RectangleShape
                                )
                                .clickable {
                                    isOnList[4] = !isOnList[4]
                                    if (isOnList[4]) {
                                        sendMagnitudeCommand(4, magnitudes[4])
                                    }
                                    else{
                                        sendMagnitudeCommand(4, 0)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = "5",
                                color = if (isOnList[4]) Color.White else Color(0xff376A3E)
                            )
                        }
                    }
                    Row{
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isOnList[5]) Color(0xff376A3E) else Color.Transparent)
                                .border(2.dp, if (isOnList[5]) Color.Transparent else Color(0xff376A3E), CircleShape)
                                .clickable {
                                    isOnList[5] = !isOnList[5]
                                    if (isOnList[5]) {
                                        sendMagnitudeCommand(5, magnitudes[5])
                                    }
                                    else{
                                        sendMagnitudeCommand(5, 0)
                                    } },
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = "6",
                                color = if (isOnList[5]) Color.White else Color(0xff376A3E)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isOnList[6]) Color(0xff376A3E) else Color.Transparent)
                                .border(2.dp, if (isOnList[6]) Color.Transparent else Color(0xff376A3E), CircleShape)
                                .clickable {
                                    isOnList[6] = !isOnList[6]
                                    if (isOnList[6]) {
                                        sendMagnitudeCommand(6, magnitudes[6])
                                    }
                                    else{
                                        sendMagnitudeCommand(6, 0)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = "7",
                                color = if (isOnList[6]) Color.White else Color(0xff376A3E)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xff376A3E),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(200.dp),

                ) {
                Text("Enable All", textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Magnitude", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
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
                                .background(if (selectedMag == index) Color(0xff376A3E) else Color.Transparent)
                                .border(2.dp, if (selectedMag == index) Color.Transparent else Color(0xff376A3E), CircleShape)
                                .clickable {
                                    selectedMag = index
                                    if (!toggleStates[index]) {
                                        toggleStates[index] = true
                                        sendBinaryCommand(toggleStates)
                                    }
                                    sendMagnitudeCommand(index, index)
                                           },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                color = if (selectedMag == index) Color.White else Color(0xff376A3E)
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