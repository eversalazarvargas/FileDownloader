package com.everardo.filedownloader.data.repository

import android.database.ContentObserver
import android.net.Uri
import android.os.Parcelable
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.Error
import com.everardo.filedownloader.OpenForTesting
import com.everardo.filedownloader.Status
import com.everardo.filedownloader.downloader.DownloadResult
import kotlinx.android.parcel.Parcelize


/**
 * @author everardo.salazar on 2/22/19
 */
@OpenForTesting
internal interface DownloadRepository {
    fun addPendingDownload(token: DownloadToken)
    fun completeDownload(token: DownloadToken, result: DownloadResult)
    fun hasPendingDownloads(): Boolean
    fun registerContentObserver(contentObserver: ContentObserver)
    fun unregisterContentObserver(contentObserver: ContentObserver)
    fun getDataStatusChange(uri: Uri): DataStatusChange
}

@Parcelize
internal data class DataStatusChange(val status: Status,
                                     val token: DownloadToken,
                                     val progress: Double,
                                     val error: Error? = null): Parcelable

/**
 * @author everardo.salazar on 2/22/19
 */
internal class DownloadRepositoryImpl: DownloadRepository {
    override fun addPendingDownload(token: DownloadToken) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun completeDownload(token: DownloadToken, result: DownloadResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasPendingDownloads(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerContentObserver(contentObserver: ContentObserver) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterContentObserver(contentObserver: ContentObserver) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDataStatusChange(uri: Uri): DataStatusChange {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
