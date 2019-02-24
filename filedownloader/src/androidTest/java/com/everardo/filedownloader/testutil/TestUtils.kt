package com.everardo.filedownloader.testutil

import org.mockito.Mockito

fun <T> anySafe(type: Class<T>): T {
    Mockito.any<T>(type)
    return uninitialized()
}

private fun <T> uninitialized(): T = null as T
