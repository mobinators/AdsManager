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
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
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
            this.loadedCallback!!.onAdsError(errorState = AdsErrorState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(this.activityRef!!).not()) {
            this.loadedCallback!!.onAdsError(errorState = AdsErrorState.NETWORK_OFF)
            return
        }
        initSelectedRewardAds()
    }

    fun showRewardAds(activity: Activity, isPurchased: Boolean, listener: ShowRewardedAdsCallback) {
        this.activityRef = activity
        this.showRewardLoadCallback = listener
        if (isPurchased) {
            this.showRewardLoadCallback!!.onAdsError(errorState = AdsErrorState.APP_PURCHASED)
            return
        }
        showSelectedRewardAds()
    }

    private fun initSelectedRewardAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.loadedCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> initRewardAds()
                AdsConstants.AD_MOB -> initRewardAds()
                AdsConstants.MAX_MEDIATION -> initMaxRewardAds()
                else -> this.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_STRATEGY_WRONG)
            }
        } catch (error: Exception) {
            logException("init Selected Reward Error : ${error.localizedMessage}")
        }
    }

    private fun initRewardAds() {
        try {
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
                this.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_ID_NULL)
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.admobRewardKey == AdsConstants.TEST_ADMOB_REWARDED_ID) {
                    this.loadedCallback!!.onAdsError(errorState = AdsErrorState.TEST_ADS_ID)
                    return
                }
            }
            RewardedAd.load(
                this.activityRef!!,
                this.admobRewardKey!!,
                AdsApplication.getAdRequest(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(p0: RewardedAd) {
                        super.onAdLoaded(p0)
                        this@MediationRewardedAd.admobRewardAds = null
                        this@MediationRewardedAd.admobRewardAds = p0
                        this@MediationRewardedAd.loadedCallback!!.onAdsLoaded()
                        val options = ServerSideVerificationOptions.Builder()
                            .setCustomData("SAMPLE_CUSTOM_DATA_STRING")
                            .build()
                        this@MediationRewardedAd.admobRewardAds!!.setServerSideVerificationOptions(
                            options
                        )
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        this@MediationRewardedAd.admobRewardAds = null
                        this@MediationRewardedAd.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_LOAD_FAILED)
                    }
                })
        } catch (error: Exception) {
            logException("init load Reward Error : ${error.localizedMessage}")
        }
    }

    private fun initMaxRewardAds() {
        try {
            this.maxRewardKey = if (AdsConstants.testMode) {
                AdsConstants.TEST_MAX_REWARD_ADS_ID
            } else {
                AdsApplication.getAdsModel()!!.maxRewardedID
            }
            if (this.maxRewardKey.isNullOrEmpty() || this.maxRewardKey.isNullOrBlank()) {
                this.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_ID_NULL)
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.maxRewardKey == AdsConstants.TEST_MAX_REWARD_ADS_ID) {
                    this.loadedCallback!!.onAdsError(errorState = AdsErrorState.TEST_ADS_ID)
                    return
                }
            }
            this.maxRewardedAd = MaxRewardedAd.getInstance(this.maxRewardKey!!, this.activityRef!!)
            this.maxRewardedAd!!.setListener(object : MaxRewardedAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    this@MediationRewardedAd.loadedCallback!!.onAdsLoaded()
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    this@MediationRewardedAd.isAdsShow = true
                    this@MediationRewardedAd.showRewardLoadCallback?.onAdsDisplay()
                }

                override fun onAdHidden(p0: MaxAd) {
                    this@MediationRewardedAd.isAdsShow = false
                    this@MediationRewardedAd.showRewardLoadCallback?.onAdsError(errorState = AdsErrorState.ADS_DISMISS)
                }

                override fun onAdClicked(p0: MaxAd) {
                    this@MediationRewardedAd.showRewardLoadCallback?.onAdsClicked()
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    logException("Max Reward Ads Load Failed : ${p1.message}")
                    this@MediationRewardedAd.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_LOAD_FAILED)
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    this@MediationRewardedAd.isAdsShow = false
                    this@MediationRewardedAd.showRewardLoadCallback?.onAdsError(errorState = AdsErrorState.ADS_DISPLAY_FAILED)
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

        } catch (error: Exception) {
            logException("Init Max Reward Load Ads Error: ${error.localizedMessage}")
        }
    }

    private fun showSelectedRewardAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.showRewardLoadCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> showRewardedAds()
                AdsConstants.AD_MOB -> showRewardedAds()
                AdsConstants.MAX_MEDIATION -> showMaxRewardedAds()
                else -> this.showRewardLoadCallback!!.onAdsError(errorState = AdsErrorState.ADS_STRATEGY_WRONG)
            }
        } catch (error: Exception) {
            logException("Show Selected Reward Ads Error : ${error.localizedMessage}")
        }
    }

    private fun showRewardedAds() {
        try {
            if (this.admobRewardAds != null) {
                if (MediationOpenAd.isShowingAd.not()) {


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
                                this@MediationRewardedAd.showRewardLoadCallback!!.onAdsClicked()
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                this@MediationRewardedAd.isAdsShow = true
                                this@MediationRewardedAd.showRewardLoadCallback!!.onAdsDisplay()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                this@MediationRewardedAd.isAdsShow = false
                                this@MediationRewardedAd.showRewardLoadCallback!!.onAdsError(
                                    errorState = AdsErrorState.ADS_DISMISS
                                )
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                this@MediationRewardedAd.isAdsShow = false
                                this@MediationRewardedAd.showRewardLoadCallback!!.onAdsError(
                                    errorState = AdsErrorState.ADS_DISPLAY_FAILED
                                )
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                this@MediationRewardedAd.isAdsShow = false
                                this@MediationRewardedAd.showRewardLoadCallback!!.onAdsError(
                                    errorState = AdsErrorState.ADS_IMPRESS
                                )
                            }
                        }
                }
                initSelectedRewardAds()
            } else {
                initSelectedRewardAds()
            }
        } catch (error: Exception) {
            logException("Show Rewarded Ads Error : ${error.localizedMessage}")
        }
    }

    private fun showMaxRewardedAds() {
        try {
            if (this.maxRewardedAd!!.isReady) {
                if (MediationOpenAd.isShowingAd.not()) {
                    this.maxRewardedAd!!.showAd()
                }
                initSelectedRewardAds()
            } else {
                initSelectedRewardAds()
            }

        } catch (error: Exception) {
            logException(" Show Max Reward Ads Error : ${error.localizedMessage}")
        }
    }

    interface RewardLoadCallback {
        fun onAdsLoaded()
        fun onAdsOff()
        fun onAdsError(errorState: AdsErrorState)
    }

    interface ShowRewardedAdsCallback {
        fun onAdsOff()
        fun onRewardEarned(item: Int, type: String)
        fun onAdsClicked()
        fun onAdsDisplay()
        fun onAdsError(errorState: AdsErrorState)
    }
}