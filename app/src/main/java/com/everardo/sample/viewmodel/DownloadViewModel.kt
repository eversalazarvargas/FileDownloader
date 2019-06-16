package com.everardo.sample.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.everardo.filedownloader.AbstractDownloadListener
import com.everardo.filedownloader.StatusEvent
import com.everardo.sample.DownloaderProvider

class DownloadViewModel(provider: DownloaderProvider): ViewModel() {

    private val fileDownloader = provider.getFileDownloader()
    private val complete = MutableLiveData<StatusEvent>()

    fun download(fileUrl: String) {

        fileDownloader.uri(Uri.parse(fileUrl))
                .fileName("myfile")
                .listener(object : AbstractDownloadListener() {
                    override fun onCompleted(status: StatusEvent) {
                        complete.value = status
                    }
                })
                .download()
    }

    fun getComplete(): LiveData<StatusEvent> = complete
}