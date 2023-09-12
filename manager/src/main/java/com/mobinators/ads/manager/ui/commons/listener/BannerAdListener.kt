package com.mobinators.ads.manager.ui.commons.listener

import com.facebook.ads.AdView

interface BannerAdListener {
    fun onLoaded(adType: Int)
    fun onAdClicked(adType: Int)
    fun onError(error: String)
    fun onFacebookAdCreated(facebookBanner: AdView)
    fun isEnableAds(isAds: Boolean)
    fun isOffline(offline: Boolean)
}