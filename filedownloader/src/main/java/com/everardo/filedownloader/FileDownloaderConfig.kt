package com.everardo.filedownloader

import android.content.Context
import com.everardo.filedownloader.di.ObjectFactory
import com.everardo.filedownloader.di.ObjectFactoryImpl
import java.io.File

class FileDownloaderConfig private constructor(builder: Builder) {

    internal val objectFactory: ObjectFactory

    val directory: File = builder.directory
    val timeout: Long? = builder.timeout
    val maxDownloadRecords: Int = builder.maxDownloadRecords

    init {
        ObjectFactory.instance = ObjectFactoryImpl(builder.context)
        objectFactory = ObjectFactory.instance
    }

    class Builder {

        internal lateinit var context: Context
            private set

        internal lateinit var directory: File
            private set

        internal var timeout: Long? = null
            private set

        internal var maxDownloadRecords: Int = 10000
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
         * By default it will keep the last 100,000 download records of any [Status]
         *
         * @param numberOfRecords The number of records to keep in the [DownloadRegistry] starting from the last one.
         */
        fun keepLastDownloadRecords(numberOfRecords: Int): Builder {
            this.maxDownloadRecords = numberOfRecords
            return this
        }

        fun build(): FileDownloaderConfig {
            checkNotNull(context) { "Context cannot be null" }
            checkNotNull(directory) { "Directory cannot be null" }
            check(directory.exists()) { "Directory must exists" }
            check(directory.isDirectory) { "Directory parameter is not a directory" }
            check(directory.canRead()) { "Directory must be read enabled" }
            check(directory.canWrite()) { "Directory must be write enabled" }

            timeout?.let {
                check(it > 0) { "Timeout must be greater than zero milliseconds" }
            }

            check(maxDownloadRecords > 0) { "maxDownloadRecords has to be greater than zero" }

            return FileDownloaderConfig(this)
        }
    }
}
