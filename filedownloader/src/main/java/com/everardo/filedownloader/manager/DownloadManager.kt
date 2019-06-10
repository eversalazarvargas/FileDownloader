package com.everardo.filedownloader.manager

import android.util.Log
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.data.repository.DownloadRepository
import com.everardo.filedownloader.downloader.DownloadResult
import com.everardo.filedownloader.downloader.Downloader
import com.everardo.filedownloader.downloader.ProgressWriter
import java.lang.Exception

internal interface DownloadManager {
    fun addPendingDownload(downloadToken: DownloadToken)
    fun download(downloadToken: DownloadToken, timeout: Long)
    fun hasPendingDownloads(): Boolean
    fun cancel(downloadToken: DownloadToken)
}

internal class DownloadManagerImpl(private val downloadRepository: DownloadRepository,
                                   private val downloader: Downloader,
                                   private val progressWriter: ProgressWriter): DownloadManager {
    override fun addPendingDownload(downloadToken: DownloadToken) {
        downloadRepository.addPendingDownload(downloadToken)
    }
    override fun download(downloadToken: DownloadToken, timeout: Long) {
        // Since the downloader can be implemented by the client of this library, we must give it some object to
        // write the progress, and also it has to return us the result.
        var result: DownloadResult? = null
        try {
            result = downloader.downloadFile(downloadToken, timeout, progressWriter)
        } catch (exception: Exception) {
            Log.e(this.javaClass.name, "Error trying to download file ${downloadToken.fileName}", exception)
        }

        downloadRepository.completeDownload(downloadToken, result ?: DownloadResult.ERROR)
    }

    override fun hasPendingDownloads() = downloadRepository.hasPendingDownloads()

    override fun cancel(downloadToken: DownloadToken) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
