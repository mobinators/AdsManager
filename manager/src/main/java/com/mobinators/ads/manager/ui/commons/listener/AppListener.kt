package com.mobinators.ads.manager.ui.commons.listener

interface AppListener {
    fun onDownload()
    fun onInstalled()
    fun onCancel()
    fun onFailure(error: Exception)
    fun onNoUpdateAvailable()
}