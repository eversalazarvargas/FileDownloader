package com.everardo.filedownloader.manager

import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.data.repository.DownloadRepository

internal interface DownloadManager {
    fun cancel(downloadToken: DownloadToken)
}

internal class DownloadManagerImpl(private val downloadRepository: DownloadRepository): DownloadManager {
    override fun cancel(downloadToken: DownloadToken) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
