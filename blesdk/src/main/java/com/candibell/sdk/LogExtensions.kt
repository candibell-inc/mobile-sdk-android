package com.candibell.sdk

import android.util.Log

fun Any.d(msg: () -> String) {
    if (Log.isLoggable(tag, Log.DEBUG)) d(msg())
}

fun Any.i(msg: () -> String) {
    if (Log.isLoggable(tag, Log.INFO)) i(msg())
}

fun Any.e(msg: () -> String) {
    if (Log.isLoggable(tag, Log.ERROR)) e(msg())
}

fun Any.d(msg: String) {
    d(tag, msg)
}

fun Any.i(msg: String) {
    i(tag, msg)
}

fun Any.e(msg: String) {
    e(tag, msg)
}

fun d(tag: String, msg: String) {
    Log.d(tag, msg)
}

fun i(tag: String, msg: String) {
    Log.i(tag, msg)
}

fun e(tag: String, msg: String) {
    Log.e(tag, msg)
}

private val Any.tag: String
    get() = BaseConstant.LOG_TAG_CANDIBELL_ANDROID