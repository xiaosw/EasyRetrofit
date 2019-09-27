package com.xiaosw.http

import okhttp3.RequestBody
import android.os.Handler
import android.os.Looper

import java.io.File
import java.io.FileInputStream
import java.io.IOException

import okhttp3.MediaType
import okio.BufferedSink
import java.lang.Exception

/**
 * @ClassName [ProgressRequestBody]
 * @Description
 *
 * @Date 2019-08-23.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
class ProgressRequestBody(
    private val mFile: File,
    private val mMediaType: String,
    private val mListener: UploadCallbacks
) : RequestBody() {

    interface UploadCallbacks {
        fun onProgressUpdate(percentage: Int)
        fun onError()
        fun onFinish()
    }

    override fun contentType(): MediaType? {
        // i want to upload only images
        return MediaType.parse(mMediaType)
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = mFile.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val fis = FileInputStream(mFile)
        var uploaded: Long = 0

        try {
            fis.use {
                var read: Int = fis.read(buffer)
                val handler = Handler(Looper.getMainLooper())
                while (read != -1) {
                    uploaded += read.toLong()
                    sink.write(buffer, 0, read)
                    // update progress on UI thread
                    handler.post(ProgressUpdater(uploaded, fileLength))
                    read = fis.read(buffer)
                }
                mListener?.onFinish()
            }
        } catch (e: Exception) {
            mListener?.onError()
        }
    }

    private inner class ProgressUpdater(private val mUploaded: Long, private val mTotal: Long) : Runnable {

        override fun run() {
            mListener.onProgressUpdate((100 * mUploaded / mTotal).toInt())
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }

}