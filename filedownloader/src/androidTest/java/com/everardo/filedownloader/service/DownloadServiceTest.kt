package com.everardo.filedownloader.service

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ServiceTestRule
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.di.ObjectFactory
import com.everardo.filedownloader.manager.DownloadManager
import com.everardo.filedownloader.testutil.anySafe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.ThreadPoolExecutor
import org.mockito.Mockito.`when` as whenever
import org.mockito.ArgumentMatchers
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

    @Mock
    private lateinit var serviceHandler: ServiceHandler

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
        whenever(objectFactory.getServiceHandler(
                anySafe(ServiceHandler.CompletableService::class.java),
                anySafe(DownloadManager::class.java),
                anySafe(ThreadPoolExecutor::class.java))).thenReturn(serviceHandler)
    }

    @Test
    fun addPending() {
        val intent = DownloadService.getDownloadIntent(context, token, 100L)
        mServiceRule.startService(intent)
        verify(serviceHandler).handleMessage(ArgumentMatchers.anyInt(), anySafe(Bundle::class.java), ArgumentMatchers.anyInt())
    }
}
