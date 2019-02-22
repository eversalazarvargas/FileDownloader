package com.everardo.filedownloader

internal interface Notifier {
    fun addObserver(token: DownloadToken, listener: DownloadListener)
    fun removeObserver(listener: DownloadListener)
}

internal class NotifierImpl: Notifier {
    override fun addObserver(token: DownloadToken, listener: DownloadListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeObserver(listener: DownloadListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
