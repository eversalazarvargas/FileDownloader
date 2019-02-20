package com.everardo.filedownloader

import android.content.Context
import java.util.*

class DownloadRegistry(private val context: Context) {
    fun getCompleted(): List<DownloadInfo> {
        TODO()
    }

    // interval, it searchs over endDownloadTime
    fun getCompleted(start: Date, end: Date): List<DownloadInfo> {
        TODO()
    }
    fun getInProgress(): List<DownloadInfo> {
        TODO()
    }

    // interval, it considers startDownloadTime
    fun getInProgress(start: Date, end: Date): List<DownloadInfo> {
        TODO()
    }
    fun getCancelled(): List<DownloadInfo> {
        TODO()
    }

    // interval, it considers startDownloadTime
    fun getCancelled(start: Date, end: Date): List<DownloadInfo> {
        TODO()
    }
    fun getErrors(): List<DownloadInfo> {
        TODO()
    }

    // interval, it considers startDownloadTime
    fun getErrors(start: Date, end: Date): List<DownloadInfo> {
        TODO()
    }
}
