package com.mobinators.ads.manager.ui.commons.rewarded

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
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
import com.mobinators.ads.manager.ui.commons.listener.OnRewardedAdListener
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow

@SuppressLint("StaticFieldLeak")
object MediationRewardedAd {
    private var onRewardedAdListener: OnRewardedAdListener? = null
    private var maxRewardedAd: MaxRewardedAd? = null
    private var currentActivity: Activity? = null
    private var admobRewardedKey: String? = null
    private var rewardedAd: RewardedAd? = null
    private var maxRewardedKey: String? = null
    private var retryAttempt = 0

    fun loadRewardedAd(activity: Activity, isPurchase: Boolean, listener: OnRewardedAdListener) {
        this.currentActivity = activity
        this.onRewardedAdListener = listener
        if (isPurchase) {
            this.onRewardedAdListener!!.onError("You have purchase")
            return
        }
        try {
            this.admobRewardedKey = if (AdsConstants.isInit) {
                AdsConstants.TEST_ADMOB_REWARDED_ID
            } else {
                if (AdsApplication.getAdsModel()!!.admobMediation) {
                    AdsApplication.getAdsModel()!!.admobMediationRewardedId
                } else {
                    AdsApplication.getAdsModel()!!.admobRewardedID
                }
            }
            this.maxRewardedKey = if (AdsConstants.isInit) {
                AdsConstants.TEST_MAX_REWARD_ADS_ID
            } else {
                AdsApplication.getAdsModel()!!.maxRewardedID
            }

            if (AdsApplication.isAdmobInLimit()) {
                if (AdsApplication.applyLimitOnAdmob) {
                    onRewardedAdListener!!.onError("admob limit is applied")
                    return
                }
            }
            if (AdsConstants.isInit.not()) {
                Handler(Looper.getMainLooper()).postDelayed({
                    loadRewardedAd(activity, isPurchase, listener)
                }, 2000)
            }

            selectAd()
        } catch (error: Exception) {
            this.onRewardedAdListener!!.onError("showRewardedAds Error : ${error.localizedMessage}")
        }
    }

    private fun selectAd() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> {
                    this.onRewardedAdListener!!.isEnableAds(false)
                }

                AdsConstants.AD_MOB_MEDIATION -> {
                    this.onRewardedAdListener!!.isEnableAds(true)
                    admobRewardedAd()
                }

                AdsConstants.AD_MOB -> {
                    this.onRewardedAdListener!!.isEnableAds(true)
                    admobRewardedAd()
                }

