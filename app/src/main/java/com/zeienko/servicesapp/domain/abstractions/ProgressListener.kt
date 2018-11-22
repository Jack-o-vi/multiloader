package com.zeienko.servicesapp.domain.abstractions

interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}