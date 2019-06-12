package com.everardo.filedownloader

import android.content.Context
import android.net.Uri
import com.everardo.filedownloader.di.ObjectFactory
import com.everardo.filedownloader.manager.DownloadManager
import com.everardo.filedownloader.service.Scheduler
import com.everardo.filedownloader.util.anySafe
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations
import java.io.File
import java.lang.IllegalStateException

class FileDownloaderTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var objectFactory: ObjectFactory

    @Mock
    private lateinit var config: FileDownloaderConfig

    @Mock
    private lateinit var notifier: Notifier

    @Mock
    private lateinit var downloadManager: DownloadManager

    @Mock
    private lateinit var scheduler: Scheduler

    @Mock
    private lateinit var uri: Uri

    @Mock
    private lateinit var downloadListener: DownloadListener

    @Mock
    private lateinit var directory: File

    private lateinit var fileDownloader: FileDownloader


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        whenever(objectFactory.getNotifier(anySafe(FileDownloader::class.java))).thenReturn(notifier)
        whenever(objectFactory.context).thenReturn(context)
        whenever(objectFactory.downloadManager).thenReturn(downloadManager)
        whenever(objectFactory.scheduler).thenReturn(scheduler)
        whenever(config.objectFactory).thenReturn(objectFactory)
        whenever(directory.path).thenReturn("filepath")
        whenever(directory.exists()).thenReturn(true)
        whenever(directory.isDirectory).thenReturn(true)
        whenever(directory.canRead()).thenReturn(true)
        whenever(directory.canWrite()).thenReturn(true)
        whenever(config.directory).thenReturn(directory)
        whenever(config.timeout).thenReturn(10)

        fileDownloader = FileDownloader(config)
    }

    @Test(expected = IllegalStateException::class)
    fun requestCreatorInvalidFileName() {
        fileDownloader.uri(uri)
                .fileName("")
                .listener(downloadListener)
                .timeout(200)
                .directory(directory)
                .download()
    }

    @Test(expected = IllegalStateException::class)
    fun requestCreatorInvalidTimeout() {
        fileDownloader.uri(uri)
                .fileName("filename")
                .listener(downloadListener)
                .timeout(0)
                .directory(directory)
                .download()
    }

    @Test(expected = IllegalStateException::class)
    fun requestCreatorInvalidTimeoutNegative() {
        fileDownloader.uri(uri)
                .fileName("filename")
                .listener(downloadListener)
                .timeout(-3)
                .directory(directory)
                .download()
    }

    @Test
    fun downloadWithListener() {
        fileDownloader.uri(uri)
                .fileName("filename")
                .listener(downloadListener)
                .download()

        verify(notifier).addObserver(anySafe(DownloadToken::class.java), anySafe(DownloadListener::class.java))
    }

    @Test
    fun downloadNoListener() {
        fileDownloader.uri(uri)
                .fileName("filename")
                .download()

        verify(notifier, times(0)).addObserver(anySafe(DownloadToken::class.java), anySafe(DownloadListener::class.java))
    }

    @Test
    fun downloadServiceStarted() {
        fileDownloader.uri(uri)
                .fileName("filename")
                .download()

        verify(scheduler).download(anySafe(DownloadToken::class.java), anyLong())
    }

    @Test
    fun downloadDefaultParams() {
        fileDownloader.uri(uri)
                .fileName("filename")
                .download()

        verify(config).timeout
        verify(config).directory
    }

    @Test
    fun downloadOverrideParams() {
        fileDownloader.uri(uri)
                .fileName("filename")
                .directory(directory)
                .timeout(20)
                .download()

        verify(config, times(0)).timeout
        verify(config, times(0)).directory
    }

    @Test
    fun removeListener() {
        fileDownloader.removeListener(mock(DownloadToken::class.java))

        verify(notifier).removeObserver(anySafe(DownloadToken::class.java))
    }

    @Test
    fun retry() {
        fileDownloader.retry(mock(DownloadToken::class.java))

        verify(scheduler).retry(anySafe(DownloadToken::class.java), anyLong())
    }
}
