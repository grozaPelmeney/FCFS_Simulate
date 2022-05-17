package com.example.fcfssimulate.models

import android.util.Log
import com.example.fcfssimulate.Core
import com.example.fcfssimulate.interfaces.ProcessLifecycle
import kotlinx.coroutines.delay
import kotlin.random.Random

data class ProcessModel(val PID: Int,
                   val name: String,
                   var state: ProcessStates = ProcessStates.UNCREATED,
                   private var PCB: ProcessControlBlock? = null,
                   private var usedProcessTime: Int = 0,
                   private val neededProcessTime: Int,
                   private val neededIOTime: Int) : ProcessLifecycle {

    fun getPCB() = PCB

    override suspend fun create() {
        delay(getTimeDelay(from = 0, until = 1)) //время на переключение состояния

        changeState(ProcessStates.START)
        PCB = ProcessControlBlock(PID = PID, processState = state, CPUburst = neededProcessTime, IOburst = neededIOTime)

    }

    override suspend  fun startToReady() {
        delay(getTimeDelay(from = 0, until = 1)) //время на переключение состояния

        changeState(ProcessStates.READY)
    }

    override suspend  fun readyToRunning() {
        delay(getTimeDelay(from = 0, until = 1)) //время на переключение состояния
        delay(getTimeDelay(from = 1, until = 3)) //восстановление контекста

        changeState(ProcessStates.RUNNING)
    }

    override suspend  fun runningToTerminated() {
        delay(getTimeDelay(from = 0, until = 1)) //время на переключение состояния
        delay(getTimeDelay(from = neededProcessTime - usedProcessTime, until = neededProcessTime - usedProcessTime)) //время на переключение состояния

        changeState(ProcessStates.TERMINATED)
    }

    override suspend  fun runningToWait() {
        delay(getTimeDelay(from = 0, until = 1)) //время на переключение состояния
        delay(getTimeDelay(from = 1, until = 3)) //cохранение контекста

        changeState(ProcessStates.WAIT)

        usedProcessTime = Random.nextInt(from = 1, until = neededProcessTime) //через какое время заблокируется
    }

    override suspend  fun waitToReady() {
        delay(getTimeDelay(from = 0, until = 1)) //время на переключение состояния
        delay(getTimeDelay(from = neededIOTime, until = neededIOTime)) //выполнение ввода-вывода

        changeState(ProcessStates.READY)

        PCB?.IOburst = 0 //после выполнения ввода-вывода, он больше не нужен
        PCB?.CPUburst = (neededProcessTime - usedProcessTime) //оставшееся время на выполнение
    }

    private fun changeState(state: ProcessStates) {
        if(Core.isPause()) return
        this.state = state
        PCB?.processState = state
    }

    private fun getTimeDelay(from: Int, until: Int) =
        if (from == until) {
            from * Core.getReferMillsToTicks()
        } else {
            (Random.nextDouble(from = from.toDouble(), until = until.toDouble()) * Core.getReferMillsToTicks()).toLong()
        }

}