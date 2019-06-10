package com.everardo.filedownloader.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.di.getObjectFactory
import com.everardo.filedownloader.manager.DownloadManager
import java.util.concurrent.ThreadPoolExecutor


internal class DownloadService: Service() {

    companion object {
        const val TOKEN_EXTRA = "DownloadServiceTokenExtra"
        const val TIMEOUT_EXTRA = "DownloadServiceTimeoutExtra"
        const val RETRY_EXTRA = "DownloadServiceRetryExtra"

        private const val ADD_PENDING_MSG = 1
        private const val TASK_FINISHED_MSG = 2
        private const val RETRY_MSG = 3

        fun getDownloadIntent(context: Context, downloadToken: DownloadToken, timeout: Long): Intent {
            val intent = Intent(context, DownloadService::class.java)
            intent.putExtra(TOKEN_EXTRA, downloadToken)
            intent.putExtra(TIMEOUT_EXTRA, timeout)
            return intent
        }

        fun getRetryIntent(context: Context, downloadToken: DownloadToken, timeout: Long): Intent {
            val intent = Intent(context, DownloadService::class.java)
            intent.putExtra(RETRY_EXTRA, true)
            intent.putExtra(TOKEN_EXTRA, downloadToken)
            intent.putExtra(TIMEOUT_EXTRA, timeout)
            return intent
        }
    }

    private lateinit var handler: Handler
    private lateinit var downloadManager: DownloadManager
    private lateinit var threadExecutor: ThreadPoolExecutor

    override fun onCreate() {
        downloadManager = getObjectFactory().downloadManager
        threadExecutor = getObjectFactory().getNewThreadExecutor()

        HandlerThread("ServiceStartArguments").apply {
            start()
            handler = ServiceHandler(looper)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = Binder()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        handler.obtainMessage()?.also { msg ->
            msg.what = if (intent.getBooleanExtra(RETRY_EXTRA, false)) RETRY_MSG else ADD_PENDING_MSG
            msg.arg1 = startId
            Bundle().also { bundle ->
                bundle.putParcelable(TOKEN_EXTRA, intent.getParcelableExtra(TOKEN_EXTRA))
                bundle.putLong(TIMEOUT_EXTRA, intent.getLongExtra(TIMEOUT_EXTRA, 3L * 60 * 1000))
                msg.data = bundle
            }

            handler.sendMessage(msg)
        }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        threadExecutor.shutdown()
        super.onDestroy()
    }

    inner class ServiceHandler(looper: Looper): Handler(looper) {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                ADD_PENDING_MSG -> {
                    // store pending in db
                    val data = msg.data
                    val token: DownloadToken = data.getParcelable(TOKEN_EXTRA) as DownloadToken
                    downloadManager.addPendingDownload(token)

                    //TODO improve performance by using Kotlin's co-routines instead of thread pools
                    // submit download to Executor
                    threadExecutor.execute(DownloadTask(this, downloadManager, token, data.getLong(TIMEOUT_EXTRA), msg.arg1))
                }
                RETRY_MSG -> {
                    val data = msg.data
                    val token: DownloadToken = data.getParcelable(TOKEN_EXTRA) as DownloadToken

                    //TODO improve performance by using Kotlin's co-routines instead of thread pools
                    // submit download to Executor
                    threadExecutor.execute(DownloadTask(this, downloadManager, token, data.getLong(TIMEOUT_EXTRA), msg.arg1))
                }
                TASK_FINISHED_MSG -> {
                    // use db manager to check if there are still pending downloads
                    // if not then stopSelf()
                    if (!downloadManager.hasPendingDownloads()) {
                        stopSelf(msg.arg1)
                    }
                }
            }
        }
    }

    class DownloadTask(private val serviceHandler: ServiceHandler,
                       private val downloadManager: DownloadManager,
                       private val token: DownloadToken,
                       private val timeout: Long,
                       private val startId: Int) : Runnable {

        override fun run() {
            downloadManager.download(token, timeout)
            serviceHandler.obtainMessage()?.also { msg ->
                msg.what = TASK_FINISHED_MSG
                msg.arg1 = startId

                serviceHandler.sendMessage(msg)
            }
        }
    }
}
