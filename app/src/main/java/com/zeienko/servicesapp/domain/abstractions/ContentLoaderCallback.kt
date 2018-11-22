package com.zeienko.servicesapp.domain.abstractions

interface ContentLoaderCallback {
    fun onComplete()

    fun onFirstUpdate(bytesRead: Long, contentLength: Long)

    fun onBytesReadUpdate(bytesRead: Long, contentLength: Long)

    fun onUpdate(bytesRead: Long, contentLength: Long)
}