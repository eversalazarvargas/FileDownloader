package com.everardo.filedownloader

import android.net.Uri
import com.everardo.filedownloader.service.DownloadService
import java.io.File


class FileDownloader(private val config: FileDownloaderConfig) {

    private val objectFactory by lazy { config.objectFactory }
    private val context by lazy { objectFactory.context }
    private val notifier by lazy { objectFactory.getNotifier() }
    val downloadRegistry by lazy { objectFactory.downloadRegistry }

    fun uri(uri: Uri): RequestCreator = RequestCreator(this, uri)

    //TODO set visibility access to "internal"
    @Synchronized
    protected fun download(uri: Uri, fileName: String, listener: DownloadListener?, timeout: Long? = null, directory: File? = null): DownloadToken {
        val token = DownloadToken(uri, fileName)
        listener?.let {
            notifier.addObserver(token, it)
        }

        val directoryParam = directory ?: config.directory
        val directoryPath = directoryParam.path

        context.startService(DownloadService.getDownloadIntent(context, token, timeout ?: config.timeout, directoryPath))

        return token
    }

    @Synchronized
    fun cancel(downloadToken: DownloadToken) {
        context.startService(DownloadService.getCancelIntent(context, downloadToken))
    }

    @Synchronized
    fun removeListener(downloadToken: DownloadToken) {
        notifier.removeObserver(downloadToken)
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

            return fileDownloader.download(uri, fileName, listener, timeout, directory)
        }
    }
}
