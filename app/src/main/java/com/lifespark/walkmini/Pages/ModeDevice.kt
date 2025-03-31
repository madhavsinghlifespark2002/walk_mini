package com.lifespark.walkmini.Pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lifespark.walkmini.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeDevice(navController: NavController){
    FlowRow(
        modifier = Modifier.fillMaxSize().background(color = Color(0xfff4f4f4)),
        verticalArrangement = Arrangement.Center,
    ){
        Image(
            painter = painterResource(id = R.drawable.devicecontrol),
            contentDescription = "",
            modifier = Modifier
                .size(200.dp).clickable{
                    navController.navigate("newDeviceControlScreen")
                }
        )
        Image(
            painter = painterResource(id = R.drawable.patterncontrol),
            contentDescription = "",
            modifier = Modifier
                .size(200.dp).clickable{
                    navController.navigate("PatternDevice")
                }
        )
        Image(
            painter = painterResource(id = R.drawable.addpatient),
            contentDescription = "",
            modifier = Modifier
                .size(200.dp).clickable{
                    navController.navigate("addPatient")
                }
        )
    }
}