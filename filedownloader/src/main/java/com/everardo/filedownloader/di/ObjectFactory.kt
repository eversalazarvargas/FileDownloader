package com.everardo.filedownloader.di

import android.content.Context
import android.os.Looper
import com.everardo.filedownloader.DownloadRegistry
import com.everardo.filedownloader.FileDownloader
import com.everardo.filedownloader.Notifier
import com.everardo.filedownloader.NotifierImpl
import com.everardo.filedownloader.data.repository.DownloadRepository
import com.everardo.filedownloader.data.repository.DownloadRepositoryImpl
import com.everardo.filedownloader.manager.DownloadManager
import com.everardo.filedownloader.manager.DownloadManagerImpl
import com.everardo.filedownloader.service.Scheduler
import com.everardo.filedownloader.service.SchedulerImpl

internal interface ObjectFactory {

    companion object {
        lateinit var instance: ObjectFactory
    }

    val context: Context
    val uiThreadLooper: Looper
    val downloadRegistry: DownloadRegistry
    val downloadRepository: DownloadRepository
    val downloadManager: DownloadManager
    val scheduler: Scheduler

    fun getNotifier(fileDownloader: FileDownloader): Notifier
}

internal class ObjectFactoryImpl(override val context: Context): ObjectFactory {

    override val uiThreadLooper: Looper by lazy { Looper.getMainLooper() }
    override val downloadRegistry: DownloadRegistry by lazy { DownloadRegistry(context) }
    override val downloadRepository: DownloadRepository by lazy { DownloadRepositoryImpl() }
    override val downloadManager: DownloadManager by lazy { DownloadManagerImpl(downloadRepository) }
    override val scheduler: Scheduler by lazy { SchedulerImpl(context) }

    override fun getNotifier(fileDownloader: FileDownloader): Notifier = NotifierImpl(fileDownloader, downloadRepository, uiThreadLooper)
}

internal fun getObjectFactory() = ObjectFactory.instance

