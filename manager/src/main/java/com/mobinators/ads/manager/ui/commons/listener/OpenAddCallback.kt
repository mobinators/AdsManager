package com.mobinators.ads.manager.ui.commons.listener

interface OpenAddCallback {
    fun onDismissClick()
    fun onErrorToShow(error: String)
    fun isEnableAds(isAds: Boolean)
    fun isOffline(offline: Boolean)
}