package com.everardo.filedownloader

import android.os.Bundle
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

internal class NotifierImpl(private val fileDownloader: FileDownloader, downloadRepository: DownloadRepository, uiThreadLooper: Looper) : Notifier {

    companion object {
        const val NOTIFY_STATUS_MSG = 1
        const val DATA_EXTRA = "dataExtra"
    }

    private val listenersMap = ArrayMap<DownloadToken, DownloadListener>()
    private val handler = object : Handler(uiThreadLooper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == NOTIFY_STATUS_MSG) {
                val dataStatusChange = msg.data[DATA_EXTRA] as DataStatusChange
                notifyListener(dataStatusChange)
            }
        }
    }

    init {
        downloadRepository.observe(StatusChangeListenerImpl(handler))
    }

    @Synchronized
    override fun addObserver(token: DownloadToken, listener: DownloadListener) {
        listenersMap[token] = listener
    }

    @Synchronized
    override fun removeObserver(token: DownloadToken) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Synchronized
    private fun notifyListener(dataStatusChange: DataStatusChange) {
        if (listenersMap.containsKey(dataStatusChange.token)) {
            val listener = listenersMap[dataStatusChange.token]
            listener?.let {
                val statusEvent = with(dataStatusChange) {
                    StatusEvent(status, token, progress, fileDownloader, token.uri, token.fileName, error)
                }
                it.onChange(statusEvent)
            }
        }
    }

    internal class StatusChangeListenerImpl(private val handler: Handler) : StatusChangeListener {
        override fun onStatusChange(status: DataStatusChange) {
            val bundle = Bundle()
            bundle.putParcelable(DATA_EXTRA, status)
            val msg = handler.obtainMessage(NOTIFY_STATUS_MSG)
            msg.data = bundle
            handler.sendMessage(msg)
        }
    }
}
