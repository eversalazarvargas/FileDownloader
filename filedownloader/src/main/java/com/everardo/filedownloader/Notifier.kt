package com.everardo.filedownloader

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.util.ArrayMap
import com.everardo.filedownloader.data.repository.DataStatusChange
import com.everardo.filedownloader.data.repository.DownloadRepository
import com.everardo.filedownloader.data.repository.StatusChangeListener

internal interface Notifier {
    fun addObserver(token: DownloadToken, listener: DownloadListener)
    fun removeObserver(token: DownloadToken)
}

internal class NotifierImpl(private val fileDownloader: FileDownloader, downloadRepository: DownloadRepository, uiThreadLooper: Looper): Notifier {

    companion object {
        const val NOTIFY_STATUS_MSG = 1
    }

    private val listenersMap = ArrayMap<DownloadToken, DownloadListener>()
    private val handler = object : Handler(uiThreadLooper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == NOTIFY_STATUS_MSG) {
                notifyListeners()
            }
        }
    }

    init {
        downloadRepository.observe(StatusChangeListenerImpl(listenersMap))
    }

    override fun addObserver(token: DownloadToken, listener: DownloadListener) {
        listenersMap[token] = listener
    }

    override fun removeObserver(token: DownloadToken) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun notifyListeners() {

    }

    internal class StatusChangeListenerImpl(private val listenersMap: ArrayMap<DownloadToken, DownloadListener>): StatusChangeListener {
        override fun onStatusChange(status: DataStatusChange) {
            if (listenersMap.containsKey(status.token)) {

            }
        }
    }
}
