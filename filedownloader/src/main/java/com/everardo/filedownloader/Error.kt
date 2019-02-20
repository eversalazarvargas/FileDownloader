package com.everardo.filedownloader

data class Error(val code: Code, val httpErrorCode: Int? = 0) {

    val message = code.message

    enum class Code(val message: String) {
        HTTP_ERROR("Http error"),
        DOWNLOAD_ALREADY_IN_PROGRESS("Cannot start download while download is already in progress")
    }
}
