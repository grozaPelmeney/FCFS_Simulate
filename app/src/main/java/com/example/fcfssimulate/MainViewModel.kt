package com.example.fcfssimulate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fcfssimulate.models.ProcessControlBlock
import com.example.fcfssimulate.models.ProcessModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val readyProcessesCB = MutableLiveData<List<ProcessControlBlock>>()
    val waitingProcessesCB = MutableLiveData<List<ProcessControlBlock>>()
    val runningProcess = MutableLiveData<ProcessModel?>()
    val absoluteTicks = MutableLiveData<Int>()

    private val scheduler by lazy { Scheduler() }

    fun getProcessNameByPID(PID: Int) : String {
        return scheduler.getProcessNameByPID(PID = PID)
    }

    fun addProcess(count: Int, name: String? = null, cpuBurst: Int? = null, ioBurst: Int? = null, usingIOCount: Int? = null) {
        scheduler.addProcess(count, name, cpuBurst, ioBurst, usingIOCount)
    }

    fun startScheduling() {
        scheduler.startScheduling()
        startLiveDataUpdates()
    }

    private fun startLiveDataUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if (Core.isPause()) continue

                readyProcessesCB.postValue(scheduler.getReadyProcessesCB())
                waitingProcessesCB.postValue(scheduler.getWaitingProcessesCB())
                runningProcess.postValue(scheduler.getRunningProcess())
                absoluteTicks.postValue(Core.getAbsoluteTicks())

                delay(Core.getReferMillsToTicks() / 2)
            }
        }
    }
}