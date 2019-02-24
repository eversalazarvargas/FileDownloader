package com.everardo.filedownloader

interface DownloadListener {
    fun onChange(status: StatusEvent)
}
