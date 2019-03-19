package com.everardo.filedownloader.manager

import android.net.Uri
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.data.repository.DownloadRepository
import com.everardo.filedownloader.service.Downloader
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class DownloadManagerTest {

    @Mock
    private lateinit var repository: DownloadRepository

    @Mock
    private lateinit var downloader: Downloader

    @Mock
    private lateinit var uri: Uri

    private lateinit var manager: DownloadManager
    private lateinit var token: DownloadToken

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        manager = DownloadManagerImpl(repository, downloader)
        token = DownloadToken(uri, "path", "filename")
    }

    @Test
    fun addPendingDownload() {
        manager.addPendingDownload(token)

        verify(repository).addPendingDownload(token)
    }
}