package com.everardo.filedownloader

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.everardo.filedownloader.data.repository.DataStatusChange
import com.everardo.filedownloader.data.repository.DownloadRepository
import com.everardo.filedownloader.di.ObjectFactory
import com.everardo.filedownloader.manager.DownloadManager
import com.everardo.filedownloader.service.SchedulerImpl
import com.everardo.filedownloader.testutil.anySafe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.doAnswer
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import org.mockito.Mockito.`when` as whenever

@LargeTest
@RunWith(AndroidJUnit4::class)
class FileDownloaderIntegrationTest {

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

    @Mock
    private lateinit var downloadRepository: DownloadRepository

    @Mock
    private lateinit var downloadManager: DownloadManager

    @Mock
    private lateinit var objectFactory: ObjectFactory

    @Mock
    private lateinit var config: FileDownloaderConfig

    private lateinit var executor: ThreadPoolExecutor
    private lateinit var fileDownloader: FileDownloader
    private lateinit var notifier: Notifier

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        val context: Context = getApplicationContext()

        val decodeWorkQueue: BlockingQueue<Runnable> = LinkedBlockingQueue<Runnable>()
        executor = ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                decodeWorkQueue
        )

        ObjectFactory.instance = objectFactory
        whenever(objectFactory.downloadRepository).thenReturn(downloadRepository)
        whenever(objectFactory.downloadManager).thenReturn(downloadManager)
        whenever(objectFactory.scheduler).thenReturn(SchedulerImpl(context))
        whenever(objectFactory.getNewThreadExecutor()).thenReturn(executor)

        whenever(config.objectFactory).thenReturn(objectFactory)
        whenever(config.directory).thenReturn(context.filesDir)

        fileDownloader = FileDownloader(config)

        whenever(objectFactory.getNotifier(anySafe(FileDownloader::class.java))).thenReturn(NotifierImpl(context, fileDownloader, downloadRepository))

    }

    @Test
    fun simple() {
        val latch = CountDownLatch(2)

        var statusObserver: NotifierImpl.StatusContentObserver? = null
        var fileOneFinished = false
        var fileTwoFinished = false

        whenever(downloadManager.hasPendingDownloads()).thenReturn(false)

        doAnswer(object: Answer<Unit> {
            override fun answer(invocation: InvocationOnMock?) {
                statusObserver = invocation!!.getArgument(0)
            }
        }).`when`(downloadRepository).registerContentObserver(anySafe(NotifierImpl.StatusContentObserver::class.java))


        doAnswer(object: Answer<Unit> {
            override fun answer(invocation: InvocationOnMock?) {
                val token: DownloadToken = invocation!!.getArgument(0)

                when(token.fileName) {
                    "file1" -> {
                        Thread.sleep(3 * 1000)
                        val dataStatusChange = DataStatusChange(Status.COMPLETED, token, 1.0)
                        whenever(downloadRepository.getDataStatusChange(token.uri)).thenReturn(dataStatusChange)
                        statusObserver?.let {
                            it.onChange(true, token.uri)
                        }
                    }
                    "file2" -> {
                        Thread.sleep(2 * 1000)
                        val dataStatusChange = DataStatusChange(Status.ERROR, token, 0.5)
                        whenever(downloadRepository.getDataStatusChange(token.uri)).thenReturn(dataStatusChange)
                        statusObserver?.let {
                            it.onChange(true, token.uri)
                        }
                    }
                }
            }
        }).`when`(downloadManager).download(anySafe(DownloadToken::class.java), anyLong())

        fileDownloader.uri(Uri.parse("https://www.google.com"))
                .fileName("file1")
                .listener(object: DownloadListener {
                    override fun onChange(status: StatusEvent) {
                        assertEquals("file1", status.fileName)
                        assertEquals(Status.COMPLETED, status.status)
                        assertEquals(1.0, status.progress, 0.05)
                        fileOneFinished = true
                        latch.countDown()
                    }
                })
                .download()

        fileDownloader.uri(Uri.parse("https://www.google.com/subpath"))
                .fileName("file2")
                .listener(object: DownloadListener {
                    override fun onChange(status: StatusEvent) {
                        assertEquals("file2", status.fileName)
                        assertEquals(Status.ERROR, status.status)
                        assertEquals(0.5, status.progress, 0.05)
                        fileTwoFinished = true
                        latch.countDown()
                    }
                })
                .download()

        latch.await(10, TimeUnit.SECONDS)
        assertTrue(fileOneFinished)
        assertTrue(fileTwoFinished)
    }
}
