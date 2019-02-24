package com.everardo.filedownloader.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.everardo.filedownloader.DownloadToken


internal class DownloadService: IntentService("DownloadService") {

    companion object {
        const val TOKEN_EXTRA = "DownloadServiceTokenExtra"
        const val TIMEOUT_EXTRA = "DownloadServiceTimeoutExtra"

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

    override fun onHandleIntent(intent: Intent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
