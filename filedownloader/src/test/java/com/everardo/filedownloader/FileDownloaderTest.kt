package com.everardo.filedownloader

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.everardo.filedownloader.di.ObjectFactory
import com.everardo.filedownloader.util.anySafe
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations
import java.io.File
import java.lang.IllegalStateException
import java.net.URI

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
    private lateinit var uri: Uri

    @Mock
    private lateinit var downloadListener: DownloadListener

    @Mock
    private lateinit var directory: File

    private lateinit var fileDownloader: FileDownloader


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        whenever(objectFactory.getNotifier()).thenReturn(notifier)
        whenever(objectFactory.context).thenReturn(context)
        whenever(config.objectFactory).thenReturn(objectFactory)
        whenever(directory.path).thenReturn("filepath")
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
    fun downloadDefaultParams() {
        fileDownloader.uri(uri)
                .fileName("filename")
                .download()

        verify(config).timeout
        verify(config).directory
    }

    @Test
    fun downloadOverrideParams() {
        whenever(directory.toURI()).thenReturn(mock(URI::class.java))

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
        fileDownloader.removeListener(downloadListener)

        verify(notifier).removeObserver(anySafe(DownloadListener::class.java))
    }

    @Test
    fun cancel() {
        fileDownloader.cancel(mock(DownloadToken::class.java))

        verify(context).startService(anySafe(Intent::class.java))
    }
}
