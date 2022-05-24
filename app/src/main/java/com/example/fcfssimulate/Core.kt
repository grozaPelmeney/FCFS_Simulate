package com.example.fcfssimulate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Core {
    init {
        startTickCounter()
    }

    private var pause = false

    private var absoluteTicks = 0

    private var secondsToRefer = 2 // 2 секунды..
    private var ticksToRefer = 3 // ..это 3 тика

 //   private var maxProcessCount = 10

    private fun startTickCounter() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (isPause()) continue
                absoluteTicks++
                //  absoluteTicks += ticksToRefer
                //  delay(1000 * secondsToRefer.toLong())
                delay(getReferMillsToTicks())
            }
        }
    }

    fun isPause() = pause
    fun pauseSimulation() { pause = true }
    fun playSimulation() { pause = false }

    fun getReferMillsToTicks() = (1000 * secondsToRefer / ticksToRefer).toLong()
    fun getAbsoluteTicks() = absoluteTicks
    fun getTicks() = ticksToRefer
    fun getSeconds() = secondsToRefer

    fun setTicks(ticks: Int) { ticksToRefer = ticks }
    fun setSeconds(seconds: Int) { secondsToRefer = seconds }
}