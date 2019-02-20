package com.everardo.filedownloader

import android.net.Uri

data class StatusEvent(val status: Status,
                       val progress: Double,
                       val downloader: FileDownloader,
                       val uri: Uri,
                       val fileName: String,
                       val error: Error? = null) {

}
