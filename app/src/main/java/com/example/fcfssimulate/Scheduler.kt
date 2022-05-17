package com.example.fcfssimulate

import android.util.Log
import com.example.fcfssimulate.models.ProcessControlBlock
import com.example.fcfssimulate.models.ProcessModel
import com.example.fcfssimulate.models.ProcessStates
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random

/*val processes = mutableListOf(
    ProcessModel(PID = 1, name = "process1", neededProcessTime = 50, neededIOTime = 0),
    ProcessModel(PID = 2, name = "process2", neededProcessTime = 10, neededIOTime = 10),
    ProcessModel(PID = 3, name = "process3", neededProcessTime = 40, neededIOTime = 7),
    ProcessModel(PID = 4, name = "process4", neededProcessTime = 60, neededIOTime = 30),
    ProcessModel(PID = 5, name = "process11", neededProcessTime = 20, neededIOTime = 0),
)*/

class Scheduler {
    private var readyProcessesCB = listOf<ProcessControlBlock>()
    private var waitingProcessesCB = listOf<ProcessControlBlock>()
    private var runningProcess: ProcessModel? = null

    private var nextPID = 0

    val allProcesses = mutableListOf<ProcessModel>()

    fun startScheduling() {
        val time = Calendar.getInstance().time
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                Log.e("AA", "${allProcesses.map { Pair(it.PID, it.state) }}")
                if (Core.isPause()) { continue }
//                if (processes.firstOrNull { it.state != ProcessStates.TERMINATED } == null) {
//                    Core.pauseSimulation()
//                    Log.e("AA", "start time - ${time}")
//                    Log.e("AA", "end time - ${Calendar.getInstance().time}")
//                    break
//                }
                delay(Core.getReferMillsToTicks())
            }
        }
    }

    private fun startProcessesScheduling(processes : List<ProcessModel>) {
        this.allProcesses.addAll(processes)
        for (process in allProcesses.toMutableList()) {
            //Если процесс уже запущен, то у него уже есть PCB и его пропускаем
            if (process.getPCB() != null) continue
       //     Log.e("AA", "${allProcesses.map { Pair(it.PID, it.state) }}")

            CoroutineScope(Dispatchers.IO).launch {
                while (process.state != ProcessStates.TERMINATED) {
                    if (Core.isPause()) { continue }

                    //если процесс TERMINATED то ничего с ним не делаем
                    //   if (process.state == ProcessStates.TERMINATED) {
                    //       continue
                    //   }

                    if (process.state == ProcessStates.UNCREATED) {
                        process.create()
                        process.startToReady()
                        continue
                    }

                    if (process.state == ProcessStates.WAIT) {
                        process.waitToReady()
                        replaceProcessToEnd(process = process)
                        continue
                    }

                    readyProcessesCB =
                        allProcesses.filter { it.state == ProcessStates.READY }.map { it.getPCB()!! }
                    waitingProcessesCB =
                        allProcesses.filter { it.state == ProcessStates.WAIT }.map { it.getPCB()!! }

                    //Если нет исполняемого процесса, то берем первый из очереди
                    if (runningProcess == null) {
                        if (readyProcessesCB.isEmpty()) continue
                        val firstReadyProcess = getProcessByPID(readyProcessesCB.first().PID)
                        if (runningProcess != null) continue
                     //   Log.e("AA","running process - $runningProcess")
                     //   Log.e("AA","current process - $firstReadyProcess")
                        runningProcess = firstReadyProcess
                        runningProcess!!.readyToRunning()
                        continue
                    }

                    if (runningProcess == process) {
                        if (process.getPCB()!!.IOburst != 0) {
                            process.runningToWait()
                        } else {
                            process.runningToTerminated()
                        }
                        runningProcess = null
                    }

                    delay(Core.getReferMillsToTicks())
                }
            }
        }
    }

    private fun getProcessByPID(PID: Int) : ProcessModel {
        return allProcesses.first{ PID == it.PID }
    }

    private fun replaceProcessToEnd(process: ProcessModel) {
        allProcesses.remove(process)
        allProcesses.add(process)
    }

    private fun addReadyProcess(process: ProcessModel) {
            val processes = readyProcessesCB.toMutableList()
            processes.add(process.getPCB()!!)
    }

    private fun removeReadyProcess(process: ProcessModel) {
        val processes = readyProcessesCB.toMutableList()
        processes.remove(process.getPCB()!!)
    }

    private fun addWaitProcess(process: ProcessModel) {
        val processes = waitingProcessesCB.toMutableList()
        processes.add(process.getPCB()!!)
    }

    private fun removeWaitProcess(process: ProcessModel) {
        val processes = waitingProcessesCB.toMutableList()
        processes.remove(process.getPCB()!!)
    }

    fun addProcess(count: Int, name: String? = null, cpuBurst: Int? = null, ioBurst: Int? = null) {
        val processes = mutableListOf<ProcessModel>()
        if (count == 1) {
            val newProcess =
                ProcessModel(
                    PID = nextPID++,
                    name = name!!,
                    neededProcessTime = cpuBurst!!,
                    neededIOTime = ioBurst!!
                )
            processes.add(newProcess)
        } else {
            repeat(count) {
                val newProcess =
                    ProcessModel(
                        PID = nextPID++,
                        name = "process${nextPID - 1}",
                        neededProcessTime = Random.nextInt(from = 10, until = 50),
                        neededIOTime = if (Random.nextBoolean()) 0 else Random.nextInt(from = 10, until = 50)

                    )
                processes.add(newProcess)
            }
        }
        startProcessesScheduling(processes = processes)
    }

    fun getRunningProcess() = runningProcess
    fun getReadyProcessesCB() = readyProcessesCB
    fun getWaitingProcessesCB() = waitingProcessesCB

    fun getProcessNameByPID(PID: Int) : String {
        return allProcesses.first{ PID == it.PID }.name
    }
}