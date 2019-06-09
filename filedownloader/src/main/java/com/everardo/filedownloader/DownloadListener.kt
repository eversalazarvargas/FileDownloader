package com.everardo.filedownloader

interface DownloadListener {
    fun onPending(status: StatusEvent)
    fun onCompleted(status: StatusEvent)
    fun onProgress(status: StatusEvent)
    fun onCancelled(status: StatusEvent)
    fun onError(status: StatusEvent)
}

abstract class AbstractDownloadListener : DownloadListener {
    override fun onPending(status: StatusEvent) {}
    override fun onCompleted(status: StatusEvent) {}
    override fun onProgress(status: StatusEvent) {}
    override fun onCancelled(status: StatusEvent) {}
    override fun onError(status: StatusEvent) {}
}
