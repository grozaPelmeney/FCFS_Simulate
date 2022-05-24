package com.example.fcfssimulate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fcfssimulate.models.ProcessControlBlock

val viewModel by lazy { MainViewModel() }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.startScheduling()
        setContent {
            Row(modifier = Modifier.background(Color(red = 233, green = 233, blue = 255))) {
                DrawLeftPart()
                // Divider(color = Color.Black, thickness = 1.dp)
                Divider(color = Color.Black, modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))
                DrawMiddlePart()
            }
            DrawButtons()
        }
    }

}

@Composable
private fun DrawButtons() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Column(modifier = Modifier.fillMaxSize(),  horizontalAlignment = Alignment.End) {
            DrawPauseBtn()
            DrawSettingsBtn()
            DrawAddProcessBtn()
        }
    }
}

@Composable
fun DrawSettingsBtn() {
    val openDialog = remember { mutableStateOf(false) }
    FloatingActionButton(
        modifier = Modifier.padding(5.dp),
        onClick = {
            openDialog.value = true
        }) {
        Icon(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = null
        )
    }
    if (openDialog.value) {
        ShowSettingsDialog(
            onBtnClick = {
                openDialog.value = false
            },
            onDismiss = {
                openDialog.value = false
            })
    }
}

@Composable
fun DrawAddProcessBtn() {
    val openDialog = remember { mutableStateOf(false) }
    FloatingActionButton(
        modifier = Modifier.padding(5.dp),
        onClick = {
            openDialog.value = true
        }) {
        Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = null
        )
    }
    if (openDialog.value) {
        ShowAddProcessDialog(
            onBtnClick = {
                openDialog.value = false
            },
            onDismiss = {
                openDialog.value = false
            })
    }
}

@Composable
fun DrawPauseBtn() {
    val isPause = remember { mutableStateOf(Core.isPause()) }
    FloatingActionButton(
        modifier = Modifier.padding(5.dp),
        onClick = {
            with(Core) {
                if(isPause()) playSimulation()
                else pauseSimulation()
                isPause.value = isPause()
            }
        }) {
        Icon(
            painter = if (isPause.value) { painterResource(id = R.drawable.ic_play) }
                        else { painterResource(id = R.drawable.ic_pause) },
            contentDescription = null
        )
    }
}

@Composable
fun ShowAddProcessDialog(
    onDismiss: () -> Unit,
    onBtnClick: () -> Unit) {

    Core.pauseSimulation()

    val addManyProcesses = remember { mutableStateOf(false) }
    val processesCount = remember { mutableStateOf(1) }
    val processName = remember { mutableStateOf("") }
    val cpuBurst = remember { mutableStateOf(0) }
    val ioBurst = remember { mutableStateOf(0) }
    val usingIOCount = remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            Core.playSimulation()
        },
        title = { Text(text = "Новый процесс") },
        text = {
               Column {
                   Row(modifier = Modifier.padding(bottom = 5.dp)) {
                       Text(
                           modifier = Modifier.weight(5f),
                           text = "Создать процессы случайно"
                       )
                       Checkbox(
                           modifier = Modifier.weight(2f),
                           checked = addManyProcesses.value,
                           onCheckedChange = { addManyProcesses.value = it })
                   }

                   Row(
                       modifier = Modifier.padding(bottom = 5.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Text(
                           modifier = Modifier.weight(5f),
                           text = "Количество процессов:"
                       )
                       TextField(
                           modifier = Modifier.weight(2f),
                           enabled = addManyProcesses.value,
                           value = processesCount.value.toString(),
                           onValueChange = { processesCount.value = it.toIntOrNull() ?: 0  },
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                       )
                   }

                   Divider(color = Color.Black, modifier = Modifier
                       .fillMaxWidth()
                       .height(1.dp))

                   Column(modifier = Modifier.padding(bottom = 5.dp)) {
                       Text(text = "Название процесса:")
                       TextField(
                           modifier = Modifier.fillMaxWidth(),
                           enabled = !addManyProcesses.value,
                           value = processName.value,
                           onValueChange = { processName.value = it },
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                       )
                   }

                   Row(
                       modifier = Modifier.padding(bottom = 5.dp),
                       verticalAlignment = Alignment.CenterVertically) {
                       Text(
                           modifier = Modifier.weight(5f),
                           text = "CPU Burst:"
                       )
                       TextField(
                           modifier = Modifier.weight(2f),
                           enabled = !addManyProcesses.value,
                           value = cpuBurst.value.toString(),
                           onValueChange = { cpuBurst.value = it.toIntOrNull() ?: 0 },
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                       )
                   }

                   Row(
                       modifier = Modifier.padding(bottom = 5.dp),
                       verticalAlignment = Alignment.CenterVertically) {
                       Text(
                           modifier = Modifier.weight(5f),
                           text = "IO Burst:"
                       )
                       TextField(
                           modifier = Modifier.weight(2f),
                           enabled = !addManyProcesses.value,
                           value = ioBurst.value.toString(),
                           onValueChange = { ioBurst.value = it.toIntOrNull() ?: 0 },
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                       )
                   }

                   Row(
                       modifier = Modifier.padding(bottom = 5.dp),
                       verticalAlignment = Alignment.CenterVertically) {
                       Text(
                           modifier = Modifier.weight(5f),
                           text = "Количество операций ввода-вывода:"
                       )
                       TextField(
                           modifier = Modifier.weight(2f),
                           enabled = !addManyProcesses.value,
                           value = usingIOCount.value.toString(),
                           onValueChange = { usingIOCount.value = it.toIntOrNull() ?: 0 },
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                       )
                   }
               }
        },
        confirmButton = {
            Button(
                onClick = {
                    if(processesCount.value == 1) {
                        viewModel.addProcess(
                            count = 1,
                            name = processName.value,
                            cpuBurst = cpuBurst.value,
                            ioBurst = ioBurst.value,
                            usingIOCount = usingIOCount.value)
                    } else {
                        viewModel.addProcess(count = processesCount.value)
                    }
                    Core.playSimulation()
                    onBtnClick()
                }) {
                Text("Добавить")
            }
        }

    )
}

