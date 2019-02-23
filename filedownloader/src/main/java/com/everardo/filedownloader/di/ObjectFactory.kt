package com.everardo.filedownloader.di

import android.content.Context
import android.os.Looper
import com.everardo.filedownloader.DownloadRegistry
import com.everardo.filedownloader.FileDownloader
import com.everardo.filedownloader.Notifier
import com.everardo.filedownloader.NotifierImpl
import com.everardo.filedownloader.data.repository.DownloadRepository
import com.everardo.filedownloader.data.repository.DownloadRepositoryImpl

internal interface ObjectFactory {

    companion object {
        lateinit var instance: ObjectFactory
    }

    val context: Context
    val uiThreadLooper: Looper
    val downloadRegistry: DownloadRegistry
    val downloadRepository: DownloadRepository

    fun getNotifier(fileDownloader: FileDownloader): Notifier
}

internal class ObjectFactoryImpl(override val context: Context): ObjectFactory {

    override val uiThreadLooper: Looper by lazy { Looper.getMainLooper() }
    override val downloadRegistry: DownloadRegistry by lazy { DownloadRegistry(context) }
    override val downloadRepository: DownloadRepository by lazy { DownloadRepositoryImpl() }

    override fun getNotifier(fileDownloader: FileDownloader): Notifier = NotifierImpl(fileDownloader, downloadRepository, uiThreadLooper)
}

internal fun getObjectFactory() = ObjectFactory.instance

