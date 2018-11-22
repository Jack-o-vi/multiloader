package com.zeienko.servicesapp.domain.interactors

import com.zeienko.servicesapp.domain.abstractions.ControllerCallback
import java.util.concurrent.locks.ReentrantLock

class ControllerCallbackImp : ControllerCallback {

    private val lock = ReentrantLock()
    override var isStarted = true
    override var isResumed = true

    override fun pause() {
        lock.apply {
            if (!isLocked) {
                lock()
            }
        }
    }

    override fun resume() {
        lock.apply {
            if (isLocked) {
                lock.unlock()
            }
        }
    }

}