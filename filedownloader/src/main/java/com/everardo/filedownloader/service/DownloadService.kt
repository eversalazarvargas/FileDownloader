package com.everardo.filedownloader.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import com.everardo.filedownloader.DownloadToken
import com.everardo.filedownloader.di.getObjectFactory
import com.everardo.filedownloader.service.ServiceHandler.Companion.ADD_PENDING_MSG
import com.everardo.filedownloader.service.ServiceHandler.Companion.CANCEL_MSG
import com.everardo.filedownloader.service.ServiceHandler.Companion.RETRY_MSG


internal class DownloadService: Service(), ServiceHandler.CompletableService {

    companion object {
        const val TOKEN_EXTRA = "DownloadServiceTokenExtra"
        const val TIMEOUT_EXTRA = "DownloadServiceTimeoutExtra"
        const val RETRY_EXTRA = "DownloadServiceRetryExtra"
        const val CANCEL_EXTRA = "DownloadServiceCancelExtra"

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

        fun getCancelIntent(context: Context, downloadToken: DownloadToken): Intent {
            val intent = Intent(context, DownloadService::class.java)
            intent.putExtra(CANCEL_EXTRA, true)
            intent.putExtra(TOKEN_EXTRA, downloadToken)
            return intent
        }
    }

    private lateinit var handler: ServiceHandler

    override fun onCreate() {
        val downloadManager = getObjectFactory().downloadManager
        val threadExecutor = getObjectFactory().getNewThreadExecutor()
        handler = getObjectFactory().getServiceHandler(this@DownloadService, downloadManager, threadExecutor)
    }

    override fun onBind(intent: Intent?): IBinder? = Binder()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val bundle = Bundle().apply {
            putParcelable(TOKEN_EXTRA, intent.getParcelableExtra(TOKEN_EXTRA))
            putLong(TIMEOUT_EXTRA, intent.getLongExtra(TIMEOUT_EXTRA, 3L * 60 * 1000))
        }

        when {
            intent.getBooleanExtra(RETRY_EXTRA, false) -> handler.handleMessage(RETRY_MSG, bundle, startId)
            intent.getBooleanExtra(CANCEL_EXTRA, false) -> handler.handleMessage(CANCEL_MSG, bundle, startId)
            else -> handler.handleMessage(ADD_PENDING_MSG, bundle, startId)
        }

        return START_REDELIVER_INTENT
    }

    override fun stop(startId: Int) {
        stopSelf(startId)
    }

    override fun onDestroy() {
        handler.shutdown()
        super.onDestroy()
    }
}
