package com.mobinators.ads.manager.ui.commons.openad

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.ui.commons.enums.AdsErrorState
import com.mobinators.ads.manager.ui.commons.interstitial.MediationAdInterstitial
import com.mobinators.ads.manager.ui.commons.rewarded.MediationRewardedAd
import com.mobinators.ads.manager.ui.commons.rewardedInter.MediationRewardedInterstitialAd
import com.mobinators.ads.manager.ui.commons.utils.AdsConstants
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD
import pak.developer.app.managers.extensions.logException

@SuppressLint("StaticFieldLeak")
object MediationOpenAd {

    private var activityRef: Activity? = null
    private var admobAppOpenAdsID: String? = null
    private var admobAppOPenAd: AppOpenAd? = null
    private var loadedCallback: AdsLoadedCallback? = null
    private var maxAppOpenAdsId: String? = null
    private var maxAppOpenAds: MaxAppOpenAd? = null
    private var showCallback: AdsShowAppOpenCallback? = null
    private var contextRef: Context? = null
    var isShowingAd: Boolean = false

    fun loadAppOpenAds(activity: Context, isPurchased: Boolean, listener: AdsLoadedCallback) {
        this.contextRef = activity
        this.loadedCallback = listener
        if (isPurchased) {
            this.loadedCallback!!.onAdsError(errorState = AdsErrorState.APP_PURCHASED)
            return
        }
        if (AdsUtils.isOnline(this.contextRef!!).not()) {
            this.loadedCallback!!.onAdsError(errorState = AdsErrorState.NETWORK_OFF)
            return
        }
        if (AdsApplication.getAdsModel()!!.isAppOpenAdd.not()) {
            logException("App OPen Ads is not enable")
            return
        }
        initSelectedAppOPenAds()
    }

    fun showAppOpenAds(activity: Activity, isPurchased: Boolean, listener: AdsShowAppOpenCallback) {
        this.activityRef = activity
        this.showCallback = listener
        if (isPurchased) {
            this.showCallback!!.onAdsError(errorState = AdsErrorState.APP_PURCHASED)
            return
        }
        showSelectedAppOpenAds()

    }

