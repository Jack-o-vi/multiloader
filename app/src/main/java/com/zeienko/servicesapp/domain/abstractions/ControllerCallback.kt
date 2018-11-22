package com.zeienko.servicesapp.domain.abstractions

interface ControllerCallback {
    var isStarted: Boolean
    var isResumed: Boolean

    fun pause()
    fun resume()
}