@Composable
fun ShowSettingsDialog(
    onDismiss: () -> Unit,
    onBtnClick: () -> Unit) {

    Core.pauseSimulation()

    val ticks = remember { mutableStateOf(Core.getTicks()) }
    val seconds = remember { mutableStateOf(Core.getSeconds()) }
    val ioBurst = remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            Core.playSimulation()
        },
        title = { Text(text = "Настройки") },
        text = {
               Column {
                   Row(
                       modifier = Modifier.padding(bottom = 5.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       TextField(
                           modifier = Modifier.weight(2f),
                           value = seconds.value.toString(),
                           onValueChange = { seconds.value = it.toIntOrNull() ?: 0 },
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                       )
                       Text(
                           modifier = Modifier.weight(5f),
                           text = "секунд это"
                       )
                   }

                   Row(
                       modifier = Modifier.padding(bottom = 5.dp),
                       verticalAlignment = Alignment.CenterVertically) {
                       TextField(
                           modifier = Modifier.weight(2f),
                           value = ticks.value.toString(),
                           onValueChange = { ticks.value = it.toIntOrNull() ?: 0 },
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                       )
                       Text(
                           modifier = Modifier.weight(5f),
                           text = "тиков процессора"
                       )
                   }
               }
        },
        confirmButton = {
            Button(
                onClick = {
                    Core.setSeconds(seconds = seconds.value)
                    Core.setTicks(ticks = ticks.value)
                    Core.playSimulation()
                    onBtnClick()
                }) {
                Text("Сохранить")
            }
        }

    )
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
        .fillMaxWidth()) {
        if(currentProcess != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column {
                    Text(text = "${currentProcess!!.name}", fontWeight = FontWeight.Bold)
                    Text(text = "is running")
                }
            }
        }
    }
}

@Composable
fun DrawCurrentProcessInfo() {
    val currentProcess by viewModel.runningProcess.observeAsState()

    Column(modifier = Modifier
        .fillMaxSize()) {
        if(currentProcess != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column {
                    Text(text = "Process control block\n", fontWeight = FontWeight.Bold)
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
        DrawReadyProcesses()
        Divider(color = Color.Black, thickness = 1.dp)
        DrawBlockProcesses()
    }
}

@Composable
fun DrawReadyProcesses() {
    val readyProcessesCB by viewModel.readyProcessesCB.observeAsState()
  //  if (readyProcessesCB == null || readyProcessesCB!!.isEmpty()) { return }

    val showDialog = remember{ mutableStateOf(false) }
    val selectedProcessCB = remember { mutableStateOf<ProcessControlBlock?>(null) }

    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .fillMaxWidth()
    ) {
        Text(text = "ready")
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            if (readyProcessesCB != null) {
                items(readyProcessesCB!!) { PCB ->
                    Text(
                        modifier = Modifier.clickable {
                            selectedProcessCB.value = PCB
                            showDialog.value = true
                        },
                        text = viewModel.getProcessNameByPID(PID = PCB.PID),
                    )
                }
            }
        }
    }

    if (showDialog.value && selectedProcessCB.value != null) {
        ShowDialogWithProcessInfo(
            PCB = selectedProcessCB.value!!,
            onDismiss = {
                showDialog.value = false
                selectedProcessCB.value = null
                Core.playSimulation()
            },
            onBtnClick = {
                showDialog.value = false
                selectedProcessCB.value = null
                Core.playSimulation()
            }
        )
    }
}

@Composable
fun DrawBlockProcesses() {
    val waitingProcessesCB by viewModel.waitingProcessesCB.observeAsState()
  //  if (waitingProcessesCB == null || waitingProcessesCB!!.isEmpty()) { return }

    val showDialog = remember{ mutableStateOf(false) }
    val selectedProcessCB = remember { mutableStateOf<ProcessControlBlock?>(null) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Text(text = "block")
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            if (waitingProcessesCB != null) {
                items(waitingProcessesCB!!) { PCB ->
                    Text(
                        modifier = Modifier.clickable {
                            selectedProcessCB.value = PCB
                            showDialog.value = true
                        },
                        text = viewModel.getProcessNameByPID(PID = PCB.PID),
                    )
                }
            }
        }
    }

    if (showDialog.value && selectedProcessCB.value != null) {
        ShowDialogWithProcessInfo(
            PCB = selectedProcessCB.value!!,
            onDismiss = {
                showDialog.value = false
                selectedProcessCB.value = null
                Core.playSimulation()
            },
            onBtnClick = {
                showDialog.value = false
                selectedProcessCB.value = null
                Core.playSimulation()
            }
        )
    }
}

@Composable
fun ShowDialogWithProcessInfo(
    onDismiss: () -> Unit,
    onBtnClick: () -> Unit,
    PCB : ProcessControlBlock) {

    Core.pauseSimulation()
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = viewModel.getProcessNameByPID(PCB.PID)) },
        text = { Text(getProcessInfo(PCB)) },
        confirmButton = {
            Button(onClick = { onBtnClick() }) {
                Text("Ok")
            }
        }

    )
}

fun getProcessInfo(PCB : ProcessControlBlock) : String {
    return "PID: ${PCB.PID}\nState: ${PCB.processState}\nCPUburst: ${PCB.CPUburst}\nIOburst: ${PCB.IOburst}"
}