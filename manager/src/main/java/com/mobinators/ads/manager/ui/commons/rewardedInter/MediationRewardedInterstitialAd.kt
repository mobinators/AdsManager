package com.mobinators.ads.manager.ui.commons.rewardedInter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.ui.commons.enums.AdsLoadingState
import com.mobinators.ads.manager.ui.commons.enums.AdsShowState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logE

@SuppressLint("StaticFieldLeak")
object MediationRewardedInterstitialAd {
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var showRewardCallback: ShowRewardAdsCallback? = null
    private var rewardedLoadAds: RewardedLoadAds? = null
    private var admobRewardedInterKey: String? = null
    private var currentActivity: Activity? = null
    private var contextRef: Context? = null
    var isAdsShow: Boolean = false


    fun loadRewardedInterstitialAds(
        activity: Context,
        isPurchased: Boolean,
        listener: RewardedLoadAds
    ) {
        this.contextRef = activity
        this.rewardedLoadAds = listener
        if (isPurchased) {
            this.rewardedLoadAds?.onAdsLoadState(adsLoadingState = AdsLoadingState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(this.contextRef!!).not()) {
            this.rewardedLoadAds?.onAdsLoadState(adsLoadingState = AdsLoadingState.NETWORK_OFF)
            return
        }

        if (AdsApplication.isAdmobInLimit()) {
            if (AdsApplication.applyLimitOnAdmob) {
                logE("admob limit is applied")
                return
            }
        }
        initSelectedRewardedInterstitialAds()
    }


    private fun initSelectedRewardedInterstitialAds() {
        when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
            AdsConstants.ADS_OFF -> this.rewardedLoadAds?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_OFF)
            AdsConstants.AD_MOB_MEDIATION -> {}
            AdsConstants.AD_MOB -> initRewardedInterstitialAds()
            AdsConstants.MAX_MEDIATION -> {}
            else -> this.rewardedLoadAds?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_STRATEGY_WRONG)
        }
    }

    private fun initRewardedInterstitialAds() {
        this.admobRewardedInterKey = if (AdsConstants.testMode) {
            AdsConstants.TEST_ADMOB_REWARDED_INTERSTITIAL_ID
        } else {
            AdsApplication.getAdsModel()!!.admobRewardedInterstitialID
        }
        if (this.admobRewardedInterKey!!.isEmpty() || this.admobRewardedInterKey!!.isBlank()) {
            this.rewardedLoadAds?.onAdsLoadState(adsLoadingState = AdsLoadingState.ADS_ID_NULL)
            return
        }
        if (AdsConstants.testMode.not()) {
            if (this.admobRewardedInterKey == AdsConstants.TEST_ADMOB_REWARDED_INTERSTITIAL_ID) {
                this.rewardedLoadAds?.onAdsLoadState(adsLoadingState = AdsLoadingState.TEST_ADS_ID)
                return
            }
        }
        logD("Admob initRewardedInterstitialAds ads Key: $admobRewardedInterKey")
        RewardedInterstitialAd.load(this.contextRef ?: this.currentActivity!!,
            this.admobRewardedInterKey!!,
            AdsApplication.getAdRequest(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    val options = ServerSideVerificationOptions.Builder()
                        .setCustomData("SAMPLE_CUSTOM_DATA_STRING")
                        .build()
                    rewardedInterstitialAd!!.setServerSideVerificationOptions(options)
                    this@MediationRewardedInterstitialAd.rewardedLoadAds?.onAdsLoadState(
                        adsLoadingState = AdsLoadingState.ADS_LOADED
                    )
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    this@MediationRewardedInterstitialAd.rewardedLoadAds?.onAdsLoadState(
                        adsLoadingState = AdsLoadingState.ADS_LOAD_FAILED
                    )
                    rewardedInterstitialAd = null
                }
            })
    }

    fun showRewardedInterstitialAd(
        activity: Activity,
        isPurchase: Boolean,
        listener: ShowRewardAdsCallback
    ) {
        this.currentActivity = activity
        this.showRewardCallback = listener
        if (isPurchase) {
            this.showRewardCallback?.onAdsShowState(adsShowState = AdsShowState.APP_PURCHASED)
            return
        }
        showSelectedRewardedInterstitial()
    }


    private fun showSelectedRewardedInterstitial() {
        when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
            AdsConstants.ADS_OFF -> this.showRewardCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_OFF)
            AdsConstants.AD_MOB_MEDIATION -> {}
            AdsConstants.AD_MOB -> admobRewardInterstitialAdShow()
            AdsConstants.MAX_MEDIATION -> {}
            else -> this.showRewardCallback?.onAdsShowState(adsShowState = AdsShowState.ADS_STRATEGY_WRONG)
        }
    }


    private fun admobRewardInterstitialAdShow() {
        if (rewardedInterstitialAd != null) {
            if (MediationOpenAd.isShowingAd || MediationAdInterstitial.isAdsShow || MediationRewardedAd.isAdsShow) {
                logD("Other Ads Show")
            } else {
                rewardedInterstitialAd!!.show(currentActivity!!) {
                    this.showRewardCallback!!.onAdsReward(it)
                }
                rewardedInterstitialAd!!.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            super.onAdClicked()
                            this@MediationRewardedInterstitialAd.showRewardCallback?.onAdsShowState(
                                adsShowState = AdsShowState.ADS_CLICKED
                            )
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            this@MediationRewardedInterstitialAd.isAdsShow = false
                            this@MediationRewardedInterstitialAd.showRewardCallback!!.onAdsDismiss(
                                this@MediationRewardedInterstitialAd.rewardedInterstitialAd!!.rewardItem
                            )

                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            this@MediationRewardedInterstitialAd.isAdsShow = false
                            this@MediationRewardedInterstitialAd.showRewardCallback!!.onAdsShowState(
                                adsShowState = AdsShowState.ADS_DISPLAY_FAILED
                            )
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                            this@MediationRewardedInterstitialAd.showRewardCallback!!.onAdsShowState(
                                adsShowState = AdsShowState.ADS_IMPRESS
                            )
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            this@MediationRewardedInterstitialAd.isAdsShow = true
                            this@MediationRewardedInterstitialAd.showRewardCallback!!.onAdsShowState(
                                adsShowState = AdsShowState.ADS_DISPLAY
                            )
                        }
                    }
                initSelectedRewardedInterstitialAds()
            }
        } else {
            initSelectedRewardedInterstitialAds()
        }
    }


    interface RewardedLoadAds {

        fun onAdsLoadState(adsLoadingState: AdsLoadingState)
    }

    interface ShowRewardAdsCallback {
        fun onAdsReward(item: RewardItem)
        fun onAdsDismiss(item: RewardItem)
        fun onAdsShowState(adsShowState: AdsShowState)
    }
}