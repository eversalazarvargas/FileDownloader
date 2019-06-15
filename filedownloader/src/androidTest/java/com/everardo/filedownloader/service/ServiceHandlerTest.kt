package com.everardo.filedownloader.service

import android.net.Uri
import android.os.Bundle
import android.os.HandlerThread
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.manager.DownloadManager
import com.everardo.filedownloader.service.ServiceHandler.Companion.ADD_PENDING_MSG
import com.everardo.filedownloader.service.ServiceHandler.Companion.CANCEL_MSG
import com.everardo.filedownloader.testutil.anySafe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when` as whenever
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@MediumTest
@RunWith(AndroidJUnit4::class)
class ServiceHandlerTest {

    @Mock
    private lateinit var service: ServiceHandler.CompletableService

    @Mock
    private lateinit var downloadManager: DownloadManager

    @Mock
    private lateinit var threadExecutor: ThreadPoolExecutor

    @Mock
    private lateinit var future: Future<*>

    private val uri = Uri.parse("http://authority/path")
    private lateinit var token: DownloadToken
    private lateinit var serviceHandler: ServiceHandler
    private lateinit var bundle: Bundle

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        token = DownloadToken(uri, "path", "filename")

        HandlerThread("ServiceStartArguments").apply {
            start()
            serviceHandler = ServiceHandler(looper, service, downloadManager, threadExecutor)
        }

        bundle = Bundle().apply {
            putParcelable(DownloadService.TOKEN_EXTRA, token)
            putLong(DownloadService.TIMEOUT_EXTRA, 3L * 60 * 1000)
        }
    }

    @Test
    fun addPending() {

        val latch = CountDownLatch(1)

        doAnswer(object : Answer<Future<*>> {
            override fun answer(invocation: InvocationOnMock): Future<*> {
                val task = invocation.getArgument<ServiceHandler.DownloadTask>(0)
                task.run()
                latch.countDown()
                return future
            }
        }).`when`(threadExecutor).submit(anySafe(ServiceHandler.DownloadTask::class.java))

        serviceHandler.handleMessage(ADD_PENDING_MSG, bundle, 1)
        latch.await(1, TimeUnit.SECONDS)

        verify(service).stop(1)
    }

    @Test
    fun cancel() {
        val latch = CountDownLatch(1)

        doAnswer(object : Answer<Future<*>> {
            override fun answer(invocation: InvocationOnMock): Future<*> {
                latch.countDown()
                return future
            }
        }).`when`(threadExecutor).submit(anySafe(ServiceHandler.DownloadTask::class.java))

        whenever(future.isCancelled).thenReturn(false)

        serviceHandler.handleMessage(ADD_PENDING_MSG, bundle, 1)
        latch.await(1, TimeUnit.SECONDS)
        serviceHandler.handleMessage(CANCEL_MSG, bundle, 1)

        Thread.sleep(500)
        verify(future).cancel(true)
    }

    @Test
    fun cancel_Already_Cancelled() {
        val latch = CountDownLatch(1)

        doAnswer(object : Answer<Future<*>> {
            override fun answer(invocation: InvocationOnMock): Future<*> {
                latch.countDown()
                return future
            }
        }).`when`(threadExecutor).submit(anySafe(ServiceHandler.DownloadTask::class.java))

        whenever(future.isCancelled).thenReturn(true)

        serviceHandler.handleMessage(ADD_PENDING_MSG, bundle, 1)
        latch.await(1, TimeUnit.SECONDS)
        serviceHandler.handleMessage(CANCEL_MSG, bundle, 1)

        verify(future, times(0)).cancel(true)
    }

    @Test
    fun serviceNoStop() {
        val latch = CountDownLatch(1)

        doAnswer(object : Answer<Future<*>> {
            override fun answer(invocation: InvocationOnMock): Future<*> {
                latch.countDown()
                return future
            }
        }).`when`(threadExecutor).submit(anySafe(ServiceHandler.DownloadTask::class.java))

        whenever(future.isCancelled).thenReturn(false)

        serviceHandler.handleMessage(ADD_PENDING_MSG, bundle, 1)
        latch.await(1, TimeUnit.SECONDS)

        val otherToken = DownloadToken(uri, "path", "otherfile")

        val otherBundle = Bundle().apply {
            putParcelable(DownloadService.TOKEN_EXTRA, otherToken)
            putLong(DownloadService.TIMEOUT_EXTRA, 3L * 60 * 1000)
        }

        val secondLatch = CountDownLatch(1)

        doAnswer(object : Answer<Future<*>> {
            override fun answer(invocation: InvocationOnMock): Future<*> {
                val task = invocation.getArgument<ServiceHandler.DownloadTask>(0)
                task.run()
                secondLatch.countDown()
                return future
            }
        }).`when`(threadExecutor).submit(anySafe(ServiceHandler.DownloadTask::class.java))

        serviceHandler.handleMessage(ADD_PENDING_MSG, otherBundle, 1)
        secondLatch.await(1, TimeUnit.SECONDS)

        verifyZeroInteractions(service)
        verify(downloadManager).download(otherToken, 3L * 60 * 1000)
    }
}
