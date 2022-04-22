package com.example.fcfssimulate

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fcfssimulate.models.ProcessControlBlock
import com.example.fcfssimulate.models.ProcessModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


//scheduler
class MainViewModel : ViewModel() {
    val readyProcesses = MutableLiveData<List<ProcessControlBlock>>()
    val waitingProcesses = MutableLiveData<List<ProcessControlBlock>>()
    val runningProcess = MutableLiveData<ProcessModel?>()
    val absoluteTicks = MutableLiveData<Int>()

    private val scheduler by lazy { Scheduler() }

    fun startScheduling() {
        scheduler.startScheduling()
        startLiveDataUpdates()
    }

    private fun startLiveDataUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if (Core.isPause()) continue

                readyProcesses.postValue(scheduler.getReadyProcessesCB())
                waitingProcesses.postValue(scheduler.getWaitingProcessesCB())
                runningProcess.postValue(scheduler.getRunningProcess())
                absoluteTicks.postValue(Core.getAbsoluteTicks())
                delay(Core.getReferMillsToTicks())
            }
        }
    }
}