                AdsConstants.MAX_MEDIATION -> {
                    this.onRewardedAdListener!!.isEnableAds(true)
                    maxRewardedAd()
                }
            }
        } catch (error: Exception) {
            this.onRewardedAdListener!!.onError("showRewardedAds Error : ${error.localizedMessage}")
        }
    }

    private fun admobRewardedAd() {
        try {
            if (this.admobRewardedKey!!.isEmpty() || this.admobRewardedKey!!.isBlank()) {
                this.onRewardedAdListener!!.onError("AdMob Reward Ads Id is null")
                return
            }
            if (AdsUtils.isOnline(this.currentActivity!!).not()) {
                logD("is Offline ")
                this.onRewardedAdListener!!.isOffline(true)
                return
            }
            if (this.admobRewardedKey == AdsConstants.TEST_ADMOB_REWARDED_ID) {
                logD("Test Ids")
                if (AdsConstants.testMode.not()) {
                    logD("NULL OR TEST IDS FOUND")
                    this.onRewardedAdListener!!.onError("NULL OR TEST IDS FOUND")
                    return
                }
            }
            logD("Admob Rewarded Ad Id : $admobRewardedKey")
            RewardedAd.load(
                currentActivity!!,
                admobRewardedKey!!,
                AdsApplication.getAdRequest(),
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(loadError: LoadAdError) {
                        super.onAdFailedToLoad(loadError)
                        rewardedAd = null
                        onRewardedAdListener!!.onError(loadError.message)
                        if (AdsApplication.isAdmobInLimit()) {
                            AdsApplication.applyLimitOnAdmob = true
                        }
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        super.onAdLoaded(ad)
                        rewardedAd = ad
                        onRewardedAdListener!!.onAdLoaded(AdsConstants.AD_MOB)
                        val options = ServerSideVerificationOptions.Builder()
                            .setCustomData("SAMPLE_CUSTOM_DATA_STRING")
                            .build()
                        rewardedAd!!.setServerSideVerificationOptions(options)
                        admobRewardedShow()
                    }
                })
        } catch (error: Exception) {
            this.onRewardedAdListener!!.onError("showRewardedAds Error : ${error.localizedMessage}")
        }
    }

    private fun admobRewardedShow() {
        try {
            if (rewardedAd != null) {
                rewardedAd!!.show(currentActivity!!) {
                    logD("Reward : ${it.type} : ${it.amount}")
                    onRewardedAdListener!!.onRewarded(it)
                }
                rewardedAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        super.onAdClicked()
                        onRewardedAdListener!!.onClicked(AdsConstants.AD_MOB)
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        rewardedAd = null
                        onRewardedAdListener!!.onDismissClick(AdsConstants.AD_MOB, rewardedAd!!.rewardItem)
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        rewardedAd = null
                        onRewardedAdListener!!.onError(p0.message)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        onRewardedAdListener!!.onAdLoaded(AdsConstants.AD_MOB)
                    }
                }
            }
        } catch (error: Exception) {
            this.onRewardedAdListener!!.onError("showRewardedAds Error : ${error.localizedMessage}")
        }
    }

    private fun maxRewardedAd() {
        try {
            if (this.maxRewardedKey!!.isEmpty() || this.maxRewardedKey!!.isBlank()) {
                this.onRewardedAdListener!!.onError("Max Reward Ads Id is null")
                return
            }
            if (AdsUtils.isOnline(this.currentActivity!!).not()) {
                logD("is Offline ")
                this.onRewardedAdListener!!.isOffline(true)
                return
            }
            if (this.maxRewardedKey == AdsConstants.TEST_MAX_REWARD_ADS_ID) {
                logD("Test Ids")
                if (AdsConstants.testMode.not()) {
                    logD("NULL OR TEST IDS FOUND")
                    this.onRewardedAdListener!!.onError("NULL OR TEST IDS FOUND")
                    return
                }
            }
            logD("Max Rewarded Ad Id : $maxRewardedKey")
            maxRewardedAd = MaxRewardedAd.getInstance(this.maxRewardedKey!!, this.currentActivity!!)
            maxRewardedAd!!.setListener(object : MaxRewardedAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    logD("onAdLoaded")
                    retryAttempt = 0
                    onRewardedAdListener!!.onAdLoaded(AdsConstants.MAX_MEDIATION)
                    showMaxRewardedAd()
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    logD("onAdDisplayed")
                }

                override fun onAdHidden(p0: MaxAd) {
                    logD("onAdHidden")
                }

                override fun onAdClicked(p0: MaxAd) {
                    logD("onAdClicked")
                    onRewardedAdListener!!.onClicked(AdsConstants.MAX_MEDIATION)
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    logD("onAdLoadFailed")
                    retryAttempt++
                    val delayMillis =
                        TimeUnit.SECONDS.toMillis(2.0.pow(min(6, retryAttempt)).toLong())
                    Handler(Looper.myLooper()!!).postDelayed(
                        { maxRewardedAd!!.loadAd() },
                        delayMillis
                    )
                    onRewardedAdListener!!.onError(p1.message)
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    logD("onAdDisplayFailed")
                    onRewardedAdListener!!.onError(p1.message)
                }

                override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
                    logD("onUserRewarded")
                }

                @Deprecated("Deprecated in Java", ReplaceWith(
                    "logD(\"onRewardedVideoStarted\")",
                    "pak.developer.app.managers.extensions.logD"
                )
                )
                override fun onRewardedVideoStarted(p0: MaxAd) {
                    logD("onRewardedVideoStarted")
                }

                @Deprecated("Deprecated in Java", ReplaceWith(
                    "logD(\"onRewardedVideoCompleted\")",
                    "pak.developer.app.managers.extensions.logD"
                )
                )
                override fun onRewardedVideoCompleted(p0: MaxAd) {
                    logD("onRewardedVideoCompleted")
                }
            })
            maxRewardedAd!!.setRevenueListener { ad ->
                logD("setRevenueListener : ${ad.revenue}")
                val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
                adjustAdRevenue.setRevenue(ad.revenue, "USD")
                adjustAdRevenue.setAdRevenueNetwork(ad.networkName)
                adjustAdRevenue.setAdRevenueUnit(ad.adUnitId)
                adjustAdRevenue.setAdRevenuePlacement(ad.placement)
                Adjust.trackAdRevenue(adjustAdRevenue)
            }
            maxRewardedAd!!.loadAd()
        } catch (error: Exception) {
            this.onRewardedAdListener!!.onError("showRewardedAds Error : ${error.localizedMessage}")
        }
    }

    private fun showMaxRewardedAd() {
        if (this.maxRewardedAd!!.isReady) {
            this.maxRewardedAd!!.showAd()
        }
    }
}