    private fun initSelectedAppOPenAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.loadedCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> logD("No Admob Mediation For App OPen Ads")
                AdsConstants.AD_MOB -> initAppOpenAds()
                AdsConstants.MAX_MEDIATION -> initMaxAppOpenAds()
                else -> this.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_STRATEGY_WRONG)
            }
        } catch (error: Exception) {
            logException("Init Selected App Open Ads Error : ${error.localizedMessage}")
        }
    }

    private fun initAppOpenAds() {
        try {
            this.admobAppOpenAdsID = if (AdsConstants.testMode) {
                AdsConstants.TEST_ADMOB_OPEN_APP_ID
            } else {
                AdsApplication.getAdsModel()!!.admobOpenAdID
            }

            if (this.admobAppOpenAdsID.isNullOrEmpty() || this.admobAppOpenAdsID.isNullOrBlank()) {
                this.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_ID_NULL)
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.admobAppOpenAdsID == AdsConstants.TEST_ADMOB_OPEN_APP_ID) {
                    this.loadedCallback!!.onAdsError(errorState = AdsErrorState.TEST_ADS_ID)
                    return
                }
            }
            AppOpenAd.load(
                this.contextRef!!,
                this.admobAppOpenAdsID!!,
                AdsApplication.getAdRequest(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(p0: AppOpenAd) {
                        super.onAdLoaded(p0)
                        this@MediationOpenAd.admobAppOPenAd = p0
                        this@MediationOpenAd.loadedCallback!!.onAdsLoaded()
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        this@MediationOpenAd.admobAppOPenAd = null
                        this@MediationOpenAd.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_LOAD_FAILED)
                    }
                })
        } catch (error: Exception) {
            logException("Init App Open Ads Error : ${error.localizedMessage}")
        }
    }

    private fun initMaxAppOpenAds() {
        try {
            this.maxAppOpenAdsId = if (AdsConstants.testMode) {
                AdsConstants.TEST_MAX_APP_OPEN_ADS_ID
            } else {
                AdsApplication.getAdsModel()!!.maxAppOpenID
            }
            if (this.maxAppOpenAdsId.isNullOrEmpty() || this.maxAppOpenAdsId.isNullOrBlank()) {
                this.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_ID_NULL)
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.maxAppOpenAdsId == AdsConstants.TEST_MAX_APP_OPEN_ADS_ID) {
                    this.loadedCallback!!.onAdsError(errorState = AdsErrorState.TEST_ADS_ID)
                    return
                }
            }
            this.maxAppOpenAds = MaxAppOpenAd(this.maxAppOpenAdsId!!, this.contextRef!!)
            this.maxAppOpenAds!!.setListener(object : MaxAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    this@MediationOpenAd.loadedCallback!!.onAdsLoaded()
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    this@MediationOpenAd.isShowingAd = true
                    this@MediationOpenAd.showCallback?.onAdsDisplay()
                }

                override fun onAdHidden(p0: MaxAd) {
                    this@MediationOpenAd.isShowingAd = false
                    this@MediationOpenAd.showCallback?.onAdsError(errorState = AdsErrorState.ADS_DISMISS)
                }

                override fun onAdClicked(p0: MaxAd) {
                    this@MediationOpenAd.showCallback!!.onAdsClicked()
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    this@MediationOpenAd.isShowingAd = false
                    this@MediationOpenAd.loadedCallback!!.onAdsError(errorState = AdsErrorState.ADS_LOAD_FAILED)
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    this@MediationOpenAd.isShowingAd = false
                    this@MediationOpenAd.showCallback?.onAdsError(errorState = AdsErrorState.ADS_DISPLAY_FAILED)
                }
            })
            this.maxAppOpenAds!!.setRevenueListener {
                val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
                adjustAdRevenue.setRevenue(it.revenue, "USD")
                adjustAdRevenue.setAdRevenueNetwork(it.networkName)
                adjustAdRevenue.setAdRevenueUnit(it.adUnitId)
                adjustAdRevenue.setAdRevenuePlacement(it.placement)
                Adjust.trackAdRevenue(adjustAdRevenue)
            }
            this.maxAppOpenAds!!.loadAd()
        } catch (error: Exception) {
            logException("Init Max App Open Ads Error : ${error.localizedMessage}")
        }
    }

    private fun showSelectedAppOpenAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.showCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> logD("No Admob Mediation For App OPen Ads")
                AdsConstants.AD_MOB -> showAdmobAppOpenAds()
                AdsConstants.MAX_MEDIATION -> showMaxAppOpenAds()
                else -> this.showCallback!!.onAdsError(errorState = AdsErrorState.ADS_STRATEGY_WRONG)
            }

        } catch (error: Exception) {
            logException("Show App Open Ads Error : ${error.localizedMessage}")
        }
    }

    private fun showAdmobAppOpenAds() {
        try {
            if (this.admobAppOPenAd != null) {
                if (MediationRewardedAd.isAdsShow || MediationAdInterstitial.isAdsShow || MediationRewardedInterstitialAd.isAdsShow) {
                    logD("Other Ads Open")
                } else {
                    logD("Other Ads Open : Reward : ${MediationRewardedAd.isAdsShow} : Interstitial : ${MediationAdInterstitial.isAdsShow} : Reward Interstitial : ${MediationRewardedInterstitialAd.isAdsShow}")
                    this.admobAppOPenAd!!.show(this.activityRef!!)
                    this.admobAppOPenAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                super.onAdClicked()
                                this@MediationOpenAd.showCallback!!.onAdsClicked()
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                this@MediationOpenAd.isShowingAd = true
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                this@MediationOpenAd.isShowingAd = false
                                this@MediationOpenAd.showCallback!!.onAdsError(errorState = AdsErrorState.ADS_DISMISS)
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                this@MediationOpenAd.isShowingAd = false
                                this@MediationOpenAd.showCallback!!.onAdsError(errorState = AdsErrorState.ADS_DISPLAY_FAILED)
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                this@MediationOpenAd.showCallback!!.onAdsError(errorState = AdsErrorState.ADS_IMPRESS)
                            }
                        }
                    initSelectedAppOPenAds()
                }
            } else {
                initSelectedAppOPenAds()
            }
        } catch (error: Exception) {
            logException(" Show App Open Ads Error : ${error.localizedMessage}")
        }
    }

    private fun showMaxAppOpenAds() {
        try {
            if (this.maxAppOpenAds!!.isReady) {
                if (MediationRewardedAd.isAdsShow || MediationAdInterstitial.isAdsShow || MediationRewardedInterstitialAd.isAdsShow) {
                    logD("Ads Loaded Other")
                } else {
                    this.maxAppOpenAds!!.showAd()
                    initSelectedAppOPenAds()
                }
            } else {
                initSelectedAppOPenAds()
            }
        } catch (error: Exception) {
            logException("Show Max App Open Ads Error : ${error.localizedMessage}")
        }
    }

    interface AdsLoadedCallback {
        fun onAdsOff()
        fun onAdsLoaded()
        fun onAdsError(errorState: AdsErrorState)
    }

    interface AdsShowAppOpenCallback {
        fun onAdsOff()
        fun onAdsClicked()
        fun onAdsDisplay()
        fun onAdsError(errorState: AdsErrorState)
    }
}