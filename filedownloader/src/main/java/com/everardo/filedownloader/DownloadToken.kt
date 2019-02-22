package com.everardo.filedownloader

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadToken(private val uri: Uri, private val fileName: String) : Parcelable {
    private val timestamp: Long = System.currentTimeMillis()
}