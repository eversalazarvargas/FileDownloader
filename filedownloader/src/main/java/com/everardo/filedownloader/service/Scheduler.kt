package com.everardo.filedownloader.service

import android.content.Context
import com.everardo.filedownloader.DownloadToken

internal interface Scheduler {
    fun download(token: DownloadToken, timeout: Long)
    fun retry(token: DownloadToken, timeout: Long)
}

internal class SchedulerImpl(private val context: Context): Scheduler {
    override fun download(token: DownloadToken, timeout: Long) {
        //TODO plan for use a SDK JobScheduler when Android version is > 21
        context.startService(DownloadService.getDownloadIntent(context, token, timeout))
    }

    override fun retry(token: DownloadToken, timeout: Long) {
        //TODO plan for use a SDK JobScheduler when Android version is > 21
        context.startService(DownloadService.getRetryIntent(context, token, timeout))
    }
}
