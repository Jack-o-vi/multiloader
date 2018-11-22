package com.zeienko.servicesapp.ui.service

import android.annotation.SuppressLint
import android.util.Log
import com.zeienko.servicesapp.BuildConfig
import com.zeienko.servicesapp.data.net.api.UnsplashApi
import com.zeienko.servicesapp.data.net.manager.NetworkManager
import com.zeienko.servicesapp.domain.abstractions.ContentLoaderCallback
import com.zeienko.servicesapp.domain.interactors.ControllerCallbackImp
import com.zeienko.servicesapp.ui.fragment.TestFragment
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.locks.ReentrantLock

class ImageLoader(
    private val id: Int,
    updateProgress: (msg: String, id: Int) -> Unit,
    private val updateImage: (path: String, id: Int) -> Unit,
    private val controllerCallbackImp: ControllerCallbackImp
) {

    @SuppressLint("SdCardPath")
    private val filePath = "/data/data/${BuildConfig.APPLICATION_ID}/files/$id.jpg"
    private var api: UnsplashApi
    private val lock = ReentrantLock()
    private var mPauseWorkLock: Object? = null

    companion object {
        const val BUFFER_SIZE = 8192L
    }

    init {
        api = NetworkManager.getUnsplashApiWithProgressListener(
            ContentLoaderCallbackImp(
                id, updateProgress
            ), controllerCallbackImp
        )
    }

    fun downloadImage() {
        api.getImage().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TestFragment.TAG, "onFailure: $call, exception: $t")
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseBody = response.body()
                val bufferedSource = responseBody!!.source()
                val buffer = writeToBuffer(bufferedSource)
            }
        })
    }

    private fun saveToFile(buffer: Buffer, filePath: String) {
        val file = File(filePath)
        val fOut = FileOutputStream(file)
        buffer.copyTo(fOut)
        fOut.close()
    }

    private fun writeToBuffer(source: BufferedSource): Buffer {
        val buffer = Buffer()
        var totalBytesRead: Long = 0
        var readCount: Long

        readCount = source.read(buffer, BUFFER_SIZE)
        while (readCount != -1L) {
            if (!controllerCallbackImp.isResumed) pause()
            else if (!controllerCallbackImp.isStarted) return buffer
            totalBytesRead += readCount
            readCount = source.read(buffer, BUFFER_SIZE)
        }

        saveToFile(buffer, filePath)
        updateImage(filePath, id)

        return buffer
    }

    class ContentLoaderCallbackImp(
        private val id: Int,
        private val setText1: (msg: String, id: Int) -> Unit
    ) : ContentLoaderCallback {

        override fun onComplete() {
            Log.d(TestFragment.TAG, "completed")
            setText1("Completed", id)
        }

        override fun onFirstUpdate(bytesRead: Long, contentLength: Long) {
            setText1("Start", id)
            if (contentLength == -1L) {
                Log.d(TestFragment.TAG, "content-length: unknown")
            } else {
                Log.d(TestFragment.TAG, "content-length: $contentLength")
            }
        }

        override fun onBytesReadUpdate(bytesRead: Long, contentLength: Long) {
            Log.d(TestFragment.TAG, "$bytesRead")

        }

        override fun onUpdate(bytesRead: Long, contentLength: Long) {
            Log.d(TestFragment.TAG, "${(100 * bytesRead) / contentLength}% done")
            setText1("${(100 * bytesRead) / contentLength}% done", id)
        }
    }

    fun pause() {
//        lock.apply {
//            if (!isLocked) {
//                lock()
//            }
//        }
        synchronized(this) {
            try {
                mPauseWorkLock?.wait()
            } catch (e: InterruptedException) {

            }
        }
    }

    fun resume() {
//        lock.apply {
//            if (isLocked) {
//                lock.unlock()
//            }
//        }
        synchronized(this) {
            mPauseWorkLock?.notifyAll()
        }
    }
}