package com.everardo.filedownloader.di

import android.content.Context
import com.everardo.filedownloader.DownloadRegistry
import com.everardo.filedownloader.FileDownloader
import com.everardo.filedownloader.Notifier
import com.everardo.filedownloader.NotifierImpl
import com.everardo.filedownloader.data.repository.DownloadRepository
import com.everardo.filedownloader.data.repository.DownloadRepositoryImpl
import com.everardo.filedownloader.manager.DownloadManager
import com.everardo.filedownloader.manager.DownloadManagerImpl
import com.everardo.filedownloader.service.Downloader
import com.everardo.filedownloader.service.Scheduler
import com.everardo.filedownloader.service.SchedulerImpl
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal interface ObjectFactory {

    companion object {
        lateinit var instance: ObjectFactory
    }

    val context: Context
    val downloadRegistry: DownloadRegistry
    val downloadRepository: DownloadRepository
    val downloadManager: DownloadManager
    val scheduler: Scheduler

    fun getNotifier(fileDownloader: FileDownloader): Notifier

    fun getNewThreadExecutor(): ThreadPoolExecutor
}

internal class ObjectFactoryImpl(override val context: Context, private val downloader: Downloader): ObjectFactory {

    companion object {
        /*
        * Gets the number of available cores
        * (not always the same as the maximum number of cores)
        */
        private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
        // Sets the amount of time an idle thread waits before terminating
        private const val KEEP_ALIVE_TIME = 1L
        // Sets the Time Unit to seconds
        private val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS
    }

    override val downloadRegistry: DownloadRegistry by lazy { DownloadRegistry(context) }
    override val downloadRepository: DownloadRepository by lazy { DownloadRepositoryImpl() }
    override val downloadManager: DownloadManager by lazy { DownloadManagerImpl(downloadRepository, downloader) }
    override val scheduler: Scheduler by lazy { SchedulerImpl(context) }

    override fun getNotifier(fileDownloader: FileDownloader): Notifier = NotifierImpl(context, fileDownloader, downloadRepository)

    override fun getNewThreadExecutor(): ThreadPoolExecutor {
        val decodeWorkQueue: BlockingQueue<Runnable> = LinkedBlockingQueue<Runnable>()
        return ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                decodeWorkQueue
        )
    }
}

internal fun getObjectFactory() = ObjectFactory.instance

