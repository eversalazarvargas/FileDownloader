package com.everardo.filedownloader.manager

import android.net.Uri
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.data.repository.DownloadRepository
import com.everardo.filedownloader.downloader.DownloadResult
import com.everardo.filedownloader.downloader.Downloader
import com.everardo.filedownloader.downloader.ProgressWriter
import com.everardo.filedownloader.util.anySafe
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when` as whenever
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.lang.NullPointerException

class DownloadManagerTest {

    @Mock
    private lateinit var repository: DownloadRepository

    @Mock
    private lateinit var progressWriter: ProgressWriter

    @Mock
    private lateinit var downloader: Downloader

    @Mock
    private lateinit var uri: Uri

    private lateinit var manager: DownloadManager
    private lateinit var token: DownloadToken

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        manager = DownloadManagerImpl(repository, downloader, progressWriter)
        token = DownloadToken(uri, "path", "filename")
    }


    @Test
    fun download() {
        whenever(downloader.downloadFile(anySafe(DownloadToken::class.java),
                any(Long::class.java),
                anySafe(ProgressWriter::class.java))).thenReturn(DownloadResult.SUCCESSFUL)

        manager.download(token, 100)

        verify(downloader).downloadFile(token, 100, progressWriter)
        verify(repository).completeDownload(token, DownloadResult.SUCCESSFUL)
    }

    @Test
    fun download_ThrowException() {
        whenever(downloader.downloadFile(anySafe(DownloadToken::class.java),
                any(Long::class.java),
                anySafe(ProgressWriter::class.java))).thenThrow(NullPointerException())

        manager.download(token, 100)

        verify(downloader).downloadFile(token, 100, progressWriter)
        verify(repository).completeDownload(token, DownloadResult.ERROR)
    }

    @Test
    fun hasPendingDownloads() {
        whenever(repository.hasPendingDownloads()).thenReturn(true)

        val result = manager.hasPendingDownloads()
        verify(repository).hasPendingDownloads()
        assertTrue(result)
    }
}
