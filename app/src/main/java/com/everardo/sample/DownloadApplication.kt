package com.everardo.sample

import android.app.Application
import com.everardo.filedownloader.FileDownloader
import com.everardo.filedownloader.FileDownloaderConfig

class DownloadApplication: Application(), DownloaderProvider {

    private lateinit var fileDownloader: FileDownloader

    override fun onCreate() {
        super.onCreate()

        val config = FileDownloaderConfig.Builder()
                .context(this)
                .filesDirectory(filesDir)
                .build()

        fileDownloader = FileDownloader(config)
    }

    override fun getFileDownloader() = fileDownloader
}

interface DownloaderProvider {
    fun getFileDownloader(): FileDownloader
}