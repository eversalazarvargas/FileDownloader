package com.everardo.filedownloader

import android.content.Context
import java.util.*

class DownloadRegistry(private val context: Context) {
    fun getCompleted(): List<DownloadInfo> {
        TODO()
    }
    fun getCompleted(startTime: Date, endTime: Date): List<DownloadInfo> {
        TODO()
    } // interval, it searchs over endDownloadTime
    fun getInProgress(): List<DownloadInfo> {
        TODO()
    }
    fun getInProgress(startTime: Date, endTime: Date): List<DownloadInfo> {
        TODO()
    } // interval, it considers startDownloadTime
    fun getCancelled(): List<DownloadInfo> {
        TODO()
    }
    fun getCancelled(startTime: Date, endTime: Date): List<DownloadInfo> {
        TODO()
    } // interval, it considers startDownloadTime
    fun getErrors(): List<DownloadInfo> {
        TODO()
    }
    fun getErrors(startTime: Date, endTime: Date): List<DownloadInfo> {
        TODO()
    } // interval, it considers startDownloadTime
}
