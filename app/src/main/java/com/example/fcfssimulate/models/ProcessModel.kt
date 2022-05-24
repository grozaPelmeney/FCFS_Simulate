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
                        private var neededProcessTime: Int,
                        private var neededIOTime: Int,
                        private var usingIOCount : Int) : ProcessLifecycle {

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

        //через какое время заблокируется
        //(деление на количество ввода-вывода чтоб не было ситуаций, когда CPUburst = 2, а нужно еще 3 раза ввод-вывод сделать)
        usedProcessTime =
            if (usingIOCount == 0) Random.nextInt(from = 1, until = neededProcessTime)
            else Random.nextInt(from = 1, until = neededProcessTime / usingIOCount)


        //использование процессора перед вводом-выводом
        delay(getTimeDelay(from = usedProcessTime, until = usedProcessTime))

        //необходимое процессорное время уменьшилось
        neededProcessTime -= usedProcessTime
        PCB!!.CPUburst = neededProcessTime

        changeState(ProcessStates.WAIT)
    }

    override suspend fun waitToReady() {
        delay(getTimeDelay(from = 0, until = 1)) //время на переключение состояния

        //через какое время закончит ввод-вывод
        val usedIOTime =
            //если процесс не имеет ввода-вывода, то закончит ввод-вывод через 0 (т.е. его не будет)
            if (neededIOTime == 0) 0
            else {
                if (usingIOCount == 0) neededIOTime //если ввода-вывода больше не будет, то выполняет до конца
                if (neededIOTime == 1) neededIOTime
                else Random.nextInt(from = 1, until = neededIOTime) //если ввод-вывод потом еще будет, то выполняет какое-то время
            }

        delay(getTimeDelay(from = usedIOTime, until = usedIOTime)) //выполнение ввода-вывода

        usingIOCount -= 1

        neededIOTime -= usedIOTime
        PCB!!.IOburst = neededIOTime

        changeState(ProcessStates.READY)
    }

    private fun changeState(state: ProcessStates) {
        if(Core.isPause()) return
        if(PCB == null) return
        this.state = state
        PCB!!.processState = state
    }

    private fun getTimeDelay(from: Int, until: Int) =
        if (from == until) {
            from * Core.getReferMillsToTicks()
        } else {
            (Random.nextDouble(from = from.toDouble(), until = until.toDouble()) * Core.getReferMillsToTicks()).toLong()
        }

}