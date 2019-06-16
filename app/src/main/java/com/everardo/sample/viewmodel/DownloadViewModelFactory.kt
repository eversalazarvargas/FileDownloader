package com.everardo.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.everardo.sample.DownloaderProvider

class DownloadViewModelFactory(private val provider: DownloaderProvider): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DownloadViewModel::class.java!!)) {
            return DownloadViewModel(provider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}