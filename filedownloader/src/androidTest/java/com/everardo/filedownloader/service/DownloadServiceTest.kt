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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.mockito.Mockito.`when` as whenever


@MediumTest
@RunWith(AndroidJUnit4::class)
class DownloadServiceTest {

    @Mock
    private lateinit var objectFactory: ObjectFactory

    @Mock
    private lateinit var downloadManager: DownloadManager

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
    }

    @Test
    fun someTest() {
        doAnswer(object : Answer<Unit> {
            override fun answer(invocation: InvocationOnMock) {
                assertEquals(token, invocation.getArgument(0))
            }
        }).`when`(downloadManager).addPendingDownload(anySafe(DownloadToken::class.java))


        val intent = DownloadService.getDownloadIntent(context, token, 100L)
        mServiceRule.startService(intent)
    }

}
