package com.islam404.movieapp.util

import android.util.Log
import com.islam404.movieapp.util.Constants.debugMode

object AppLogger {
    fun d(tag: String, msg: String) = if (debugMode) Log.d(tag, msg) else Unit
    fun w(tag: String, msg: String) = if (debugMode) Log.w(tag, msg) else Unit
    fun e(tag: String, msg: String, t: Throwable? = null) =
        if (debugMode) Log.e(tag, msg, t) else Unit
}