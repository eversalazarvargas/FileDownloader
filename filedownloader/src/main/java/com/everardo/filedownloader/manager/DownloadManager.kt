package com.everardo.filedownloader.manager

import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.data.repository.DownloadRepository
import com.everardo.filedownloader.service.Downloader

internal interface DownloadManager {
    fun addPendingDownload(downloadToken: DownloadToken)
    fun download(downloadToken: DownloadToken, timeout: Long)
    fun hasPendingDownloads(): Boolean
    fun cancel(downloadToken: DownloadToken)
}

internal class DownloadManagerImpl(private val downloadRepository: DownloadRepository, private val downloader: Downloader): DownloadManager {
    override fun addPendingDownload(downloadToken: DownloadToken) {
        downloadRepository.addPendingDownload(downloadToken)
    }
    override fun download(downloadToken: DownloadToken, timeout: Long) {
        // TODO Since the downloader can be implemented by the client of this library we must make the downloader some class with hooks
        // to determine the output and the result of the download

        // TODO Save the status in the database
    }

    override fun hasPendingDownloads(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun cancel(downloadToken: DownloadToken) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
