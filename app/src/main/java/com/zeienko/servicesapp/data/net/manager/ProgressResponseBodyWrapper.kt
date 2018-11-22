package com.zeienko.servicesapp.data.net.manager

import com.zeienko.servicesapp.domain.abstractions.ProgressListener
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

class ProgressResponseBodyWrapper(
    private val responseBody: ResponseBody?,
    private val progressListener: ProgressListener
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? =
        responseBody?.contentType()

    override fun contentLength(): Long =
        responseBody?.contentLength() ?: 0L

    override fun source(): BufferedSource? {
        if (bufferedSource == null) {
            responseBody?.let {
                bufferedSource = Okio.buffer(source(it.source()))
            }
        }
        return bufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                responseBody?.apply {
                    progressListener.update(
                        totalBytesRead,
                        contentLength(),
                        bytesRead == -1L
                    )
                }
                return bytesRead
            }
        }
    }
}