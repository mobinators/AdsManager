package com.mobinators.ads.manager.ui.commons.listener

interface OnNativeAdListener {
    fun onError(error: String)
    fun onLoaded(adType: Int)
    fun onAdClicked(adType: Int)
    fun isEnableAds(isAds: Boolean)
    fun isOffline(offline: Boolean)
}