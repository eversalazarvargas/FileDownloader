package com.everardo.filedownloader

import android.net.Uri
import java.io.File


class FileDownloader(config: FileDownloaderConfig) {

    private val context = config.context
    val downloadRegistry = config.downloadRegistry
    val directory: File? = config.directory

    fun uri(uri: Uri): RequestCreator = RequestCreator(this, uri)

    //TODO set visibility access to "internal"
    protected fun download(uri: Uri, fileName: String, listener: DownloadListener?, timeout: Long? = null, directory: File? = null): DownloadToken {
        TODO()
    }

    fun cancel(downloadToken: DownloadToken) {
        TODO()
    }

    class RequestCreator internal constructor(private val fileDownloader: FileDownloader, private val uri: Uri) {
        private lateinit var fileName: String
        private var listener: DownloadListener? = null
        private var directory: File? = null
        private var timeout: Long? = null

        fun fileName(fileName: String): RequestCreator {
            this.fileName = fileName
            return this
        }

        fun listener(listener: DownloadListener): RequestCreator {
            this.listener = listener
            return this
        }

        fun directory(directory: File): RequestCreator {
            this.directory = directory
            return this
        }

        fun timeout(timeout: Long): RequestCreator {
            this.timeout = timeout
            return this
        }

        fun download(): DownloadToken {
            check(fileName.isNotEmpty()) { "Filename cannot be empty" }
            timeout?.let {
                check(it > 0) { "Timeout must be greater than zero milliseconds" }
            }

            return fileDownloader.download(uri = uri, fileName = fileName, listener = listener, timeout = timeout, directory = directory)
        }
    }
}
