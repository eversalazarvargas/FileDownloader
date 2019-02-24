package com.everardo.filedownloader.data.repository

import android.os.Parcelable
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.Error
import com.everardo.filedownloader.Status
import kotlinx.android.parcel.Parcelize


/**
 * @author everardo.salazar on 2/22/19
 */
internal interface DownloadRepository {
    fun observe(statusChangeListener: StatusChangeListener)
}

internal interface StatusChangeListener {
    fun onStatusChange(statusEvent: DataStatusChange)
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

    override fun observe(statusChangeListener: StatusChangeListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
