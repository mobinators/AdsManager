package com.mobinators.ads.manager.extensions

import android.util.Log

inline fun <reified T> T.TAG() = "TAG"
inline fun <reified T> T.logV(message: String) = Log.v(this.TAG(), message)
inline fun <reified T> T.logD(message: String) = Log.d(this.TAG(), message)
inline fun <reified T> T.logW(message: String) = Log.w(this.TAG(), message)
inline fun <reified T> T.logI(message: String) = Log.i(this.TAG(), message)
inline fun <reified T> T.logE(message: String) = Log.e(this.TAG(), message)
inline fun <reified T> T.logException(message: String) = Log.d(this.TAG(), message)