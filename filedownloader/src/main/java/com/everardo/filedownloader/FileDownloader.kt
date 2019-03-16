package com.everardo.filedownloader

import android.net.Uri
import java.io.File


@OpenForTesting
class FileDownloader(private val config: FileDownloaderConfig) {

    private val objectFactory by lazy { config.objectFactory }
    private val notifier by lazy { objectFactory.getNotifier(this) }
    private val downloadManager by lazy { objectFactory.downloadManager }
    private val scheduler by lazy { objectFactory.scheduler }
    val downloadRegistry by lazy { objectFactory.downloadRegistry }

    fun uri(uri: Uri): RequestCreator = RequestCreator(this, uri)

    //TODO set visibility access to "internal"
    protected fun download(uri: Uri, fileName: String, listener: DownloadListener?, timeout: Long? = null, directory: File? = null): DownloadToken {
        val directoryParam = directory ?: config.directory
        val directoryPath = directoryParam.path

        val token = DownloadToken(uri, directoryPath, fileName)
        listener?.let {
            notifier.addObserver(token, it)
        }

        scheduler.download(token, timeout ?: config.timeout)

        return token
    }

    fun cancel(token: DownloadToken) {
        downloadManager.cancel(token)
    }

    fun retry(token: DownloadToken, timeout: Long? = null) {
        scheduler.retry(token, timeout ?: config.timeout)
    }

    fun removeListener(token: DownloadToken) {
        notifier.removeObserver(token)
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

            directory?.let {
                check(it.exists()) { "Directory must exists" }
                check(it.isDirectory) { "Directory parameter is not a directory" }
                check(it.canRead()) { "Directory must be read enabled" }
                check(it.canWrite()) { "Directory must be write enabled" }
            }

            return fileDownloader.download(uri, fileName, listener, timeout, directory)
        }
    }
}
