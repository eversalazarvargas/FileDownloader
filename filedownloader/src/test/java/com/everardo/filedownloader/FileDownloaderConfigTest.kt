package com.everardo.filedownloader

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when` as whenever
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import java.io.File


/**
 * @author everardo.salazar on 2/22/19
 */
class FileDownloaderConfigTest {

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

    }

    @Test
    fun simpleConfig() {
        val context = mock(Context::class.java)
        val directory = mock(File::class.java)
        whenever(directory.exists()).thenReturn(true)
        whenever(directory.isDirectory).thenReturn(true)
        whenever(directory.canRead()).thenReturn(true)
        whenever(directory.canWrite()).thenReturn(true)

        val config = FileDownloaderConfig.Builder()
                .context(context)
                .filesDirectory(directory)
                .timeout(20)
                .keepLastDownloadRecords(100)
                .build()

        assertEquals(directory, config.directory)
        assertEquals(20L, config.timeout)
        assertEquals(100, config.maxDownloadRecords)
    }
}
