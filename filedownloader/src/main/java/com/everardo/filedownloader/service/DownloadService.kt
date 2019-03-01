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


internal class DownloadService: Service() {

    companion object {
        const val TOKEN_EXTRA = "DownloadServiceTokenExtra"
        const val TIMEOUT_EXTRA = "DownloadServiceTimeoutExtra"

        private const val ADD_PENDING = 1
        private const val TASK_FINISHED = 2

        fun getDownloadIntent(context: Context, downloadToken: DownloadToken, timeout: Long): Intent {
            val intent = Intent(context, DownloadService::class.java)
            intent.putExtra(TOKEN_EXTRA, downloadToken)
            intent.putExtra(TIMEOUT_EXTRA, timeout)
            return intent
        }

        fun getRetryIntent(context: Context, downloadToken: DownloadToken): Intent {
            val intent = Intent(context, DownloadService::class.java)
            //TODO make extras parceable and add extras
            return intent
        }
    }

    private lateinit var handler: Handler
    private lateinit var downloadManager: DownloadManager

    override fun onCreate() {
        downloadManager = getObjectFactory().downloadManager

        HandlerThread("ServiceStartArguments").apply {
            start()
            handler = ServiceHandler(looper)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = Binder()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        handler.obtainMessage()?.also { msg ->
            msg.what = ADD_PENDING
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

    inner class ServiceHandler(looper: Looper): Handler(looper) {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                ADD_PENDING -> {
                    //TODO store pending in db
                    val data = msg.data
                    downloadManager.addPendingDownload(data.getParcelable(TOKEN_EXTRA) as DownloadToken)

                    // TODO submit download to Executor
                }
                TASK_FINISHED -> {
                    //TODO use db manager to check if there are still pending downloads
                    // TODO if not then stopSelf()
                }
            }
        }
    }
}
