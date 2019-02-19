package com.everardo.filedownloader

import android.content.Context
import java.io.File

class FileDownloaderConfig private constructor(builder: Builder) {

    val context = builder.context
    val downloadRegistry = DownloadRegistry(builder.context)
    val directory: File? = builder.directory

    class Builder {

        internal lateinit var context: Context
            private set

        internal var directory: File? = null
            private set

        fun withContext(context: Context): Builder {
            this.context = context
            return this
        }

        fun withFilesDirectory(directory: File): Builder {
            this.directory = directory
            return this
        }

        fun build(): FileDownloaderConfig {
            checkNotNull(context) { "Context cannot be null" }

            directory?.let {
                check(it.exists()) { "Directory must exists" }
                check(it.isDirectory) { "Directory parameter is not a directory"}
            }
            return FileDownloaderConfig(this)
        }
    }
}
