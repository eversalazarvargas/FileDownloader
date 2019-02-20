package com.everardo.filedownloader

import android.content.Context
import java.io.File

class FileDownloaderConfig private constructor(builder: Builder) {

    val context = builder.context
    val downloadRegistry = DownloadRegistry(builder.context)
    val directory: File? = builder.directory
    val timeout: Long? = builder.timeout

    class Builder {

        internal lateinit var context: Context
            private set

        internal var directory: File? = null
            private set

        internal var timeout: Long? = null
            private set

        internal var maxDownloadRecords: Int? = null
            private set

        fun context(context: Context): Builder {
            this.context = context
            return this
        }

        fun filesDirectory(directory: File): Builder {
            this.directory = directory
            return this
        }

        fun timeout(timeout: Long): Builder {
            this.timeout = timeout
            return this
        }

        /**
         * Keeps the last numberOfRecords records in the [DownloadRegistry].
         * This doesn't delete the actual files but just delete the records from the database.
         * By default it will keep the last 100 download records of any [Status]
         *
         * @param numberOfRecords The number of records to keep in the [DownloadRegistry] starting from the last one.
         */
        fun keepLastDownloadRecords(numberOfRecords: Int): Builder {
            this.maxDownloadRecords = maxDownloadRecords
            return this
        }

        fun build(): FileDownloaderConfig {
            checkNotNull(context) { "Context cannot be null" }

            directory?.let {
                check(it.exists()) { "Directory must exists" }
                check(it.isDirectory) { "Directory parameter is not a directory"}
            }

            timeout?.let {
                check(it > 0) { "Timeout must be greater than zero milliseconds" }
            }

            maxDownloadRecords?.let {
                check(it > 0) { "maxDownloadRecords has to be greater than zero" }
            }

            return FileDownloaderConfig(this)
        }
    }
}
