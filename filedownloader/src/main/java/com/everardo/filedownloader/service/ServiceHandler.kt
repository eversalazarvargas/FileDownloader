package com.everardo.filedownloader.service

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.OpenForTesting
import com.everardo.filedownloader.manager.DownloadManager
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor


@OpenForTesting
internal class ServiceHandler(looper: Looper,
                     private val service: CompletableService,
                     private val downloadManager: DownloadManager,
                     private val threadExecutor: ThreadPoolExecutor): Handler(looper) {

    interface CompletableService {
        fun stop(startId: Int)
    }

    companion object {
        const val ADD_PENDING_MSG = 1
        const val TASK_FINISHED_MSG = 2
        const val RETRY_MSG = 3
        const val CANCEL_MSG = 4
    }

    private val downloadTasks: MutableMap<DownloadToken, Future<*>> = HashMap()

    fun handleMessage(msgId: Int, bundle: Bundle, startId: Int) {
        obtainMessage()?.also { msg ->
            msg.what = msgId
            msg.arg1 = startId
            msg.data = bundle
            sendMessage(msg)
        }
    }

    override fun handleMessage(msg: Message) {
        when(msg.what) {
            ADD_PENDING_MSG -> {
                // store pending in db
                val data = msg.data
                val token: DownloadToken = data.getParcelable(DownloadService.TOKEN_EXTRA) as DownloadToken

                if (downloadTasks.containsKey(token)) {
                    Log.e(javaClass.name, "Attempt to download file in progress ${token.fileName}")
                    return
                }

                //TODO improve performance by using Kotlin's co-routines instead of thread pools
                // submit download to Executor
                val future = threadExecutor.submit(DownloadTask(this, downloadManager, token, data.getLong(DownloadService.TIMEOUT_EXTRA), msg.arg1))
                downloadTasks[token] = future
            }
            RETRY_MSG -> {
                val data = msg.data
                val token: DownloadToken = data.getParcelable(DownloadService.TOKEN_EXTRA) as DownloadToken

                if (downloadTasks.containsKey(token)) {
                    Log.e(javaClass.name, "Attempt to download file in progress ${token.fileName}")
                    return
                }

                //TODO improve performance by using Kotlin's co-routines instead of thread pools
                // submit download to Executor
                //TODO we need to use Future and cancel the Task, when we do FileDownloader.cancel(token)
                val future = threadExecutor.submit(DownloadTask(this, downloadManager, token, data.getLong(DownloadService.TIMEOUT_EXTRA), msg.arg1))
                downloadTasks[token] = future
            }
            CANCEL_MSG -> {
                val data = msg.data
                val token: DownloadToken = data.getParcelable(DownloadService.TOKEN_EXTRA) as DownloadToken

                if (downloadTasks.containsKey(token)) {
                    val future = downloadTasks[token]
                    future?.let {
                        if (!it.isCancelled) {
                            it.cancel(true)
                        }
                    }
                }
            }
            TASK_FINISHED_MSG -> {
                val data = msg.data
                val token: DownloadToken = data.getParcelable(DownloadService.TOKEN_EXTRA) as DownloadToken
                if (downloadTasks.containsKey(token)) {
                    downloadTasks.remove(token)
                }

                //TODO Remove this log
                Log.i("PROBANDO", "downloaded of file ${token.fileName} finished")

                // use db manager to check if there are still pending downloads
                // if not then stopSelf()
                if (downloadTasks.isEmpty()) {
                    service.stop(msg.arg1)
                }
            }
        }
    }

    fun shutdown() {
        threadExecutor.shutdownNow()
    }

    internal class DownloadTask(private val serviceHandler: ServiceHandler,
                       private val downloadManager: DownloadManager,
                       private val token: DownloadToken,
                       private val timeout: Long,
                       private val startId: Int) : Runnable {

        override fun run() {
            downloadManager.download(token, timeout)

            serviceHandler.obtainMessage()?.also { msg ->
                msg.what = TASK_FINISHED_MSG
                msg.arg1 = startId

                Bundle().also { bundle ->
                    bundle.putParcelable(DownloadService.TOKEN_EXTRA, token)
                    msg.data = bundle
                }

                serviceHandler.sendMessage(msg)
            }
        }
    }
}