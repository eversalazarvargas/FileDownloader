package com.everardo.filedownloader.downloader

import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.data.repository.DownloadRepository

interface ProgressWriter {
    fun writeProgress(downloadToken: DownloadToken, percentage: Float)
}

internal class ProgressWriterImpl(private val downloadRepository: DownloadRepository): ProgressWriter {
    override fun writeProgress(downloadToken: DownloadToken, percentage: Float) {
    }
}
