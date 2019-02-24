package com.everardo.filedownloader

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadToken(internal val uri: Uri, private val directoryPath: String, internal val fileName: String) : Parcelable {
    private val timestamp: Long = System.currentTimeMillis()
}