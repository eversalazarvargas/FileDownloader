package com.everardo.filedownloader

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.support.test.InstrumentationRegistry
import com.everardo.filedownloader.data.repository.DataStatusChange
import com.everardo.filedownloader.data.repository.DownloadRepository
import com.everardo.filedownloader.testutil.anySafe
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.`when` as whenever
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NotifierTest {

    @Mock
    private lateinit var fileDownloader: FileDownloader

    @Mock
    private lateinit var repository: DownloadRepository

    @Mock
    private lateinit var listener: DownloadListener

    private lateinit var dataStatusChange: DataStatusChange

    private lateinit var notifier: Notifier
    private lateinit var context: Context
    private lateinit var token: DownloadToken
    private val uri = Uri.parse("http://authority/path")

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        token = DownloadToken(uri, "path", "filename")
        dataStatusChange = DataStatusChange(Status.IN_PROGRESS, token, 5.0, mock(Error::class.java))

        context = InstrumentationRegistry.getTargetContext()

        whenever(repository.getDataStatusChange(anySafe(Uri::class.java))).thenReturn(dataStatusChange)

        notifier = NotifierImpl(context, fileDownloader, repository)
    }

    @Test
    fun registerContentObserver() {
        notifier.addObserver(token, listener)

        verify(repository).registerContentObserver(anySafe(NotifierImpl.StatusContentObserver::class.java))
    }

    @Test
    fun notRegisterContentObserver() {
        notifier.addObserver(token, listener)
        notifier.addObserver(mock(DownloadToken::class.java), mock(DownloadListener::class.java))

        verify(repository, times(1)).registerContentObserver(anySafe(NotifierImpl.StatusContentObserver::class.java))
    }

    @Test
    fun unregisterContentObserver() {
        notifier.addObserver(token, listener)
        notifier.removeObserver(token)

        verify(repository, times(1)).registerContentObserver(anySafe(NotifierImpl.StatusContentObserver::class.java))
        verify(repository, times(1)).unregisterContentObserver(anySafe(NotifierImpl.StatusContentObserver::class.java))
    }

    @Test
    fun notUnregisterContentObserver() {
        notifier.addObserver(token, listener)
        notifier.addObserver(mock(DownloadToken::class.java), mock(DownloadListener::class.java))
        notifier.removeObserver(token)

        verify(repository, times(1)).registerContentObserver(anySafe(NotifierImpl.StatusContentObserver::class.java))
        verify(repository, times(0)).unregisterContentObserver(anySafe(NotifierImpl.StatusContentObserver::class.java))
    }

    @Test
    fun listenerNotified() {
        val latch = CountDownLatch(1)
        var listenerInvokedTimes = 0

        val listener = object: DownloadListener {
            override fun onChange(status: StatusEvent) {
                listenerInvokedTimes++

                assertEquals(token, status.token)
                assertEquals("filename", status.fileName)
                assertEquals(5.0, status.progress, 0.05)
                latch.countDown()
            }
        }

        var observer: ContentObserver? = null
        doAnswer(object: Answer<Unit> {
            override fun answer(invocation: InvocationOnMock) {
                observer = invocation.getArgument(0)
            }
        }).`when`(repository).registerContentObserver(anySafe(ContentObserver::class.java))

        notifier.addObserver(token, listener)
        observer!!.onChange(true, uri)

        latch.await(1, TimeUnit.SECONDS)
        assertEquals(1, listenerInvokedTimes)
    }

    @Test
    fun twoListenersNotified() {
        val otherToken = token.copy(directoryPath = "otherpath")
        val dataStatusChange2 = DataStatusChange(Status.COMPLETED, otherToken, 100.0, mock(Error::class.java))
        val uri2 = Uri.parse("http://authority/otherpath")

        whenever(repository.getDataStatusChange(uri)).thenReturn(dataStatusChange)
        whenever(repository.getDataStatusChange(uri2)).thenReturn(dataStatusChange2)

        val latch = CountDownLatch(2)
        var listenerInvokedTimes = 0

        val listener = object: DownloadListener {
            override fun onChange(status: StatusEvent) {
                listenerInvokedTimes++

                assertEquals(Status.IN_PROGRESS, status.status)
                assertEquals(token, status.token)
                assertEquals("filename", status.fileName)
                assertEquals(5.0, status.progress, 0.05)
                latch.countDown()
            }
        }

        val listener2 = object: DownloadListener {
            override fun onChange(status: StatusEvent) {
                listenerInvokedTimes++

                assertEquals(Status.COMPLETED, status.status)
                assertEquals(otherToken, status.token)
                assertEquals("filename", status.fileName)
                assertEquals(100.0, status.progress, 0.05)
                latch.countDown()
            }
        }

        var observer: ContentObserver? = null
        doAnswer(object: Answer<Unit> {
            override fun answer(invocation: InvocationOnMock) {
                observer = invocation.getArgument(0)
            }
        }).`when`(repository).registerContentObserver(anySafe(ContentObserver::class.java))

        notifier.addObserver(token, listener)
        notifier.addObserver(otherToken, listener2)
        observer!!.onChange(true, uri)
        observer!!.onChange(true, uri2)

        latch.await(1, TimeUnit.SECONDS)
        assertEquals(2, listenerInvokedTimes)
    }

    @Test
    fun listenerNotInMap() {
        val latch = CountDownLatch(1)
        var listenerInvokedTimes = 0

        val listener = object: DownloadListener {
            override fun onChange(status: StatusEvent) {
                listenerInvokedTimes++
                latch.countDown()
            }
        }

        var observer: ContentObserver? = null
        doAnswer(object: Answer<Unit> {
            override fun answer(invocation: InvocationOnMock) {
                observer = invocation.getArgument(0)
            }
        }).`when`(repository).registerContentObserver(anySafe(ContentObserver::class.java))

        val otherToken = token.copy(directoryPath = "otherpath")
        notifier.addObserver(otherToken, listener)
        observer!!.onChange(true, uri)

        latch.await(1, TimeUnit.SECONDS)
        assertEquals(0, listenerInvokedTimes)
    }
}
