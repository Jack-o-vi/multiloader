package com.zeienko.servicesapp.domain.interactors

import com.zeienko.servicesapp.domain.abstractions.ContentLoaderCallback
import com.zeienko.servicesapp.domain.abstractions.ControllerCallback
import com.zeienko.servicesapp.domain.abstractions.ProgressListener

class ProgressListenerImp(
    private val callback: ContentLoaderCallback,
    private val controllerCallback: ControllerCallback
) : ProgressListener {
    private var firstUpdate = true

    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
        if (controllerCallback.isStarted) {
            if (done) {
                callback.onComplete()
            } else {
                if (controllerCallback.isResumed) {
                    if (firstUpdate) {
                        firstUpdate = false
                        callback.onFirstUpdate(bytesRead, contentLength)
                    }
                    callback.onBytesReadUpdate(bytesRead, contentLength)
                    if (contentLength != -1L) {
                        callback.onUpdate(bytesRead, contentLength)
                    }
                } // isResumed
                else{
                    controllerCallback.pause()
                }
            }
        } // isStopped
    }
}