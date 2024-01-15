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
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils

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
            this.rewardedLoadAds!!.onAdsError("You have purchased")
            return
        }
        if (AdsUtils.isOnline(this.contextRef!!).not()) {
            this.rewardedLoadAds!!.onAdsError("Network Off can not loaded reward Interstitial")
            return
        }
        this.admobRewardedInterKey = if (AdsConstants.testMode) {
            AdsConstants.TEST_ADMOB_REWARDED_INTERSTITIAL_ID
        } else {
            AdsApplication.getAdsModel()!!.admobRewardedInterstitialID
        }
        if (AdsApplication.isAdmobInLimit()) {
            if (AdsApplication.applyLimitOnAdmob) {
                this.rewardedLoadAds!!.onAdsError("admob limit is applied")
                return
            }
        }
        initSelectedRewardedInterstitialAds()
    }


    private fun initSelectedRewardedInterstitialAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.rewardedLoadAds!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> {}
                AdsConstants.AD_MOB -> initRewardedInterstitialAds()
                AdsConstants.MAX_MEDIATION -> {}
                else -> this.rewardedLoadAds!!.onAdsError(error = "Ads Strategy wrong")
            }
        } catch (error: Exception) {
            this.rewardedLoadAds!!.onAdsError(error = "initSelectedRewardedInterstitialAds Error : ${error.localizedMessage}")
        }
    }

    private fun initRewardedInterstitialAds() {
        try {
            if (this.admobRewardedInterKey!!.isEmpty() || this.admobRewardedInterKey!!.isBlank()) {
                this.rewardedLoadAds!!.onAdsError(error = "Reward Interstitial Admob Ads Id is null")
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.admobRewardedInterKey == AdsConstants.TEST_ADMOB_REWARDED_INTERSTITIAL_ID) {
                    this.rewardedLoadAds!!.onAdsError(error = "NULL OR TEST Rewarded Interstitial Ads IDS FOUND")
                    return
                }
            }
            RewardedInterstitialAd.load(this.contextRef!!, this.admobRewardedInterKey!!,
                AdsApplication.getAdRequest(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        rewardedInterstitialAd = ad
                        val options = ServerSideVerificationOptions.Builder()
                            .setCustomData("SAMPLE_CUSTOM_DATA_STRING")
                            .build()
                        rewardedInterstitialAd!!.setServerSideVerificationOptions(options)
                        this@MediationRewardedInterstitialAd.rewardedLoadAds!!.onAdsLoaded()
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        this@MediationRewardedInterstitialAd.rewardedLoadAds!!.onAdsError(error = " Init Reward Interstitial Ads Error :  ${adError.message}")
                        rewardedInterstitialAd = null
                    }
                })

        } catch (error: Exception) {
            this.rewardedLoadAds!!.onAdsError(error = "Init Reward Interstitial Ads Error  : ${error.localizedMessage}")
        }
    }

    fun showRewardedInterstitialAd(
        activity: Activity,
        isPurchase: Boolean,
        listener: ShowRewardAdsCallback
    ) {
        this.currentActivity = activity
        this.showRewardCallback = listener
        if (isPurchase) {
            this.showRewardCallback!!.onAdsError(error = "You have purchase")
            return
        }
        try {
            if (AdsUtils.isOnline(activity).not()) {
                this.showRewardCallback!!.onAdsError(error = "Network Error ")
                return
            }
            showSelectedRewardedInterstitial()
        } catch (error: Exception) {
            this.showRewardCallback!!.onAdsError(error = " Show Reward Interstitial Ads Error: ${error.localizedMessage}")
        }
    }


    private fun showSelectedRewardedInterstitial() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.showRewardCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> {}
                AdsConstants.AD_MOB -> admobRewardInterstitialAdShow()
                AdsConstants.MAX_MEDIATION -> {}
                else -> this.showRewardCallback!!.onAdsError(error = "Ads Strategy wrong")
            }
        } catch (error: Exception) {
            this.showRewardCallback!!.onAdsError(error = "initSelectedRewardedInterstitialAds Error : ${error.localizedMessage}")
        }
    }


    private fun admobRewardInterstitialAdShow() {
        try {
            if (rewardedInterstitialAd != null) {
                if (MediationOpenAd.isShowingAd.not()) {
                    rewardedInterstitialAd!!.show(currentActivity!!) {
                        this.showRewardCallback!!.onAdsReward(it)
                    }
                    rewardedInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                super.onAdClicked()
                                this@MediationRewardedInterstitialAd.showRewardCallback!!.onAdsClicked()
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
                                this@MediationRewardedInterstitialAd.showRewardCallback!!.onAdsError(
                                    error = "Display Reward Interstitial Ads Failed : ${p0.message} "
                                )
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                this@MediationRewardedInterstitialAd.isAdsShow = false
                                this@MediationRewardedInterstitialAd.showRewardCallback!!.onAdsImpress()
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                this@MediationRewardedInterstitialAd.isAdsShow = true
                            }
                        }
                }
                initSelectedRewardedInterstitialAds()
            } else {
                initSelectedRewardedInterstitialAds()
            }
        } catch (error: Exception) {
            this.showRewardCallback!!.onAdsError("Show Reward Interstitial Ads Error: ${error.localizedMessage}")
        }
    }


    interface RewardedLoadAds {
        fun onAdsLoaded()
        fun onAdsOff()
        fun onAdsError(error: String)
    }

    interface ShowRewardAdsCallback {
        fun onAdsOff()
        fun onAdsError(error: String)
        fun onAdsReward(item: RewardItem)
        fun onAdsClicked()
        fun onAdsImpress()
        fun onAdsDismiss(item: RewardItem)
    }
}