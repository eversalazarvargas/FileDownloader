package com.everardo.filedownloader.service

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ServiceTestRule
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.di.ObjectFactory
import com.everardo.filedownloader.manager.DownloadManager
import com.everardo.filedownloader.testutil.anySafe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import org.mockito.Mockito.`when` as whenever
import com.everardo.filedownloader.service.DownloadService.DownloadTask
import org.mockito.Mockito.verify


@MediumTest
@RunWith(AndroidJUnit4::class)
class DownloadServiceTest {

    @Mock
    private lateinit var objectFactory: ObjectFactory

    @Mock
    private lateinit var downloadManager: DownloadManager

    @Mock
    private lateinit var threadExecutor: ThreadPoolExecutor

    private lateinit var context: Context
    private val uri = Uri.parse("http://authority/path")
    private lateinit var token: DownloadToken

    @get:Rule
    val mServiceRule = ServiceTestRule()


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        context = getApplicationContext()

        ObjectFactory.instance = objectFactory
        token = DownloadToken(uri, "path", "filename")
        whenever(objectFactory.downloadManager).thenReturn(downloadManager)
        whenever(objectFactory.getNewThreadExecutor()).thenReturn(threadExecutor)
    }

    @Test
    fun addPending() {
        val latch = CountDownLatch(1)
        var addPendingCalled = false

        doAnswer(object : Answer<Unit> {
            override fun answer(invocation: InvocationOnMock) {
                assertEquals(token, invocation.getArgument(0))
                addPendingCalled = true
                latch.countDown()
            }
        }).`when`(downloadManager).addPendingDownload(anySafe(DownloadToken::class.java))


        val intent = DownloadService.getDownloadIntent(context, token, 100L)
        mServiceRule.startService(intent)
        latch.await(1, TimeUnit.SECONDS)
        assertTrue(addPendingCalled)
    }

    @Test
    fun executeDownloadNoPendingDownloads() {

        val latch = CountDownLatch(1)
        val latchTaskFinished = CountDownLatch(1)

        doAnswer(object: Answer<Unit> {
            override fun answer(invocation: InvocationOnMock) {
                val task = invocation.getArgument<DownloadTask>(0)
                task.run()
                latch.countDown()
            }
        }).`when`(threadExecutor).execute(anySafe(DownloadTask::class.java))

        doAnswer(object: Answer<Boolean> {
            override fun answer(invocation: InvocationOnMock): Boolean {
                latchTaskFinished.countDown()
                return false
            }
        }).`when`(downloadManager).hasPendingDownloads()

        val intent = DownloadService.getDownloadIntent(context, token, 100L)
        mServiceRule.startService(intent)
        latch.await(1, TimeUnit.SECONDS)
        verify(downloadManager).download(token, 100L)
        latchTaskFinished.await(1, TimeUnit.SECONDS)
        verify(downloadManager).hasPendingDownloads()
    }
}
