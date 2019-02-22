package com.everardo.filedownloader.di

import android.content.Context
import com.everardo.filedownloader.DownloadRegistry
import com.everardo.filedownloader.Notifier
import com.everardo.filedownloader.NotifierImpl

internal interface ObjectFactory {

    companion object {
        lateinit var instance: ObjectFactory
    }

    val downloadRegistry: DownloadRegistry
    val context: Context

    fun getNotifier(): Notifier
}

internal class ObjectFactoryImpl(override val context: Context): ObjectFactory {

    override val downloadRegistry: DownloadRegistry
        get() = DownloadRegistry(context)

    override fun getNotifier(): Notifier = NotifierImpl()
}

internal fun getObjectFactory() = ObjectFactory.instance

