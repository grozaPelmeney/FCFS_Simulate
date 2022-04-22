package com.example.fcfssimulate.interfaces

interface ProcessLifecycle {
    suspend fun create()
    suspend fun startToReady()
    suspend fun readyToRunning()
    suspend fun runningToTerminated()
    suspend fun runningToWait()
    suspend fun waitToReady()
}