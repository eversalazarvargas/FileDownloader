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
        var result = DownloadResult.SUCCESSFUL
        try {
            result = downloader.downloadFile(downloadToken, timeout, progressWriter)
        } catch (exception: InterruptedException) {
            Log.d(this.javaClass.name, "Cancelling download of file ${downloadToken.fileName}", exception)
            result = DownloadResult.CANCELLED
        } catch (exception: Exception) {
            Log.e(this.javaClass.name, "Error trying to download file ${downloadToken.fileName}", exception)
            result = DownloadResult.ERROR
        } finally {
            downloadRepository.completeDownload(downloadToken, result)
        }
    }

    override fun hasPendingDownloads() = downloadRepository.hasPendingDownloads()
}
