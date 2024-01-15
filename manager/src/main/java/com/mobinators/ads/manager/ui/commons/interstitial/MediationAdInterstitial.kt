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
import com.mobinators.ads.manager.ui.commons.openad.MediationOpenAd
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
            this.adsLoadCallback!!.onAdsError("You have purchased")
            return
        }
        this.admobKey = if (AdsConstants.testMode) {
            AdsConstants.TEST_ADMOB_INTERSTITIAL_ID
        } else {
            if (AdsApplication.getAdsModel()!!.admobMediation) {
                AdsApplication.getAdsModel()!!.admobMediationInterstitialId
            } else {
                AdsApplication.getAdsModel()!!.admobInterstitialID
            }
        }
        this.maxKey = if (AdsConstants.testMode) {
            AdsConstants.TEST_MAX_INTERSTITIAL_ADS_ID
        } else {
            AdsApplication.getAdsModel()!!.maxInterstitialID
        }
        if (AdsUtils.isOnline(this.contextRef!!).not()) {
            this.adsLoadCallback!!.onAdsError(error = "Interstitial Ads Network Error")
            return
        }
        initSelectedInterstitialAds()
    }

    private fun initSelectedInterstitialAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.adsLoadCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> initInterstitialAds()
                AdsConstants.AD_MOB -> initInterstitialAds()
                AdsConstants.MAX_MEDIATION -> initMaxInterstitialAds()
                else -> this.adsLoadCallback!!.onAdsError(error = "Ads Strategy wrong")
            }

        } catch (error: Exception) {
            this.adsLoadCallback!!.onAdsError(error = "initSelectedInterstitialAds Error : ${error.localizedMessage}")
        }
    }

    private fun initInterstitialAds() {
        try {
            if (AdsApplication.isAdmobInLimit()) {
                if (AdsApplication.applyLimitOnAdmob) {
                    this.adsLoadCallback!!.onAdsError("Interstitial add banned in current duw to admob limit")
                    return
                }
            }
            if (this.admobKey!!.isEmpty() || this.admobKey!!.isBlank()) {
                this.adsLoadCallback!!.onAdsError(error = "Null Interstitial Ads IDS Found")
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.admobKey == AdsConstants.TEST_ADMOB_INTERSTITIAL_ID) {
                    this.adsLoadCallback!!.onAdsError(error = "NULL OR TEST Interstitial Ads IDS FOUND because your app is release mode")
                    return
                }
            }
            InterstitialAd.load(
                this.contextRef!!,
                admobKey!!,
                AdsApplication.getAdRequest(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ads: InterstitialAd) {
                        super.onAdLoaded(ads)
                        admobInterstitialAds = ads
                        logD("Interstitial Admob Ads Loaded")
                        this@MediationAdInterstitial.adsLoadCallback!!.onAdsLoaded()
                    }

                    override fun onAdFailedToLoad(err: LoadAdError) {
                        super.onAdFailedToLoad(err)
                        logD("Admob Front Ad  Error : ${err.message}")
                        this@MediationAdInterstitial.adsLoadCallback!!.onAdsError(error = " Interstitial Ads Load Failed")
                        admobInterstitialAds = null
                    }
                })
        } catch (error: Exception) {
            this.adsLoadCallback!!.onAdsError(error = "Interstitial Ads Load Error: ${error.localizedMessage}")
        }
    }

    private fun initMaxInterstitialAds() {
        try {
            if (this.maxKey!!.isEmpty() || this.maxKey!!.isBlank()) {
                this.adsLoadCallback!!.onAdsError(error = " NULL  Max Interstitial Ads IDS FOUND")
                return
            }
            if (AdsConstants.testMode.not()) {
                if (this.maxKey == AdsConstants.TEST_MAX_INTERSTITIAL_ADS_ID) {
                    this.adsLoadCallback!!.onAdsError(error = "NULL OR TEST MAX Interstitial Ads IDS FOUND because your app is release mode")
                    return
                }
            }
            this.maxInterstitialAds = MaxInterstitialAd("YOUR_AD_UNIT_ID", this.activityRef!!)
            this.maxInterstitialAds!!.loadAd()
            this.maxInterstitialAds!!.setListener(object : MaxAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    this@MediationAdInterstitial.adsLoadCallback!!.onAdsLoaded()
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    this@MediationAdInterstitial.isAdsShow = true
                }

                override fun onAdHidden(p0: MaxAd) {
                    this@MediationAdInterstitial.isAdsShow = false
                    this@MediationAdInterstitial.adsShowCallback!!.onAdsDismiss()
                }

                override fun onAdClicked(p0: MaxAd) {
                    this@MediationAdInterstitial.adsShowCallback!!.onAdsClicked()
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    this@MediationAdInterstitial.isAdsShow = false
                    this@MediationAdInterstitial.adsLoadCallback!!.onAdsError(error = "Max Interstitial Ads failed to load")
                    if (AdsApplication.isAdmobInLimit()) {
                        AdsApplication.applyLimitOnAdmob = true
                    }
                    this@MediationAdInterstitial.maxInterstitialAds = null
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    this@MediationAdInterstitial.isAdsShow = false
                    this@MediationAdInterstitial.adsShowCallback!!.onAdsError(error = "Max Interstitial Ads failed Display")
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
        } catch (error: Exception) {
            this@MediationAdInterstitial.adsShowCallback!!.onAdsError(error = "Max Interstitial Ads Error : ${error.localizedMessage}")
        }
    }

    fun showInterstitialAds(activity: Activity, isPurchased: Boolean, listener: AdsShowCallback) {
        this.adsShowCallback = listener
        this.activityRef = activity
        if (isPurchased) {
            this.adsShowCallback!!.onAdsError("You have purchased")
            return
        }
        if (AdsUtils.isOnline(this.contextRef!!).not()) {
            this.adsShowCallback!!.onAdsError(error = "Interstitial Ads Network Error")
            return
        }
        showSelectedInterstitialAds()
    }

    private fun showSelectedInterstitialAds() {
        try {
            when (AdsApplication.getAdsModel()?.strategy?.toInt() ?: 0) {
                AdsConstants.ADS_OFF -> this.adsShowCallback!!.onAdsOff()
                AdsConstants.AD_MOB_MEDIATION -> showAdmobInterstitialAds()
                AdsConstants.AD_MOB -> showAdmobInterstitialAds()
                AdsConstants.MAX_MEDIATION -> showMaxInterstitialAds()
                else -> this.adsShowCallback!!.onAdsError(error = "Ads Strategy wrong")
            }

        } catch (error: Exception) {
            this.adsShowCallback!!.onAdsError(error = "Show Admob SelectedInterstitial Ads Error : ${error.localizedMessage}")
        }
    }


    private fun showAdmobInterstitialAds() {
        try {
            if (this.admobInterstitialAds != null) {
                if (MediationOpenAd.isShowingAd.not()) {
                    this.admobInterstitialAds!!.show(this.activityRef!!)
                    this.admobInterstitialAds!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                super.onAdClicked()
                                this@MediationAdInterstitial.adsShowCallback!!.onAdsClicked()
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                this@MediationAdInterstitial.isAdsShow = false
                                this@MediationAdInterstitial.adsShowCallback!!.onAdsImpress()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                this@MediationAdInterstitial.isAdsShow = false
                                this@MediationAdInterstitial.adsShowCallback!!.onAdsDismiss()
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                this@MediationAdInterstitial.isAdsShow = false
                                this@MediationAdInterstitial.adsShowCallback!!.onAdsError(error = "Show Full Screen Interstitial Ads Failed : ${p0.message}")
                                this@MediationAdInterstitial.admobInterstitialAds = null
                            }

                            override fun onAdShowedFullScreenContent() {
                                this@MediationAdInterstitial.isAdsShow = true
                                super.onAdShowedFullScreenContent()
                                this@MediationAdInterstitial.admobInterstitialAds = null
                            }
                        }
                }
                initSelectedInterstitialAds()
            } else {
                initSelectedInterstitialAds()
            }
        } catch (error: Exception) {
            this.adsShowCallback!!.onAdsError("Show Admob Interstitial Ads Error : ${error.localizedMessage}")
        }
    }

    private fun showMaxInterstitialAds() {
        try {
            if (this.maxInterstitialAds != null) {
                if (this.maxInterstitialAds!!.isReady) {
                    if (MediationOpenAd.isShowingAd.not()) {
                        this.maxInterstitialAds!!.showAd()
                    }
                } else {
                    initSelectedInterstitialAds()
                }
            } else {
                initSelectedInterstitialAds()
            }
        } catch (error: Exception) {
            this.adsShowCallback!!.onAdsError(error = " Show Max Interstitial Ads Error : ${error.localizedMessage}")
        }
    }

    interface LoadCallback {
        fun onAdsLoaded()
        fun onAdsError(error: String)
        fun onAdsOff()
    }

    interface AdsShowCallback {
        fun onAdsOff()
        fun onAdsError(error: String)
        fun onAdsClicked()
        fun onAdsDismiss()
        fun onAdsImpress()
    }
}