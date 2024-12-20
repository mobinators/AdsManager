package com.mobinators.ads.manager.ui.commons.interstitial

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.ui.commons.enums.AdsLoadingState
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD

@SuppressLint("StaticFieldLeak")
object MediationAdInterstitial {
    private var admobInterstitialAds: InterstitialAd? = null
    private var maxInterstitialAds: MaxInterstitialAd? = null
    private var adsShowCallback: AdsShowCallback? = null
    private var adsLoadCallback: LoadCallback? = null
    private var activityRef: Activity? = null
    private var contextRef: Context? = null
    private var admobKey: String? = null
    private var maxKey: String? = null
    var isAdsShow: Boolean = false
    fun loadInterstitialAds(activity: Context, isPurchased: Boolean, listener: LoadCallback) {
        this.adsLoadCallback = listener
        this.contextRef = activity
        if (isPurchased) {
            this.adsLoadCallback!!.onAdsLoadState(adsLoadingState = AdsLoadingState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(this.contextRef!!).not()) {
            this.adsLoadCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.NETWORK_OFF)
            return
        }
        initSelectedInterstitialAds()
    }

    private fun initSelectedInterstitialAds() {
        when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
            AdsConstants.ADS_OFF -> this.adsLoadCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_OFF)
            AdsConstants.AD_MOB_MEDIATION -> initInterstitialAds()
            AdsConstants.AD_MOB -> initInterstitialAds()
            AdsConstants.MAX_MEDIATION -> initMaxInterstitialAds()
            else -> this.adsLoadCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_STRATEGY_WRONG)
        }
    }

    private fun initInterstitialAds() {
        this.admobKey = if (AdsConstants.testMode) {
            AdsConstants.TEST_ADMOB_INTERSTITIAL_ID
        } else {
            if (AdsApplication.getAdsModel()!!.admobMediation) {
                AdsApplication.getAdsModel()!!.admobMediationInterstitialId
            } else {
                AdsApplication.getAdsModel()!!.admobInterstitialID
            }
        }
        if (AdsApplication.isAdmobInLimit()) {
            if (AdsApplication.applyLimitOnAdmob) {
                logD("Interstitial add banned in current duw to admob limit")
                return
            }
        }
        if (this.admobKey!!.isEmpty() || this.admobKey!!.isBlank()) {
            this.adsLoadCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_ID_NULL)
            return
        }
        if (AdsConstants.testMode.not()) {
            if (this.admobKey == AdsConstants.TEST_ADMOB_INTERSTITIAL_ID) {
                this.adsLoadCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.TEST_ADS_ID)
                return
            }
        }
        logD("Admob Interstitial Ads Unit Key: $admobKey")
        InterstitialAd.load(this.contextRef ?: this.activityRef!!,
            admobKey!!,
            AdsApplication.getAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ads: InterstitialAd) {
                    super.onAdLoaded(ads)
                    admobInterstitialAds = ads
                    this@MediationAdInterstitial.adsLoadCallback?.onAdsLoadState(
                        adsLoadingState = AdsLoadingState.ADS_LOADED
                    )
                }

                override fun onAdFailedToLoad(err: LoadAdError) {
                    super.onAdFailedToLoad(err)
                    logD("Admob Interstitial Ads :  Error Code : ${err.code}")
                    this@MediationAdInterstitial.adsLoadCallback?.onAdsLoadState(
                        adsLoadingState = AdsLoadingState.ADS_LOAD_FAILED
                    )
                    admobInterstitialAds = null
                }
            })
    }

    private fun initMaxInterstitialAds() {
        this.maxKey = AdsApplication.getAdsModel()!!.maxInterstitialID
        if (this.maxKey!!.isEmpty() || this.maxKey!!.isBlank()) {
            this.adsLoadCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_ID_NULL)
            return
        }
        if (this.maxKey == AdsConstants.TEST_MAX_INTERSTITIAL_ADS_ID) {
            this.adsLoadCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.TEST_ADS_ID)
            return
        }
        logD("Interstitial Ads : $maxKey")
        this.maxInterstitialAds =
            MaxInterstitialAd(this.maxKey, this.contextRef ?: this.activityRef)
        this.maxInterstitialAds!!.setListener(object : MaxAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                this@MediationAdInterstitial.adsLoadCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_LOADED)
            }

            override fun onAdDisplayed(p0: MaxAd) {
                this@MediationAdInterstitial.isAdsShow = true
                logD("Interstitial Ads Display : $isAdsShow")
                this@MediationAdInterstitial.adsShowCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_DISPLAY)
            }

            override fun onAdHidden(p0: MaxAd) {
                this@MediationAdInterstitial.isAdsShow = false
                this@MediationAdInterstitial.adsShowCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_DISMISS)
            }

            override fun onAdClicked(p0: MaxAd) {
                this@MediationAdInterstitial.adsShowCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_CLICKED)
            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                logD("Interstitial Ads :  Error Code : ${p1.code}")
                this@MediationAdInterstitial.isAdsShow = false
                this@MediationAdInterstitial.adsLoadCallback?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_LOAD_FAILED)
                if (AdsApplication.isAdmobInLimit()) {
                    AdsApplication.applyLimitOnAdmob = true
                }
                this@MediationAdInterstitial.maxInterstitialAds = null
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                this@MediationAdInterstitial.isAdsShow = false
                this@MediationAdInterstitial.adsShowCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_DISPLAY_FAILED)
            }
        })
        this.maxInterstitialAds!!.setRevenueListener { ad ->
            val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
            adjustAdRevenue.setRevenue(ad.revenue, "USD")
            adjustAdRevenue.setAdRevenueNetwork(ad.networkName)
            adjustAdRevenue.setAdRevenueUnit(ad.adUnitId)
            adjustAdRevenue.setAdRevenuePlacement(ad.placement)
            Adjust.trackAdRevenue(adjustAdRevenue)
        }
        this.maxInterstitialAds!!.loadAd()
        logD("Interstitial Ads : $maxInterstitialAds")
    }

    fun showInterstitialAds(activity: Activity, isPurchased: Boolean, listener: AdsShowCallback) {
        this.adsShowCallback = listener
        this.activityRef = activity
        if (isPurchased) {
            this.adsShowCallback?.onAdsShowState(adsShowState = AdsShowState.APP_PURCHASED)
            return
        }
        showSelectedInterstitialAds()
    }

    private fun showSelectedInterstitialAds() {
        when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
            AdsConstants.ADS_OFF -> this.adsShowCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_OFF)
            AdsConstants.AD_MOB_MEDIATION -> showAdmobInterstitialAds()
            AdsConstants.AD_MOB -> showAdmobInterstitialAds()
            AdsConstants.MAX_MEDIATION -> showMaxInterstitialAds()
            else -> this.adsShowCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_STRATEGY_WRONG)
        }
    }


    private fun showAdmobInterstitialAds() {
        if (this.admobInterstitialAds != null) {
            if (MediationOpenAd.isShowingAd || MediationRewardedAd.isAdsShow || MediationRewardedInterstitialAd.isAdsShow) {
                logD("Other Ads Show")
            } else {
                this.admobInterstitialAds!!.show(this.activityRef!!)
                this.admobInterstitialAds!!.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            super.onAdClicked()
                            this@MediationAdInterstitial.adsShowCallback?.onAdsShowState(
                                adsShowState = AdsShowState.ADS_CLICKED
                            )
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                            this@MediationAdInterstitial.adsShowCallback?.onAdsShowState(
                                adsShowState = AdsShowState.ADS_IMPRESS
                            )
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            this@MediationAdInterstitial.isAdsShow = false
                            this@MediationAdInterstitial.adsShowCallback?.onAdsShowState(
                                adsShowState = AdsShowState.ADS_DISMISS
                            )
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            this@MediationAdInterstitial.isAdsShow = false
                            this@MediationAdInterstitial.adsShowCallback?.onAdsShowState(
                                adsShowState = AdsShowState.ADS_DISPLAY_FAILED
                            )
                            this@MediationAdInterstitial.admobInterstitialAds = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            this@MediationAdInterstitial.isAdsShow = true
                            logD("Interstitial Ads Display : $isAdsShow")
                            super.onAdShowedFullScreenContent()
                            this@MediationAdInterstitial.admobInterstitialAds = null
                            this@MediationAdInterstitial.adsShowCallback?.onAdsShowState(
                                adsShowState = AdsShowState.ADS_DISPLAY
                            )
                        }
                    }
                initSelectedInterstitialAds()
            }
        } else {
            initSelectedInterstitialAds()
        }
    }

    private fun showMaxInterstitialAds() {
        if (this.maxInterstitialAds != null) {
            if (this.maxInterstitialAds!!.isReady) {
                if (MediationOpenAd.isShowingAd.not()) {
                    this.maxInterstitialAds!!.showAd(this.activityRef)
                    initSelectedInterstitialAds()
                }
            } else {
                logD("Interstitial Ads : Max Interstitial Ads Not Ready")
                this.adsShowCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_DISPLAY_FAILED)
                initSelectedInterstitialAds()
            }
        } else {
            logD("Interstitial Ads : Max Interstitial Ads Null")
            if (AdsUtils.isOnline(activityRef!!).not()) {
                this.adsShowCallback?.onAdsShowState(adsShowState = AdsShowState.NETWORK_OFF)
            } else {
                this.adsShowCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_DISPLAY_FAILED)
            }
            initSelectedInterstitialAds()
        }
    }


    fun onDestroy() {
        admobInterstitialAds?.fullScreenContentCallback = null
        maxInterstitialAds?.setListener(null)
        maxInterstitialAds?.setRevenueListener(null)
    }

    interface LoadCallback {
        fun onAdsLoadState(adsLoadingState: AdsLoadingState)
    }

    interface AdsShowCallback {
        fun onAdsShowState(adsShowState: AdsShowState)
    }
}