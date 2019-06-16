package com.everardo.filedownloader.downloader

import com.everardo.filedownloader.DownloadToken

interface Downloader {
    fun downloadFile(downloadToken: DownloadToken, timeout: Long, progressWriter: ProgressWriter): DownloadResult
}

internal class DefaultDownloader: Downloader {
    override fun downloadFile(downloadToken: DownloadToken, timeout: Long, progressWriter: ProgressWriter): DownloadResult {
        //TODO not implemented

        return DownloadResult.SUCCESSFUL
    }
}
