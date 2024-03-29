package com.mobinators.ads.manager.ui.commons.models

data class AdsModel(
    var admobAppID: String? = null,
    var admobInterstitialID: String? = null,
    var admobNativeID: String? = null,
    var admobBannerID: String? = null,
    var collapseBannerID: String? = null,
    var admobOpenAdID: String? = null,
    var admobRewardedID: String? = null,
    var admobRewardedInterstitialID: String? = null,
    var admobMediationBannerId: String? = null,
    var admobMediationNativeId: String? = null,
    var admobMediationInterstitialId: String? = null,
    var admobMediationRewardedId: String? = null,
    var admobMediation: Boolean = false,
    var strategy: Long = 0,
    var maxAppId: String? = null,
    var maxBannerID: String? = null,
    var maxInterstitialID: String? = null,
    var maxNativeID: String? = null,
    var maxAppOpenID: String? = null,
    var maxRewardedID: String? = null,
    var isAppOpenAdd: Boolean = false,
    var isRateUsDialog: Long = 0
)
