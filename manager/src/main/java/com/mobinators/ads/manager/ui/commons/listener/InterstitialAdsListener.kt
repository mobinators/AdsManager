package com.mobinators.ads.manager.ui.commons.listener


interface InterstitialAdsListener {
    fun onLoaded(adType:Int)
    fun onClicked(adType: Int)
    fun onBeforeAdShow()
    fun onDismisses(adType: Int)
    fun onError(error: String)
    fun isEnableAds(isAds: Boolean)
    fun isOffline(offline: Boolean)
}