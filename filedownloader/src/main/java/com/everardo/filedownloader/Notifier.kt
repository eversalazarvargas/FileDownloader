package com.everardo.filedownloader

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.util.ArrayMap
import com.everardo.filedownloader.data.repository.DataStatusChange
import com.everardo.filedownloader.data.repository.DownloadRepository

internal interface Notifier {
    fun addObserver(token: DownloadToken, listener: DownloadListener)
    fun removeObserver(token: DownloadToken)
}

internal class NotifierImpl(private val context: Context, private val fileDownloader: FileDownloader, private val repository: DownloadRepository) : Notifier {

    companion object {
        const val NOTIFY_STATUS_MSG = 1
        const val DATA_EXTRA = "dataExtra"
    }

    private val listenersMap = ArrayMap<DownloadToken, DownloadListener>()
    private val handler = object : Handler(context.mainLooper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == NOTIFY_STATUS_MSG) {
                val dataStatusChange = msg.data[DATA_EXTRA] as DataStatusChange
                notifyListener(dataStatusChange)
            }
        }
    }
    private val statusContentObserver: StatusContentObserver

    init {
        statusContentObserver = StatusContentObserver(handler, repository)
    }

    @Synchronized
    override fun addObserver(token: DownloadToken, listener: DownloadListener) {
        if (listenersMap.isEmpty) {
            repository.registerContentObserver(statusContentObserver)
        }
        listenersMap[token] = listener
    }

    @Synchronized
    override fun removeObserver(token: DownloadToken) {
        listenersMap.remove(token)
        if (listenersMap.isEmpty) {
            repository.unregisterContentObserver(statusContentObserver)
        }
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

    internal class StatusContentObserver(private val handler: Handler, private val downloadRepository: DownloadRepository) : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri) {
            val status = downloadRepository.getDataStatusChange(uri)

            val bundle = Bundle()
            bundle.putParcelable(DATA_EXTRA, status)
            val msg = handler.obtainMessage(NOTIFY_STATUS_MSG)
            msg.data = bundle
            handler.sendMessage(msg)
        }
    }
}
