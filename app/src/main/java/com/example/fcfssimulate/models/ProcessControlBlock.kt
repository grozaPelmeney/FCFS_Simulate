package com.example.fcfssimulate.models

data class ProcessControlBlock(
    val PID: Int,
    var processState: ProcessStates,
    var CPUburst: Int,
    var IOburst: Int
)