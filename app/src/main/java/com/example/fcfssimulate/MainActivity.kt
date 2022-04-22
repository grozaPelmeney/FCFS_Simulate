package com.example.fcfssimulate

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fcfssimulate.ui.theme.FCFSSimulateTheme

val viewModel by lazy { MainViewModel() }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.startScheduling()
        setContent {
            Row {
                DrawLeftPart()
                // Divider(color = Color.Black, thickness = 1.dp)
                Divider(color = Color.Black, modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))
                DrawMiddlePart()
            }
            DrawButtons()
        }
//        setContent {
//            FCFSSimulateTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colors.background
//                ) {
//                    Row {
//                        DrawLeftPart()
//                       // Divider(color = Color.Black, thickness = 1.dp)
//                        Divider(color = Color.Black, modifier = Modifier
//                            .fillMaxHeight()
//                            .width(1.dp))
//                        DrawMiddlePart()
//                    }
//                }
//            }
//        }
    }

}

@Composable
private fun DrawButtons() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.End) {
            ExtendedFloatingActionButton(
                text = { "pause" },
                onClick = { with(Core) { if(isPause()) playSimulation() else pauseSimulation()} } )
        }
    }
}

@Composable
fun DrawMiddlePart() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DrawCurrentProcess()
            Divider(color = Color.Black, thickness = 1.dp)
            DrawCurrentProcessInfo()
        }
        DrawAbsoluteTicks()
    }
}

@Composable
fun DrawAbsoluteTicks() {
    val absoluteTicks by viewModel.absoluteTicks.observeAsState()
    Text(text = "ticks: ${absoluteTicks}")
}

@Composable
fun DrawCurrentProcess() {
    val currentProcess by viewModel.runningProcess.observeAsState()

    Column(modifier = Modifier
        .fillMaxHeight(0.5f)
        .fillMaxWidth()
        .background(Color.LightGray)) {
        if(currentProcess != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "currentProcess - ${currentProcess!!.PID}")
            }
        }
    }
}

@Composable
fun DrawCurrentProcessInfo() {
    val currentProcess by viewModel.runningProcess.observeAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Cyan)) {
        if(currentProcess != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column {
                    Text(text = "Process control block", fontWeight = FontWeight.Bold)
                    Text(text = "PID: ${currentProcess!!.getPCB()!!.PID}")
                    Text(text = "State: ${currentProcess!!.getPCB()!!.processState}")
                    Text(text = "CPUburst: ${currentProcess!!.getPCB()!!.CPUburst}")
                    Text(text = "IOburst: ${currentProcess!!.getPCB()!!.IOburst}")
                }
            }
        }
    }
}

@Composable
fun DrawLeftPart() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.2f)
    ) {
        DrawReadyProcess()
        Divider(color = Color.Black, thickness = 1.dp)
        DrawBlockProcess()
    }
}

@Composable
fun DrawReadyProcess() {
    val readyProcesses by viewModel.readyProcesses.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .fillMaxWidth()
            .background(Color.Green)
    ) {
        Text(text = "ready")
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            if (readyProcesses != null) {
                items(readyProcesses!!) { PCB ->
                    Text(text = PCB.PID.toString())
                }
            }
        }
    }
}

@Composable
fun DrawBlockProcess() {
    val waitingProcesses by viewModel.waitingProcesses.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.Red)
    ) {
        Text(text = "block")
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            if (waitingProcesses != null) {
                items(waitingProcesses!!) { PCB ->
                    Text(text = PCB.PID.toString())
                }
            }
        }
    }
}