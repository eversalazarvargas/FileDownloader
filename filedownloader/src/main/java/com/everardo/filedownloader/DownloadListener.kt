package com.everardo.filedownloader

interface DownloadListener {
    fun onCompleted(status: StatusEvent)
    fun onProgress(status: StatusEvent)
    fun onCancelled(status: StatusEvent)
    fun onError(status: StatusEvent)
}
