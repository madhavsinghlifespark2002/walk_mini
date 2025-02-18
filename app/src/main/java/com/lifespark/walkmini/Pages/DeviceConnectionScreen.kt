package com.lifesparktech.lsphysio.android.pages
import android.annotation.SuppressLint
import android.os.Build
import android.os.CountDownTimer
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juul.kable.Advertisement
import com.juul.kable.Filter
import com.juul.kable.Scanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.focusRequester
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.juul.kable.Characteristic
import com.juul.kable.Peripheral
import com.lifespark.walkmini.MainActivity
import com.lifespark.walkmini.connectdevice.ConnectDeviced
import com.lifespark.walkmini.connectdevice.scanBluetoothDevices
import com.lifesparktech.lsphysio.android.pages.PeripheralManager.mainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay


@SuppressLint("StaticFieldLeak")
object PeripheralManager {
    var peripheral: Peripheral? = null
    var charWrite: Characteristic? = null
    var charRead: Characteristic? = null
    lateinit var mainScope: CoroutineScope
    var Command: String = "0000000"
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeviceConnectionScreen(navcontroller: NavController) {
    mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var isScanning by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf<List<Advertisement>>(emptyList()) }
    var timerValue by remember { mutableStateOf(10) }
    var isConnecting by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var context = LocalContext.current
    var connectingDevice by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val mainActivity = LocalContext.current as MainActivity

    Column(
        modifier = Modifier
            .background(color = Color(0xFFf4f4f4))
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth().padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Connection", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222429))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(color = Color(0xFFD6E7EE))
                        .padding(8.dp)
                        .clickable {}
                ) {
                    Row(
                        modifier = Modifier
                            .width(105.dp)
                            .clickable {
                                mainActivity.requestBluetoothPermissions()
                                mainActivity.requestLocationPermissions()
                                if (!isScanning) {
                                    isScanning = true
                                    timerValue = 10 // Reset timer
                                    val timer = object : CountDownTimer(10000, 1000) { // 10 seconds scan
                                        override fun onTick(millisUntilFinished: Long) {
                                            timerValue = (millisUntilFinished / 1000).toInt()
                                        }

                                        override fun onFinish() {
                                            isScanning = false
                                        }
                                    }
                                    timer.start()

                                    scanBluetoothDevices(context, scope) { foundDevices ->
                                        devices = foundDevices
                                        isScanning = false // Stop scanning once devices are found
                                    }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            if (isScanning) "Scanning..." else " Start Scan",
                            color = Color.Black
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Available device",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF474747),
                modifier = Modifier.padding(start = 12.dp)
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                color = Color(0xFFD6D6D6),
                thickness = 1.dp
            )
            Box(
                modifier = Modifier.height(if (screenWidth <= 800.0.dp) { 800.dp } else { 400.dp })
            ) {
                if (devices.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        devices.forEach { device ->
                            item{
                                Card(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .width(320.dp)
                                        .height(100.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${device.name}", color = Color.Black)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                isConnecting = true
                                                mainScope.launch {
                                                    connectingDevice = device.name // Set the connecting device
                                                    try {
                                                        delay(2000)
                                                        ConnectDeviced(context, navcontroller, device)
                                                    } catch (e: Exception) {
                                                        println("Error connecting: ${e.message}")
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to connect: ${e.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } finally {
                                                        connectingDevice = null
                                                        isConnecting = false
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF005749)),
                                        ) {
                                            Text(
                                                text = if (connectingDevice == device.name) "Connecting..." else "Connect",
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (!isScanning) {
                    Text("No devices found. Click 'Start Scan' to search for devices.", color = Color.Black, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }
    }
}
// po