package com.mobinators.ads.manager.ui.commons.listener

import com.google.android.gms.ads.rewarded.RewardItem

interface OnRewardedAdListener {
    fun onError(error: String)
    fun onAdLoaded(adType: Int)
    fun onClicked(adType: Int)
    fun onDismissClick(adType: Int)
    fun onRewarded(item: RewardItem)
    fun isEnableAds(isAds: Boolean)
    fun isOffline(offline: Boolean)
}