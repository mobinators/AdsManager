package com.mobinators.ads.manager.ui.commons.rewarded

import android.annotation.SuppressLint
import android.app.Activity
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.extensions.then
import com.mobinators.ads.manager.ui.commons.enums.AdsLoadingState
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException

@SuppressLint("StaticFieldLeak")
object MediationRewardedAd {
    private var admobRewardKey: String? = null
    private var maxRewardKey: String? = null
    private var activityRef: Activity? = null
    private var loadedCallback: RewardLoadCallback? = null
    private var admobRewardAds: RewardedAd? = null
    private var maxRewardedAd: MaxRewardedAd? = null
    private var showRewardLoadCallback: ShowRewardedAdsCallback? = null
    var isAdsShow: Boolean = false

    fun loadRewardAds(context: Activity, isPurchased: Boolean, listener: RewardLoadCallback) {
        this.activityRef = context
        this.loadedCallback = listener
        if (isPurchased) {
            this.loadedCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(this.activityRef!!).not()) {
            this.loadedCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.NETWORK_OFF)
            return
        }
        initSelectedRewardAds()
    }

    fun showRewardAds(activity: Activity, isPurchased: Boolean, listener: ShowRewardedAdsCallback) {
        this.activityRef = activity
        this.showRewardLoadCallback = listener
        if (isPurchased) {
            this.showRewardLoadCallback?.onAdsShowState(adsShowState = AdsShowState.APP_PURCHASED)
            return
        }
        showSelectedRewardAds()
    }

    private fun initSelectedRewardAds() {
        when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
            AdsConstants.ADS_OFF -> this.loadedCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_OFF)
            AdsConstants.AD_MOB_MEDIATION -> initRewardAds()
            AdsConstants.AD_MOB -> initRewardAds()
            AdsConstants.MAX_MEDIATION -> initMaxRewardAds()
            else -> this.loadedCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_STRATEGY_WRONG)
        }
    }

    private fun initRewardAds() {
        this.admobRewardKey = if (AdsConstants.testMode) {
            AdsConstants.TEST_ADMOB_REWARDED_ID
        } else {
            if (AdsApplication.getAdsModel()!!.admobMediation) {
                AdsApplication.getAdsModel()!!.admobMediationRewardedId
            } else {
                AdsApplication.getAdsModel()!!.admobRewardedID
            }
        }
        if (this.admobRewardKey.isNullOrEmpty() || this.admobRewardKey.isNullOrBlank()) {
            this.loadedCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_ID_NULL)
            return
        }
        if (AdsConstants.testMode.not()) {
            if (this.admobRewardKey == AdsConstants.TEST_ADMOB_REWARDED_ID) {
                this.loadedCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.TEST_ADS_ID)
                return
            }
        }
        logD("Admob initRewardAds ads Key: $admobRewardKey")
        RewardedAd.load(
            this.activityRef!!,
            this.admobRewardKey!!,
            AdsApplication.getAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(p0: RewardedAd) {
                    super.onAdLoaded(p0)
                    this@MediationRewardedAd.admobRewardAds = null
                    this@MediationRewardedAd.admobRewardAds = p0
                    this@MediationRewardedAd.loadedCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_LOADED)
                    val options = ServerSideVerificationOptions.Builder()
                        .setCustomData("SAMPLE_CUSTOM_DATA_STRING")
                        .build()
                    this@MediationRewardedAd.admobRewardAds!!.setServerSideVerificationOptions(
                        options
                    )
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    logException("AdMob Reward Ads Load Failed Error : ${p0.message}")
                    this@MediationRewardedAd.admobRewardAds = null
                    this@MediationRewardedAd.loadedCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_LOAD_FAILED)
                }
            })
    }

    private fun initMaxRewardAds() {
        this.maxRewardKey = AdsApplication.getAdsModel()!!.maxRewardedID
        if (this.maxRewardKey.isNullOrEmpty() || this.maxRewardKey.isNullOrBlank()) {
            this.loadedCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_ID_NULL)
            return
        }
        if (this.maxRewardKey == AdsConstants.TEST_MAX_REWARD_ADS_ID) {
            this.loadedCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.TEST_ADS_ID)
            return
        }
        logD("initMaxRewardAds ads Key: $maxRewardKey")
        this.maxRewardedAd = MaxRewardedAd.getInstance(this.maxRewardKey!!, this.activityRef!!)
        this.maxRewardedAd!!.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                this@MediationRewardedAd.loadedCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_LOADED)
            }

            override fun onAdDisplayed(p0: MaxAd) {
                this@MediationRewardedAd.isAdsShow = true
                this@MediationRewardedAd.showRewardLoadCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_DISPLAY)
            }

            override fun onAdHidden(p0: MaxAd) {
                this@MediationRewardedAd.isAdsShow = false
                this@MediationRewardedAd.showRewardLoadCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_DISMISS)
            }

            override fun onAdClicked(p0: MaxAd) {
                this@MediationRewardedAd.showRewardLoadCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_CLICKED)
            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                logException("Max Reward Ads Load Failed : Error Code: ${p1.code}")
                this@MediationRewardedAd.loadedCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_LOAD_FAILED)
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                this@MediationRewardedAd.isAdsShow = false
                this@MediationRewardedAd.showRewardLoadCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_DISPLAY_FAILED)
            }

            override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
                this@MediationRewardedAd.showRewardLoadCallback?.onRewardEarned(
                    item = p1.amount,
                    type = p1.label
                )
            }

            override fun onRewardedVideoStarted(p0: MaxAd) {
                logD("onRewardedVideoStarted")
            }

            override fun onRewardedVideoCompleted(p0: MaxAd) {
                logD("onRewardedVideoCompleted")
            }
        })
        this.maxRewardedAd!!.setRevenueListener { ad ->
            logD("setRevenueListener : ${ad.revenue}")
            val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
            adjustAdRevenue.setRevenue(ad.revenue, "USD")
            adjustAdRevenue.setAdRevenueNetwork(ad.networkName)
            adjustAdRevenue.setAdRevenueUnit(ad.adUnitId)
            adjustAdRevenue.setAdRevenuePlacement(ad.placement)
            Adjust.trackAdRevenue(adjustAdRevenue)
        }
        this.maxRewardedAd!!.loadAd()
    }

    private fun showSelectedRewardAds() {
        when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
            AdsConstants.ADS_OFF -> this.showRewardLoadCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_OFF)
            AdsConstants.AD_MOB_MEDIATION -> showRewardedAds()
            AdsConstants.AD_MOB -> showRewardedAds()
            AdsConstants.MAX_MEDIATION -> showMaxRewardedAds()
            else -> this.showRewardLoadCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_STRATEGY_WRONG)
        }
    }

    private fun showRewardedAds() {
        if (this.admobRewardAds != null) {
            if (MediationOpenAd.isShowingAd || MediationAdInterstitial.isAdsShow || MediationRewardedInterstitialAd.isAdsShow) {
                logD("Other Ads show")
            } else {
                this.admobRewardAds!!.show(this.activityRef!!) {
                    this.showRewardLoadCallback!!.onRewardEarned(
                        item = it.amount,
                        type = it.type
                    )
                }
                this.admobRewardAds!!.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            super.onAdClicked()
                            this@MediationRewardedAd.showRewardLoadCallback!!.onAdsShowState(
                                adsShowState = AdsShowState.ADS_CLICKED
                            )
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            this@MediationRewardedAd.isAdsShow = true
                            this@MediationRewardedAd.showRewardLoadCallback!!.onAdsShowState(
                                adsShowState = AdsShowState.ADS_DISPLAY
                            )
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            this@MediationRewardedAd.isAdsShow = false
                            this@MediationRewardedAd.showRewardLoadCallback!!.onAdsShowState(
                                adsShowState = AdsShowState.ADS_DISMISS
                            )
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            this@MediationRewardedAd.isAdsShow = false
                            this@MediationRewardedAd.showRewardLoadCallback!!.onAdsShowState(
                                adsShowState = AdsShowState.ADS_DISPLAY_FAILED
                            )
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                            this@MediationRewardedAd.showRewardLoadCallback!!.onAdsShowState(
                                adsShowState = AdsShowState.ADS_IMPRESS
                            )
                        }
                    }
                initSelectedRewardAds()
            }

        } else {
            initSelectedRewardAds()
        }
    }

    private fun showMaxRewardedAds() {
        this.maxRewardedAd?.isReady?.then {
            if (MediationOpenAd.isShowingAd || MediationAdInterstitial.isAdsShow || MediationRewardedInterstitialAd.isAdsShow) {
                logD("Other Ads Show")
            } else {
                this.maxRewardedAd!!.showAd()
            }
        } ?: run {
            initSelectedRewardAds()
        }
    }

    fun onDestroy() {
        admobRewardAds?.fullScreenContentCallback = null
        maxRewardedAd?.setListener(null)
        maxRewardedAd?.setRevenueListener(null)
    }

    interface RewardLoadCallback {
        fun onAdsLoadState(adsLoadingState: AdsLoadingState)
    }

    interface ShowRewardedAdsCallback {
        fun onRewardEarned(item: Int, type: String)
        fun onAdsShowState(adsShowState: AdsShowState)
    }
}